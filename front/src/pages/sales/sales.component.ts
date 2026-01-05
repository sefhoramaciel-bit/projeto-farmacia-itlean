import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { ApiService } from '../../services/api.service';
import { SalesService } from '../../services/sales.service';
import { NotificationService } from '../../services/notification.service';
import { Customer, Medicine, CartItem, Sale, SaleRequest } from '../../models/types';
import { LogService } from '../../services/log.service';

@Component({
  selector: 'app-sales',
  templateUrl: './sales.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule],
})
export class SalesComponent {
  private apiService = inject(ApiService);
  private salesService = inject(SalesService);
  private notificationService = inject(NotificationService);
  private logService = inject(LogService);
  // Fix: Explicitly type FormBuilder to resolve issues where its methods were not recognized and to fix cascading type errors.
  private fb: FormBuilder = inject(FormBuilder);

  customer = signal<Customer | null>(null);
  cart = signal<CartItem[]>([]);
  searchResults = signal<Medicine[]>([]);
  allMedicines = signal<Medicine[]>([]);
  isSearching = signal(false);
  // Controla o índice da imagem atual no carrossel para cada medicamento
  currentImageIndex = signal<Map<string, number>>(new Map());

  cpfForm = this.fb.group({
    cpf: ['', [Validators.required, Validators.pattern(/^\d{3}\.\d{3}\.\d{3}\-\d{2}$/)]]
  });

  // Fix: Explicitly type the form control to avoid 'unknown' type in valueChanges stream.
  searchControl = this.fb.control<string>('');

  total = computed(() => {
    return this.cart().reduce((acc, item) => acc + item.preco * item.quantidadeCarrinho, 0);
  });
  
