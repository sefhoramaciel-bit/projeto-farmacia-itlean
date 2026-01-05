import { ChangeDetectionStrategy, Component, inject, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { MedicinesService } from '../../services/medicines.service';
import { NotificationService } from '../../services/notification.service';
import { Medicine, Category } from '../../models/types';
import { ModalComponent } from '../../components/modal/modal.component';
import { LogService } from '../../services/log.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-medicines',
  templateUrl: './medicines.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule, ModalComponent],
})
export class MedicinesComponent {
  private apiService = inject(ApiService);
  private medicinesService = inject(MedicinesService);
  private notificationService = inject(NotificationService);
  private logService = inject(LogService);
  private authService = inject(AuthService);
  // Fix: Explicitly type FormBuilder to resolve type inference issue with inject().
  private fb: FormBuilder = inject(FormBuilder);

  activeTab = signal<'active' | 'inactive'>('active');
  medicines = signal<Medicine[]>([]);
  filteredMedicines = signal<Medicine[]>([]);
  searchTerm = signal<string>('');
  categories = signal<Category[]>([]);
  isLoading = signal(true);
  
  isModalOpen = signal(false);
  modalTitle = signal('Adicionar Medicamento');
  currentMedicineId = signal<string | null>(null);

  medicineForm = this.fb.group({
    nome: ['', [Validators.required]],
    descricao: [''],
    preco: [0, [Validators.required, this.validatePreco]],
    quantidadeEstoque: [0, [Validators.required, this.validateQuantidade]],
    validade: ['', [Validators.required, this.validateDataFutura]],
    categoriaId: ['', [Validators.required]],
    ativo: [true], // Campo para ativo/inativo, padrão é true
  });

