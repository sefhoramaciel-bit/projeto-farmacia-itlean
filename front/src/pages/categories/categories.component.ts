import { ChangeDetectionStrategy, Component, inject, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { Category } from '../../models/types';
import { ModalComponent } from '../../components/modal/modal.component';
import { LogService } from '../../services/log.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-categories',
  templateUrl: './categories.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule, ModalComponent],
})
export class CategoriesComponent {
  private apiService = inject(ApiService);
  private notificationService = inject(NotificationService);
  private logService = inject(LogService);
  private router = inject(Router);
  private authService = inject(AuthService);
  // Fix: Explicitly type FormBuilder to resolve type inference issue with inject().
  private fb: FormBuilder = inject(FormBuilder);

  categories = signal<Category[]>([]);
  filteredCategories = signal<Category[]>([]);
  searchTerm = signal<string>('');
  isLoading = signal(true);
  
  isModalOpen = signal(false);
  modalTitle = signal('Adicionar Categoria');
  currentCategoryId = signal<string | null>(null);

  categoryForm = this.fb.group({
    nome: ['', [Validators.required]],
    descricao: [''], 
  });

  private cdr = inject(ChangeDetectorRef);

  constructor() {
    console.log('📁 CategoriesComponent: Constructor called');
    this.loadCategories();
  }

  loadCategories() {
    console.log('📁 CategoriesComponent: loadCategories() called');
    this.isLoading.set(true);
    console.log('📁 CategoriesComponent: Calling apiService.getCategories()...');
    this.apiService.getCategories().subscribe({
      next: (data) => {
        console.log('📁 CategoriesComponent: getCategories response received:', data);
        // Ordena alfabeticamente por nome
        const sortedData = data.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' }));
        this.categories.set(sortedData);
        this.applySearchFilter();
        this.isLoading.set(false);
        console.log('📁 CategoriesComponent: Calling detectChanges()...');
        this.cdr.detectChanges();
        console.log('📁 CategoriesComponent: detectChanges() completed');
      },
      error: (err) => {
        console.error('📁 CategoriesComponent: ERROR loading categories:', err);
        console.error('📁 CategoriesComponent: Error details:', JSON.stringify(err, null, 2));
        this.isLoading.set(false);
        this.cdr.detectChanges();
      }
    });
  }

  openCreateModal() {
    if (!this.isAdmin()) {
      this.notificationService.error('Acesso Negado', 'Você não tem permissão para criar categorias. Apenas administradores podem realizar esta ação.');
      return;
    }
    this.currentCategoryId.set(null);
    this.modalTitle.set('Adicionar Categoria');
    this.categoryForm.reset();
    this.isModalOpen.set(true);
  }

  openEditModal(cat: Category) {
    if (!this.isAdmin()) {
      this.notificationService.error('Acesso Negado', 'Você não tem permissão para editar categorias. Apenas administradores podem realizar esta ação.');
      return;
    }
    this.currentCategoryId.set(cat.id);
    this.modalTitle.set('Editar Categoria');
    this.categoryForm.patchValue(cat);
    this.isModalOpen.set(true);
  }

  saveCategory() {
    if (!this.isAdmin()) {
      this.notificationService.error('Acesso Negado', 'Você não tem permissão para salvar categorias. Apenas administradores podem realizar esta ação.');
      return;
    }
    
    if (this.categoryForm.invalid) {
      // Verifica campos específicos e mostra mensagens específicas
      const nomeControl = this.categoryForm.get('nome');
      
      if (nomeControl?.hasError('required')) {
        this.notificationService.error('Campo Obrigatório', 'O campo Nome é obrigatório. Por favor, preencha o nome.');
        return;
      }
      
      // Mensagem genérica caso algum outro erro não capturado
      this.notificationService.error('Formulário Inválido', 'Por favor, preencha todos os campos obrigatórios corretamente.');
      return;
    }
    
    const formValue = this.categoryForm.value;
    const id = this.currentCategoryId();
    const action = id ? 'update' : 'create';
    const apiCall = id 
      ? this.apiService.update('categorias', id, formValue)
      : this.apiService.create('categorias', formValue);

    apiCall.subscribe({
      next: () => {
        const actionText = action === 'create' ? 'criada' : 'atualizada';
        this.notificationService.success(`Categoria ${actionText}!`, 'O registro foi salvo com sucesso.');
        this.logService.logAction(
          action === 'create' ? 'Criação' : 'Atualização',
          'Categoria',
          `Categoria '${formValue.nome}' foi ${actionText}.`
        );
        this.loadCategories();
        this.isModalOpen.set(false);
      },
      error: (err) => {
        console.error('📁 CategoriesComponent: ERROR saving category:', err);
        console.error('📁 CategoriesComponent: Error details:', JSON.stringify(err, null, 2));
        
        // Extrai a mensagem de erro do backend
        let errorMessage = 'Ocorreu um erro. Tente novamente.';
        
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
        
        this.notificationService.error('Erro ao Salvar', errorMessage);
      }
    });
  }

  applySearchFilter() {
    const term = this.searchTerm().toLowerCase().trim();
    const allCategories = this.categories();
    
    if (!term) {
      this.filteredCategories.set(allCategories);
      return;
    }
    
    const filtered = allCategories.filter(cat => 
      cat.nome.toLowerCase().includes(term) ||
      cat.descricao?.toLowerCase().includes(term) ||
      cat.id.toLowerCase().includes(term)
    );
    this.filteredCategories.set(filtered);
  }

  onSearchChange(term: string) {
    this.searchTerm.set(term);
    this.applySearchFilter();
  }

  deleteCategory(id: string) {
    if (!this.isAdmin()) {
      this.notificationService.error('Acesso Negado', 'Você não tem permissão para excluir categorias. Apenas administradores podem realizar esta ação.');
      return;
    }
    this.notificationService.confirm('Confirmar Exclusão', 'Você tem certeza? Excluir uma categoria pode afetar medicamentos associados.')
      .then((result) => {
        if (result.isConfirmed) {
          const catName = this.categories().find(c => c.id === id)?.nome || `ID ${id}`;
          this.apiService.delete('categorias', id).subscribe({
            next: () => {
              this.notificationService.success('Excluído!', 'A categoria foi excluída com sucesso.');
              this.logService.logAction('Exclusão', 'Categoria', `Categoria '${catName}' foi excluída.`);
              this.loadCategories();
            },
            error: () => this.notificationService.error('Erro ao Excluir', 'Não foi possível excluir. Verifique se não há medicamentos nesta categoria.')
          });
        }
      });
  }

  viewCategoryDetails(categoryId: string) {
    this.router.navigate(['/categorias', categoryId, 'medicamentos']);
  }

  isAdmin(): boolean {
    const user = this.authService.currentUser();
    return user?.role === 'ADMIN';
  }
}