  constructor() {
    this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      // Fix: Explicitly type the 'term' parameter to resolve 'unknown' type from valueChanges stream.
      switchMap((term: string | null) => {
        if (!term || term.length < 2) {
          // Se não há termo de busca, mostra todos os medicamentos válidos
          this.isSearching.set(false);
          const allValid = this.filterValidMedicines(this.allMedicines());
          this.searchResults.set(allValid);
          return [];
        }
        this.isSearching.set(true);
        return this.apiService.searchMedicines(term);
      })
    ).subscribe(results => {
      // Filtra medicamentos vencidos e ordena alfabeticamente por nome
      const validResults = this.filterValidMedicines(results);
      const sortedResults = validResults.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' }));
      this.searchResults.set(sortedResults);
      this.isSearching.set(false);
    });
  }

  /**
   * Filtra medicamentos ativos e dentro da data de validade
   * Medicamentos vencidos NÃO podem aparecer na loja de vendas
   */
  private filterValidMedicines(medicines: Medicine[]): Medicine[] {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    
    return medicines.filter(med => {
      // Deve estar ativo
      if (!med.ativo) {
        return false;
      }
      
      // Se tem validade, deve ser futura ou igual a hoje
      // Medicamentos vencidos NÃO PODEM aparecer na listagem de vendas
      if (med.validade) {
        try {
          // A data pode vir em formato ISO (YYYY-MM-DD) ou outro formato
          let dataValidade: Date;
          const validadeStr = String(med.validade).trim();
          
          if (!validadeStr) {
            // Se a validade está vazia, permite o medicamento
            return true;
          }
          
          // Tenta diferentes formatos de data
          if (validadeStr.match(/^\d{4}-\d{2}-\d{2}/)) {
            // Formato ISO: YYYY-MM-DD
            const [year, month, day] = validadeStr.split('-').map(Number);
            dataValidade = new Date(year, month - 1, day);
          } else if (validadeStr.match(/^\d{2}\/\d{2}\/\d{4}/)) {
            // Formato brasileiro: DD/MM/YYYY
            const [day, month, year] = validadeStr.split('/').map(Number);
            dataValidade = new Date(year, month - 1, day);
          } else {
            // Tenta parse padrão
            dataValidade = new Date(validadeStr);
          }
          
          // Verifica se a data é válida
          if (isNaN(dataValidade.getTime())) {
            // Se não conseguiu parsear, exclui o medicamento por segurança
            return false;
          }
          
          // Normaliza as horas para comparação correta
          dataValidade.setHours(0, 0, 0, 0);
          
          // Medicamentos vencidos (validade anterior a hoje) NÃO aparecem
          // Usa getTime() para comparação precisa
          if (dataValidade.getTime() < hoje.getTime()) {
            return false;
          }
        } catch (error) {
          // Em caso de erro, exclui o medicamento por segurança
          return false;
        }
      }
      
      return true;
    });
  }

  /**
   * Carrega todos os medicamentos ativos e válidos
   */
  private loadAllValidMedicines() {
    this.isSearching.set(true);
    this.apiService.getMedicines(true).subscribe({
      next: (medicines) => {
        const validMedicines = this.filterValidMedicines(medicines);
        const sortedMedicines = validMedicines.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' }));
        this.allMedicines.set(sortedMedicines);
        this.searchResults.set(sortedMedicines);
        this.isSearching.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar medicamentos:', err);
        this.isSearching.set(false);
        this.notificationService.error('Erro', 'Não foi possível carregar os medicamentos.');
      }
    });
  }

  findCustomer() {
    if (this.cpfForm.invalid) {
      this.notificationService.error('CPF Inválido', 'Por favor, insira um CPF válido no formato 000.000.000-00.');
      return;
    }
    const cpf = this.cpfForm.value.cpf!;
    this.apiService.findCustomerByCpf(cpf).subscribe(cust => {
      if (cust) {
        this.customer.set(cust);
        this.notificationService.success('Cliente Encontrado', `Iniciando venda para ${cust.nome}.`);
        // Carrega todos os medicamentos ativos e válidos quando o cliente é encontrado
        this.loadAllValidMedicines();
      } else {
        this.notificationService.error('Cliente não Encontrado', 'Nenhum cliente com este CPF. Cadastre-o primeiro.');
      }
    });
  }

  addToCart(medicine: Medicine) {
    const existingItem = this.cart().find(item => item.id === medicine.id);
    if (existingItem) {
      this.updateQuantity(existingItem.id, existingItem.quantidadeCarrinho + 1);
    } else {
      if (medicine.quantidadeEstoque > 0) {
        const newItem: CartItem = { ...medicine, quantidadeCarrinho: 1 };
        this.cart.update(currentCart => [...currentCart, newItem]);
      } else {
        this.notificationService.error('Sem Estoque', `${medicine.nome} não está disponível em estoque.`);
      }
    }
  }

  updateQuantity(medicineId: string, newQuantity: number) {
    this.cart.update(currentCart => 
      currentCart.map(item => {
        if (item.id === medicineId) {
          if (newQuantity <= 0) return null; // Marked for removal
          if (newQuantity > item.quantidadeEstoque) {
            this.notificationService.error('Estoque Insuficiente', `Apenas ${item.quantidadeEstoque} unidades de ${item.nome} disponíveis.`);
            return { ...item, quantidadeCarrinho: item.quantidadeEstoque };
          }
          return { ...item, quantidadeCarrinho: newQuantity };
        }
        return item;
      }).filter(item => item !== null) as CartItem[]
    );
  }

  removeFromCart(medicineId: string) {
    this.cart.update(currentCart => currentCart.filter(item => item.id !== medicineId));
  }

  finalizeSale() {
    if (this.cart().length === 0) {
      this.notificationService.error('Carrinho Vazio', 'Adicione pelo menos um item para finalizar a venda.');
      return;
    }

    const sale: SaleRequest = {
      clienteId: this.customer()!.id,
      itens: this.cart().map(item => ({
        medicamentoId: item.id,
        quantidade: item.quantidadeCarrinho,
      })),
    };
    
    this.salesService.create(sale).subscribe({
      next: (res) => {
        this.notificationService.success('Venda Finalizada!', `Venda #${res.id} registrada com sucesso.`);
        this.logService.logAction('Criação', 'Venda', `Venda #${res.id} finalizada para o cliente '${res.clienteNome}' no valor total de R$ ${res.valorTotal.toFixed(2)}.`);
        // Limpa a venda sem criar venda cancelada
        this.clearSale();
      },
      error: (err) => {
        const errorMessage = err?.error?.error || 'Não foi possível registrar a venda. Tente novamente.';
        this.notificationService.error('Erro na Venda', errorMessage);
      }
    });
  }

  resetSale() {
    // Se há itens no carrinho e um cliente selecionado, cria uma venda cancelada
    if (this.cart().length > 0 && this.customer()) {
      const sale: SaleRequest = {
        clienteId: this.customer()!.id,
        itens: this.cart().map(item => ({
          medicamentoId: item.id,
          quantidade: item.quantidadeCarrinho,
        })),
      };
      
      this.salesService.createCancelada(sale).subscribe({
        next: (res) => {
          this.notificationService.success('Venda Cancelada', `Venda cancelada registrada com sucesso.`);
          this.logService.logAction('Criação', 'Venda', `Venda cancelada registrada para o cliente '${this.customer()?.nome}'.`);
          this.clearSale();
        },
        error: (err) => {
          console.error('Erro ao registrar venda cancelada:', err);
          // Mesmo com erro, limpa a venda
          this.clearSale();
        }
      });
    } else {
      // Se não há itens ou cliente, apenas limpa
      this.clearSale();
    }
  }

  private clearSale() {
    this.customer.set(null);
    this.cart.set([]);
    this.searchResults.set([]);
    this.allMedicines.set([]);
    this.cpfForm.reset();
    this.searchControl.reset();
  }

  formatCpf(event: Event) {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');
    if (value.length > 11) value = value.slice(0, 11);

    if (value.length > 9) {
      value = value.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    } else if (value.length > 6) {
      value = value.replace(/(\d{3})(\d{3})(\d{0,3})/, '$1.$2.$3');
    } else if (value.length > 3) {
      value = value.replace(/(\d{3})(\d{0,3})/, '$1.$2');
    }
    this.cpfForm.controls.cpf.setValue(value, { emitEvent: false });
  }

  /**
   * Constrói a URL completa da imagem do medicamento
   */
  getImageUrl(imagePath: string): string {
    if (!imagePath) return '';
    // Se já começa com http, retorna como está
    if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
      return imagePath;
    }
    // Caso contrário, adiciona o base URL do backend
    return `http://localhost:8081${imagePath}`;
  }

  /**
   * Formata a data de validade para exibição
   */
  formatValidade(validade: string | undefined): string {
    if (!validade) return 'Sem validade';
    
    try {
      const validadeStr = String(validade).trim();
      let date: Date;
      
      // Tenta diferentes formatos de data
      if (validadeStr.match(/^\d{4}-\d{2}-\d{2}/)) {
        // Formato ISO: YYYY-MM-DD
        const [year, month, day] = validadeStr.split('-').map(Number);
        date = new Date(year, month - 1, day);
      } else if (validadeStr.match(/^\d{2}\/\d{2}\/\d{4}/)) {
        // Formato brasileiro: DD/MM/YYYY
        const [day, month, year] = validadeStr.split('/').map(Number);
        date = new Date(year, month - 1, day);
      } else {
        // Tenta parse padrão
        date = new Date(validadeStr);
      }
      
      // Verifica se a data é válida
      if (isNaN(date.getTime())) {
        return 'Data inválida';
      }
      
      return date.toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      });
    } catch (error) {
      return 'Data inválida';
    }
  }

  /**
   * Obtém o índice da imagem atual para um medicamento
   */
  getCurrentImageIndex(medicineId: string): number {
    return this.currentImageIndex().get(medicineId) || 0;
  }

  /**
   * Define o índice da imagem atual para um medicamento
   */
  setCurrentImageIndex(medicineId: string, index: number) {
    const newMap = new Map(this.currentImageIndex());
    newMap.set(medicineId, index);
    this.currentImageIndex.set(newMap);
  }

  /**
   * Avança para a próxima imagem no carrossel
   */
  nextImage(medicineId: string, totalImages: number) {
    const current = this.getCurrentImageIndex(medicineId);
    const next = (current + 1) % totalImages;
    this.setCurrentImageIndex(medicineId, next);
  }

  /**
   * Volta para a imagem anterior no carrossel
   */
  previousImage(medicineId: string, totalImages: number) {
    const current = this.getCurrentImageIndex(medicineId);
    const prev = (current - 1 + totalImages) % totalImages;
    this.setCurrentImageIndex(medicineId, prev);
  }
}