  // Validador customizado para preço > 0
  validatePreco(control: any) {
    const value = control.value;
    if (value === null || value === undefined || value === '') {
      return { required: true };
    }
    const numValue = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(numValue) || numValue <= 0) {
      return { min: true };
    }
    return null;
  }

  // Validador customizado para quantidade >= 0
  validateQuantidade(control: any) {
    const value = control.value;
    if (value === null || value === undefined || value === '') {
      return { required: true };
    }
    const numValue = typeof value === 'string' ? parseInt(value) : value;
    if (isNaN(numValue) || numValue < 0) {
      return { min: true };
    }
    return null;
  }

  // Validador customizado para data futura
  validateDataFutura(control: any) {
    const value = control.value;
    if (!value) {
      return { required: true };
    }
    const data = new Date(value);
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    if (data <= hoje) {
      return { dataFutura: true };
    }
    return null;
  }
  
  selectedImages = signal<File[]>([]);
  imagePreviews = signal<string[]>([]);
  existingImages = signal<string[]>([]);

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  constructor() {
    console.log('🏥 MedicinesComponent: Constructor called');
    
    // Observa mudanças nos query params para recarregar quando necessário
    this.route.queryParams.subscribe(params => {
      console.log('🏥 MedicinesComponent: Query params changed:', params);
      // Se não há query params (navegação direta para /medicamentos), limpa o filtro
      if (!params['id']) {
        console.log('🏥 MedicinesComponent: No ID in params, loading all medicines');
        // Limpa qualquer filtro anterior e recarrega todos os medicamentos
        this.loadMedicines();
      } else {
        console.log('🏥 MedicinesComponent: ID in params, loading filtered medicines');
        this.loadMedicines();
      }
    });
    
    this.loadMedicines();
    console.log('🏥 MedicinesComponent: Loading categories...');
    this.apiService.getCategories().subscribe({
      next: (cats) => {
        console.log('🏥 MedicinesComponent: Categories loaded:', cats);
        this.categories.set(cats);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('🏥 MedicinesComponent: Error loading categories:', err);
        this.cdr.detectChanges();
      }
    });
  }

  loadMedicines() {
    console.log('🏥 MedicinesComponent: loadMedicines() called');
    this.isLoading.set(true);
    const isActive = this.activeTab() === 'active';
    console.log('🏥 MedicinesComponent: Active tab:', isActive);
    
    // Verifica se há um ID na query string antes de carregar
    const params = this.route.snapshot.queryParams;
    const filterById = params['id'];
    console.log('🏥 MedicinesComponent: Query params:', params);
    console.log('🏥 MedicinesComponent: filterById:', filterById);
    
    console.log('🏥 MedicinesComponent: Calling medicinesService.getAll() to get all medicines...');
    // Carrega TODOS os medicamentos (ativos e inativos) para poder encontrar qualquer um
    this.medicinesService.getAll().subscribe({
      next: (data) => {
        console.log('🏥 MedicinesComponent: getMedicines response received (all medicines):', data);
        
        let filteredData = isActive 
          ? data.filter(m => m.ativo) 
          : data.filter(m => !m.ativo);
        
        // Se há filtro por ID, aplica o filtro
        if (filterById) {
          console.log('🏥 MedicinesComponent: Filtering by medicineId from query params:', filterById);
          const medicineById = data.find(m => m.id === filterById);
          
          if (medicineById) {
            console.log('🏥 MedicinesComponent: Medicine found:', medicineById.nome, 'Active:', medicineById.ativo);
            // Muda para a aba correta se necessário
            if (medicineById.ativo && !isActive) {
              console.log('🏥 MedicinesComponent: Medicine is active, switching to active tab');
              this.activeTab.set('active');
              // Recarrega com a aba correta
              this.loadMedicines();
              return;
            } else if (!medicineById.ativo && isActive) {
              console.log('🏥 MedicinesComponent: Medicine is inactive, switching to inactive tab');
              this.activeTab.set('inactive');
              // Recarrega com a aba correta
              this.loadMedicines();
              return;
            }
            
            // Filtra para mostrar apenas este medicamento
            filteredData = filteredData.filter(m => m.id === filterById);
            console.log('🏥 MedicinesComponent: Filtered data by ID:', filteredData);
          } else {
            console.log('🏥 MedicinesComponent: Medicine not found with ID:', filterById);
          }
        } else {
          console.log('🏥 MedicinesComponent: No filter by ID, showing all medicines');
        }
        
        console.log(`🏥 MedicinesComponent: Filtered ${isActive ? 'active' : 'inactive'} medicines:`, filteredData);
        // Ordena alfabeticamente por nome
        const sortedData = filteredData.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' }));
        this.medicines.set(sortedData);
        this.applySearchFilter();
        this.isLoading.set(false);
        console.log('🏥 MedicinesComponent: Calling detectChanges()...');
        this.cdr.detectChanges();
        console.log('🏥 MedicinesComponent: detectChanges() completed');
        
        // NÃO abre o modal automaticamente - removido checkQueryParamsAndOpenModal()
      },
      error: (err) => {
        console.error('🏥 MedicinesComponent: ERROR loading medicines:', err);
        console.error('🏥 MedicinesComponent: Error details:', JSON.stringify(err, null, 2));
        this.isLoading.set(false);
        this.cdr.detectChanges();
      }
    });
  }


  changeTab(tab: 'active' | 'inactive') {
    console.log('🏥 MedicinesComponent: changeTab() called with tab:', tab);
    console.log('🏥 MedicinesComponent: Previous tab:', this.activeTab());
    this.activeTab.set(tab);
    this.searchTerm.set(''); // Limpa o filtro ao mudar de aba
    console.log('🏥 MedicinesComponent: New tab set:', this.activeTab());
    console.log('🏥 MedicinesComponent: Calling loadMedicines()...');
    this.loadMedicines();
  }

  openCreateModal() {
    if (!this.isAdmin()) {
      this.notificationService.error('Acesso Negado', 'Você não tem permissão para criar medicamentos. Apenas administradores podem realizar esta ação.');
      return;
    }
    console.log('🏥 MedicinesComponent: openCreateModal() called');
    this.currentMedicineId.set(null);
    this.modalTitle.set('Adicionar Medicamento');
    console.log('🏥 MedicinesComponent: Modal title set to:', this.modalTitle());
    this.medicineForm.reset();
    this.selectedImages.set([]);
    this.imagePreviews.set([]);
    this.existingImages.set([]);
    console.log('🏥 MedicinesComponent: Form reset');
    this.isModalOpen.set(true);
    console.log('🏥 MedicinesComponent: Modal opened');
  }

  openEditModal(med: Medicine) {
    if (!this.isAdmin()) {
      this.notificationService.error('Acesso Negado', 'Você não tem permissão para editar medicamentos. Apenas administradores podem realizar esta ação.');
      return;
    }
    console.log('🏥 MedicinesComponent: openEditModal() called');
    console.log('🏥 MedicinesComponent: Editing medicine:', med.nome, 'ID:', med.id);
    this.currentMedicineId.set(med.id);
    console.log('🏥 MedicinesComponent: Current medicine ID set to:', this.currentMedicineId());
    this.modalTitle.set('Editar Medicamento');
    console.log('🏥 MedicinesComponent: Modal title set to:', this.modalTitle());
    
    // Converte a data do formato dd/MM/yyyy para YYYY-MM-DD (formato do input date)
    let validadeFormatted = '';
    if (med.validade) {
      // Se a data vem no formato ISO (com T), remove a parte do tempo
      if (med.validade.includes('T')) {
        validadeFormatted = med.validade.split('T')[0];
      } else if (med.validade.match(/^\d{2}\/\d{2}\/\d{4}$/)) {
        // Se vem no formato dd/MM/yyyy, converte para YYYY-MM-DD
        const [dia, mes, ano] = med.validade.split('/');
        validadeFormatted = `${ano}-${mes}-${dia}`;
      } else {
        // Se já está no formato YYYY-MM-DD, usa direto
        validadeFormatted = med.validade;
      }
    }
    
    const formData: any = {
      nome: med.nome,
      descricao: med.descricao || '',
      preco: med.preco,
      quantidadeEstoque: med.quantidadeEstoque,
      validade: validadeFormatted,
      ativo: med.ativo !== undefined ? med.ativo : true,
      categoriaId: med.categoria?.id || '',
    };
    console.log('🏥 MedicinesComponent: Form data prepared:', formData);
    console.log('🏥 MedicinesComponent: Original validade:', med.validade, 'Formatted validade:', validadeFormatted);
    this.medicineForm.patchValue(formData);
    this.selectedImages.set([]);
    this.imagePreviews.set([]);
    this.existingImages.set(med.imagens || []);
    console.log('🏥 MedicinesComponent: Form patched with medicine data');
    this.isModalOpen.set(true);
    console.log('🏥 MedicinesComponent: Modal opened');
  }

  onImageSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const files = Array.from(input.files);
      const currentImages = this.selectedImages();
      const existingImgs = this.existingImages();
      const totalWithNew = currentImages.length + files.length + existingImgs.length;
      
      if (totalWithNew > 3) {
        this.notificationService.error('Muitas Imagens', `Você possui ${totalWithNew} imagem(ns). É permitido no máximo 3 imagens no total.`);
        input.value = '';
        return;
      }
      
      const allFiles = [...currentImages, ...files];
      this.selectedImages.set(allFiles);
      
      // Cria previews das novas imagens
      const currentPreviews = this.imagePreviews();
      const newPreviews: string[] = [];
      
      files.forEach((file, index) => {
        const reader = new FileReader();
        reader.onload = (e) => {
          newPreviews.push(e.target?.result as string);
          if (newPreviews.length === files.length) {
            this.imagePreviews.set([...currentPreviews, ...newPreviews]);
            this.cdr.detectChanges();
          }
        };
        reader.readAsDataURL(file);
      });
      
      input.value = '';
    }
  }

  removeImage(index: number) {
    const images = this.selectedImages();
    images.splice(index, 1);
    this.selectedImages.set([...images]);
    
    const previews = this.imagePreviews();
    previews.splice(index, 1);
    this.imagePreviews.set([...previews]);
  }

  removeExistingImage(index: number) {
    const existing = this.existingImages();
    existing.splice(index, 1);
    this.existingImages.set([...existing]);
  }

  saveMedicine() {
    if (!this.isAdmin()) {
      this.notificationService.error('Acesso Negado', 'Você não tem permissão para salvar medicamentos. Apenas administradores podem realizar esta ação.');
      return;
    }
    console.log('🏥 MedicinesComponent: saveMedicine() called');
    console.log('🏥 MedicinesComponent: Form valid:', this.medicineForm.valid);
    console.log('🏥 MedicinesComponent: Form errors:', this.medicineForm.errors);
    
    if (this.medicineForm.invalid) {
      console.log('🏥 MedicinesComponent: Form is invalid, showing error notification');
      
      // Verifica campos específicos e mostra mensagens específicas
      const nomeControl = this.medicineForm.get('nome');
      const precoControl = this.medicineForm.get('preco');
      const quantidadeControl = this.medicineForm.get('quantidadeEstoque');
      const validadeControl = this.medicineForm.get('validade');
      const categoriaControl = this.medicineForm.get('categoriaId');
      
      if (nomeControl?.hasError('required')) {
        this.notificationService.error('Campo Obrigatório', 'O campo Nome é obrigatório. Por favor, preencha o nome.');
        return;
      }
      
      if (precoControl?.hasError('required')) {
        this.notificationService.error('Campo Obrigatório', 'O campo Preço é obrigatório. Por favor, preencha o preço.');
        return;
      }
      
      if (precoControl?.hasError('min')) {
        this.notificationService.error('Valor Inválido', 'O preço é obrigatório e deve ser maior que zero. Por favor, altere o preço.');
        return;
      }
      
      if (quantidadeControl?.hasError('required')) {
        this.notificationService.error('Campo Obrigatório', 'O campo Quantidade em Estoque é obrigatório. Por favor, preencha a quantidade.');
        return;
      }
      
      if (quantidadeControl?.hasError('min')) {
        this.notificationService.error('Valor Inválido', 'A quantidade em estoque é obrigatória e não pode ser negativa. Por favor, altere a quantidade.');
        return;
      }
      
      if (validadeControl?.hasError('required')) {
        this.notificationService.error('Campo Obrigatório', 'A data de validade é obrigatória. Por favor, preencha a data de validade.');
        return;
      }
      
      if (validadeControl?.hasError('dataFutura')) {
        this.notificationService.error('Data Inválida', 'A data de validade deve ser futura. Por favor, altere a data de validade.');
        return;
      }
      
      if (categoriaControl?.hasError('required')) {
        this.notificationService.error('Campo Obrigatório', 'O campo Categoria é obrigatório. Por favor, selecione uma categoria.');
        return;
      }
      
      // Mensagem genérica caso algum outro erro não capturado
      this.notificationService.error('Formulário Inválido', 'Por favor, preencha todos os campos obrigatórios corretamente.');
      return;
    }
    
    // Valida imagens
    const id = this.currentMedicineId();
    const imagesToUpload = this.selectedImages();
    const existingImgs = this.existingImages();
    const totalImages = imagesToUpload.length + existingImgs.length;
    
    // Ao criar, precisa de pelo menos 1 imagem
    if (!id && imagesToUpload.length === 0) {
      this.notificationService.error('Imagens Obrigatórias', 'É necessário pelo menos 1 imagem e no máximo 3 imagens.');
      return;
    }
    
    // Ao editar, se não há novas imagens e removeu todas as existentes, precisa de pelo menos 1
    if (id && imagesToUpload.length === 0 && existingImgs.length === 0) {
      this.notificationService.error('Imagens Obrigatórias', 'É necessário pelo menos 1 imagem. Adicione novas imagens ou mantenha as existentes.');
      return;
    }
    
    if (imagesToUpload.length > 3 || totalImages > 3) {
      this.notificationService.error('Muitas Imagens', `Você possui ${totalImages} imagem(ns). É permitido no máximo 3 imagens no total.`);
      return;
    }
    
    const formValue = this.medicineForm.value;
    const action = id ? 'update' : 'create';
    console.log('🏥 MedicinesComponent: Action:', action);
    console.log('🏥 MedicinesComponent: Medicine ID:', id);
    console.log('🏥 MedicinesComponent: Form value:', formValue);
    console.log('🏥 MedicinesComponent: Images to upload:', imagesToUpload.length);
    console.log('🏥 MedicinesComponent: Existing images:', existingImgs.length);
    
    const apiCall = id 
      ? this.medicinesService.update(id, formValue as any, imagesToUpload.length > 0 ? imagesToUpload : null)
      : this.medicinesService.create(formValue as any, imagesToUpload);
    
    console.log('🏥 MedicinesComponent: API call initiated:', action === 'create' ? 'CREATE' : 'UPDATE');

    apiCall.subscribe({
      next: (res) => {
        const actionText = action === 'create' ? 'criado' : 'atualizado';
        console.log('🏥 MedicinesComponent: Medicine', actionText, 'successfully');
        console.log('🏥 MedicinesComponent: API response:', res);
        this.notificationService.success(`Medicamento ${actionText}!`, 'O registro foi salvo com sucesso.');
        this.logService.logAction(
          action === 'create' ? 'Criação' : 'Atualização', 
          'Medicamento', 
          `Medicamento '${formValue.nome}' foi ${actionText}.`
        );
        console.log('🏥 MedicinesComponent: Log action recorded');
        console.log('🏥 MedicinesComponent: Reloading medicines list...');
        this.loadMedicines();
        this.isModalOpen.set(false);
        this.selectedImages.set([]);
        this.imagePreviews.set([]);
        this.existingImages.set([]);
        console.log('🏥 MedicinesComponent: Modal closed');
        console.log(`🏥 MedicinesComponent: Medicine ${action} completed successfully`);
      },
      error: (err) => {
        console.error('🏥 MedicinesComponent: ERROR saving medicine:', err);
        console.error('🏥 MedicinesComponent: Error details:', JSON.stringify(err, null, 2));
        console.error('🏥 MedicinesComponent: err.error type:', typeof err?.error);
        console.error('🏥 MedicinesComponent: err.error:', err?.error);
        
        // Extrai a mensagem de erro do backend
        let errorMessage = 'Ocorreu um erro. Tente novamente.';
        
        if (err?.error) {
          // Caso 1: err.error é uma string direta
          if (typeof err.error === 'string') {
            errorMessage = err.error;
            console.log('🏥 MedicinesComponent: Error message (string):', errorMessage);
          }
          // Caso 2: err.error é um objeto - verifica ambos 'error' e 'message'
          else if (typeof err.error === 'object') {
            // Se 'error' é "Erro interno do servidor" e existe 'message', usa 'message'
            if (err.error.error === 'Erro interno do servidor' && err.error.message) {
              const message = err.error.message;
              // Remove prefixos genéricos como "Erro ao processar dados do medicamento: "
              if (message.includes('Erro ao processar dados') && message.includes(': ')) {
                errorMessage = message.split(': ').slice(1).join(': ').trim();
              } else if (message.includes(': ')) {
                errorMessage = message.split(': ').pop() || message;
              } else {
                errorMessage = message;
              }
              console.log('🏥 MedicinesComponent: Error message (message property):', errorMessage);
            }
            // Se tem 'error' e não é genérico, usa 'error'
            else if (err.error.error && err.error.error !== 'Erro interno do servidor') {
              errorMessage = err.error.error;
              console.log('🏥 MedicinesComponent: Error message (error property):', errorMessage);
            }
            // Se só tem 'message', usa 'message'
            else if (err.error.message && !err.error.error) {
              errorMessage = err.error.message;
              console.log('🏥 MedicinesComponent: Error message (message property only):', errorMessage);
            }
          }
        }
        
        console.log('🏥 MedicinesComponent: Final error message:', errorMessage);
        this.notificationService.error('Erro ao Salvar', errorMessage);
        console.error(`🏥 MedicinesComponent: Medicine ${action} failed`);
      }
    });
  }

  deleteMedicine(id: string) {
    if (!this.isAdmin()) {
      this.notificationService.error('Acesso Negado', 'Você não tem permissão para excluir medicamentos. Apenas administradores podem realizar esta ação.');
      return;
    }
    console.log('🏥 MedicinesComponent: deleteMedicine() called');
    console.log('🏥 MedicinesComponent: Medicine ID to delete:', id);
    const medName = this.medicines().find(m => m.id === id)?.nome || `ID ${id}`;
    console.log('🏥 MedicinesComponent: Medicine name:', medName);
    console.log('🏥 MedicinesComponent: Showing confirmation dialog...');
    
    this.notificationService.confirm('Confirmar Exclusão', 'Você tem certeza que deseja excluir este medicamento? Esta ação é irreversível.')
      .then((result) => {
        console.log('🏥 MedicinesComponent: Confirmation dialog result:', result.isConfirmed);
        if (result.isConfirmed) {
          console.log('🏥 MedicinesComponent: User confirmed deletion, calling API...');
          this.apiService.delete('medicamentos', id).subscribe({
            next: () => {
              console.log('🏥 MedicinesComponent: Medicine deleted successfully');
              this.notificationService.success('Excluído!', 'O medicamento foi excluído com sucesso.');
              this.logService.logAction('Exclusão', 'Medicamento', `Medicamento '${medName}' foi excluído.`);
              console.log('🏥 MedicinesComponent: Log action recorded');
              console.log('🏥 MedicinesComponent: Reloading medicines list...');
              this.loadMedicines();
              console.log('🏥 MedicinesComponent: Delete operation completed');
            },
            error: (err) => {
              console.error('🏥 MedicinesComponent: ERROR deleting medicine:', err);
              console.error('🏥 MedicinesComponent: Error details:', JSON.stringify(err, null, 2));
              
              // Extrai a mensagem de erro do backend
              let errorMessage = 'Não foi possível excluir o medicamento.';
              
              if (err?.error) {
                // Caso 1: err.error é uma string direta
                if (typeof err.error === 'string') {
                  errorMessage = err.error;
                }
                // Caso 2: err.error é um objeto
                else if (typeof err.error === 'object') {
                  if (err.error.error && err.error.error !== 'Erro interno do servidor') {
                    errorMessage = err.error.error;
                  } else if (err.error.message) {
                    errorMessage = err.error.message;
                  }
                }
              }
              
              this.notificationService.error('Erro ao Excluir', errorMessage);
              console.error('🏥 MedicinesComponent: Delete operation failed');
            }
          });
        } else {
          console.log('🏥 MedicinesComponent: User cancelled deletion');
        }
      });
  }

  getShortId(id: string): string {
    if (!id) {
      console.log('🏥 MedicinesComponent: getShortId() called with empty ID');
      return '-';
    }
    const shortId = id.substring(0, 8) + '...';
    console.log('🏥 MedicinesComponent: getShortId() - Original:', id, 'Short:', shortId);
    return shortId;
  }

  copyToClipboard(text: string) {
    console.log('🏥 MedicinesComponent: copyToClipboard() called');
    console.log('🏥 MedicinesComponent: Text to copy:', text);
    console.log('🏥 MedicinesComponent: Attempting to copy to clipboard...');
    
    navigator.clipboard.writeText(text).then(() => {
      console.log('🏥 MedicinesComponent: Text copied to clipboard successfully (modern API)');
      this.notificationService.success('Copiado!', 'ID copiado para a área de transferência.');
    }).catch((err) => {
      console.log('🏥 MedicinesComponent: Modern clipboard API failed, trying fallback method');
      console.log('🏥 MedicinesComponent: Fallback error:', err);
      // Fallback para navegadores mais antigos
      const textArea = document.createElement('textarea');
      textArea.value = text;
      textArea.style.position = 'fixed';
      textArea.style.opacity = '0';
      document.body.appendChild(textArea);
      textArea.select();
      try {
        document.execCommand('copy');
        console.log('🏥 MedicinesComponent: Text copied to clipboard successfully (fallback method)');
        this.notificationService.success('Copiado!', 'ID copiado para a área de transferência.');
      } catch (fallbackErr) {
        console.error('🏥 MedicinesComponent: ERROR copying to clipboard (both methods failed):', fallbackErr);
        this.notificationService.error('Erro', 'Não foi possível copiar o ID.');
      }
      document.body.removeChild(textArea);
    });
  }


  inactivateMedicine(med: Medicine) {
    if (!this.isAdmin()) {
      this.notificationService.error('Acesso Negado', 'Você não tem permissão para inativar medicamentos. Apenas administradores podem realizar esta ação.');
      return;
    }
    console.log('🏥 MedicinesComponent: inactivateMedicine() called');
    console.log('🏥 MedicinesComponent: Medicine to inactivate:', med.nome, 'ID:', med.id);
    console.log('🏥 MedicinesComponent: Showing confirmation dialog...');
    
    this.notificationService.confirm('Confirmar Inativação', `Você tem certeza que deseja inativar o medicamento "${med.nome}"?`, 'Sim, inativar!')
      .then((result) => {
        console.log('🏥 MedicinesComponent: Confirmation dialog result:', result.isConfirmed);
        if (result.isConfirmed) {
          console.log('🏥 MedicinesComponent: User confirmed inactivation, calling API...');
          this.medicinesService.updateStatus(med.id, false).subscribe({
            next: () => {
              console.log('🏥 MedicinesComponent: Medicine inactivated successfully');
              this.notificationService.success('Inativado!', `O medicamento ${med.nome} foi inativado.`);
              this.logService.logAction('Inativação', 'Medicamento', `Medicamento '${med.nome}' foi inativado.`);
              console.log('🏥 MedicinesComponent: Log action recorded');
              // Muda para a aba de inativos e recarrega
              console.log('🏥 MedicinesComponent: Switching to inactive tab...');
              this.activeTab.set('inactive');
              console.log('🏥 MedicinesComponent: Reloading medicines list...');
              this.loadMedicines();
              console.log('🏥 MedicinesComponent: Inactivation operation completed');
            },
            error: (err) => {
              console.error('🏥 MedicinesComponent: ERROR inactivating medicine:', err);
              console.error('🏥 MedicinesComponent: Error details:', JSON.stringify(err, null, 2));
              this.notificationService.error('Erro', 'Não foi possível inativar o medicamento.');
              console.error('🏥 MedicinesComponent: Inactivation operation failed');
            }
          });
        } else {
          console.log('🏥 MedicinesComponent: User cancelled inactivation');
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
      med.descricao?.toLowerCase().includes(term) ||
      med.categoria?.nome.toLowerCase().includes(term) ||
      med.id.toLowerCase().includes(term)
    );
    this.filteredMedicines.set(filtered);
  }

  onSearchChange(term: string) {
    this.searchTerm.set(term);
    this.applySearchFilter();
  }

  reactivateMedicine(med: Medicine) {
    if (!this.isAdmin()) {
      this.notificationService.error('Acesso Negado', 'Você não tem permissão para reativar medicamentos. Apenas administradores podem realizar esta ação.');
      return;
    }
    console.log('🏥 MedicinesComponent: reactivateMedicine() called');
    console.log('🏥 MedicinesComponent: Medicine to reactivate:', med.nome, 'ID:', med.id);
    console.log('🏥 MedicinesComponent: Calling API to reactivate...');
    
    this.medicinesService.updateStatus(med.id, true).subscribe({
      next: () => {
        console.log('🏥 MedicinesComponent: Medicine reactivated successfully');
        this.notificationService.success('Reativado!', `O medicamento ${med.nome} foi reativado.`);
        this.logService.logAction('Reativação', 'Medicamento', `Medicamento '${med.nome}' foi reativado.`);
        console.log('🏥 MedicinesComponent: Log action recorded');
        // Muda para a aba de ativos e recarrega
        console.log('🏥 MedicinesComponent: Switching to active tab...');
        this.activeTab.set('active');
        console.log('🏥 MedicinesComponent: Reloading medicines list...');
        this.loadMedicines();
        console.log('🏥 MedicinesComponent: Reactivation operation completed');
      },
      error: (err) => {
        console.error('🏥 MedicinesComponent: ERROR reactivating medicine:', err);
        console.error('🏥 MedicinesComponent: Error details:', JSON.stringify(err, null, 2));
        this.notificationService.error('Erro', 'Não foi possível reativar o medicamento.');
        console.error('🏥 MedicinesComponent: Reactivation operation failed');
      }
    });
  }

  isAdmin(): boolean {
    const user = this.authService.currentUser();
    return user?.role === 'ADMIN';
  }

  getMinDate(): string {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split('T')[0];
  }
}