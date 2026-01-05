import { ChangeDetectionStrategy, Component, inject, signal, afterNextRender, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { Medicine } from '../../models/types';
import { LogService } from '../../services/log.service';

@Component({
  selector: 'app-stock',
  templateUrl: './stock.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule],
})
export class StockComponent {
  private apiService = inject(ApiService);
  private notificationService = inject(NotificationService);
  private logService = inject(LogService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  // Fix: Explicitly type FormBuilder to resolve type inference issue with inject().
  private fb: FormBuilder = inject(FormBuilder);

  medicines = signal<Medicine[]>([]);
  filteredMedicines = signal<Medicine[]>([]);
  searchTerm = signal<string>('');
  isSelectOpen = signal<boolean>(false);
  selectedMedicine = signal<Medicine | null>(null);
  
  stockForm = this.fb.group({
    medicamentoId: ['', Validators.required],
    tipo: ['entrada', Validators.required],
    quantidade: [1, [Validators.required, Validators.min(1)]],
  });

  constructor() {
    afterNextRender(() => {
      this.apiService.getMedicines(true).subscribe(data => {
        // Ordena alfabeticamente por nome
        const sortedData = data.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' }));
        this.medicines.set(sortedData);
        this.applySearchFilter();
        // Após carregar os medicamentos, verifica se há um medicamento na query string
        this.checkQueryParamsAndSelectMedicine();
      });
    });
    
    // Sincroniza selectedMedicine quando o formControl muda
    this.stockForm.get('medicamentoId')?.valueChanges.subscribe(id => {
      if (!id) {
        this.selectedMedicine.set(null);
      } else if (!this.selectedMedicine() || this.selectedMedicine()?.id !== id) {
        const medicine = this.medicines().find(m => m.id === id);
        if (medicine) {
          this.selectedMedicine.set(medicine);
        }
      }
    });
  }

  private checkQueryParamsAndSelectMedicine() {
    const params = this.route.snapshot.queryParams;
    if (params['medicamento']) {
      const medicamentoId = params['medicamento'];
      // Verifica se o medicamento existe na lista
      const medicine = this.medicines().find(m => m.id === medicamentoId);
      if (medicine) {
        // Seleciona o medicamento no formulário e no signal
        this.selectedMedicine.set(medicine);
        this.stockForm.patchValue({ medicamentoId: medicamentoId });
        // Remove o queryParam da URL após selecionar
        this.router.navigate([], {
          relativeTo: this.route,
          queryParams: {},
          replaceUrl: true
        });
      }
    }
  }

  onSubmit() {
    if (this.stockForm.invalid) {
      this.notificationService.error('Formulário Inválido', 'Selecione um medicamento e a quantidade.');
      return;
    }

    const { medicamentoId, tipo, quantidade } = this.stockForm.value;
    const medicine = this.medicines().find(m => m.id === medicamentoId);

    if (!medicine) {
      this.notificationService.error('Erro', 'Medicamento não encontrado.');
      return;
    }

    if (tipo === 'saida' && quantidade! > medicine.quantidadeEstoque) {
      this.notificationService.error('Estoque Insuficiente', `A quantidade de saída (${quantidade}) é maior que o estoque atual (${medicine.quantidadeEstoque}).`);
      return;
    }
    
    // MOCK API CALL
    const entity = tipo === 'entrada' ? 'estoque/entrada' : 'estoque/saida';
    console.log('📦 StockComponent: Registrando movimentação de estoque:', { tipo, medicamentoId, quantidade });
    this.apiService.create(entity, this.stockForm.value).subscribe({
      next: () => {
        const details = `Registrada ${tipo} de ${quantidade} unidade(s) de '${medicine.nome}'.`;
        console.log('📦 StockComponent: Movimentação registrada com sucesso');
        this.notificationService.success('Movimentação Registrada!', `A ${tipo} de ${quantidade} unidade(s) de ${medicine.nome} foi registrada.`);
        this.logService.logAction('Movimentação', 'Estoque', details);
        this.stockForm.reset({ tipo: 'entrada', quantidade: 1 });
        this.selectedMedicine.set(null);
        this.searchTerm.set('');
        this.applySearchFilter();
        
        // Recarrega a lista de medicamentos para ver o estoque atualizado
        console.log('📦 StockComponent: Recarregando lista de medicamentos...');
        this.apiService.getMedicines(true).subscribe({
          next: (data) => {
            console.log('📦 StockComponent: Lista de medicamentos recarregada:', data);
            // Ordena alfabeticamente por nome
            const sortedData = data.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' }));
            this.medicines.set(sortedData);
            this.applySearchFilter();
          },
          error: (err) => {
            console.error('📦 StockComponent: Erro ao recarregar medicamentos:', err);
          }
        });
      },
      error: (err) => {
        console.error('📦 StockComponent: Erro ao registrar movimentação:', err);
        this.notificationService.error('Erro', 'Não foi possível registrar a movimentação.');
      }
    });
  }

  applySearchFilter() {
    const term = this.searchTerm().toLowerCase().trim();
    const allMedicines = this.medicines();
    
    if (!term) {
      this.filteredMedicines.set(allMedicines);
      return;
    }
    
    const filtered = allMedicines.filter(med => 
      med.nome.toLowerCase().includes(term) ||
      med.id.toLowerCase().includes(term)
    );
    this.filteredMedicines.set(filtered);
  }

  onSearchChange(term: string) {
    this.searchTerm.set(term);
    this.applySearchFilter();
    this.isSelectOpen.set(true);
  }

  toggleSelect() {
    this.isSelectOpen.set(!this.isSelectOpen());
    if (!this.isSelectOpen()) {
      this.searchTerm.set('');
      this.applySearchFilter();
    }
  }

  selectMedicine(med: Medicine) {
    this.selectedMedicine.set(med);
    this.stockForm.patchValue({ medicamentoId: med.id });
    this.searchTerm.set('');
    this.isSelectOpen.set(false);
    this.applySearchFilter();
  }

  getDisplayValue(): string {
    const selected = this.selectedMedicine();
    if (selected) {
      return `${selected.nome} (Estoque: ${selected.quantidadeEstoque})`;
    }
    return 'Selecione um medicamento...';
  }

  clearSelection() {
    this.selectedMedicine.set(null);
    this.stockForm.patchValue({ medicamentoId: '' });
    this.searchTerm.set('');
    this.applySearchFilter();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.custom-select-container')) {
      this.isSelectOpen.set(false);
    }
  }
}