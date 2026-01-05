import { ChangeDetectionStrategy, Component, inject, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { ApiService } from '../../services/api.service';
import { MedicinesService } from '../../services/medicines.service';
import { NotificationService } from '../../services/notification.service';
import { Medicine, Category } from '../../models/types';
import { LogService } from '../../services/log.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-category-medicines',
  templateUrl: './category-medicines.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule],
})
export class CategoryMedicinesComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private apiService = inject(ApiService);
  private medicinesService = inject(MedicinesService);
  private notificationService = inject(NotificationService);
  private logService = inject(LogService);
  private authService = inject(AuthService);
  private fb: FormBuilder = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  category = signal<Category | null>(null);
  medicines = signal<Medicine[]>([]);
  filteredMedicines = signal<Medicine[]>([]);
  searchTerm = signal<string>('');
  isLoading = signal(true);
  currentImageIndex = signal<Map<string, number>>(new Map());

  searchControl = this.fb.control<string>('');

  constructor() {
    // Carrega a categoria e os medicamentos
    const categoryId = this.route.snapshot.paramMap.get('id');
    if (categoryId) {
      this.loadCategory(categoryId);
      this.loadMedicines(categoryId);
    }

    // Configura busca com debounce
    this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe((term: string | null) => {
      this.searchTerm.set(term || '');
      this.applySearchFilter();
    });
  }

  loadCategory(categoryId: string) {
    this.apiService.getCategories().subscribe({
      next: (categories) => {
        const category = categories.find(c => c.id === categoryId);
        if (category) {
          this.category.set(category);
        } else {
          this.notificationService.error('Erro', 'Categoria não encontrada.');
          this.router.navigate(['/categorias']);
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.notificationService.error('Erro', 'Não foi possível carregar a categoria.');
        this.router.navigate(['/categorias']);
      }
    });
  }

  loadMedicines(categoryId: string) {
    this.isLoading.set(true);
    this.medicinesService.getAll().subscribe({
      next: (medicines) => {
        // Filtra medicamentos da categoria
        const categoryMedicines = medicines.filter(m => m.categoria?.id === categoryId);
        const sorted = categoryMedicines.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' }));
        this.medicines.set(sorted);
        this.applySearchFilter();
        this.isLoading.set(false);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao carregar medicamentos:', err);
        this.notificationService.error('Erro', 'Não foi possível carregar os medicamentos.');
        this.isLoading.set(false);
        this.cdr.detectChanges();
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
      med.preco.toString().includes(term) ||
      med.quantidadeEstoque.toString().includes(term)
    );
    this.filteredMedicines.set(filtered);
  }

  toggleMedicineStatus(medicine: Medicine) {
    if (!this.isAdmin()) {
      this.notificationService.error('Acesso Negado', 'Você não tem permissão para ativar/inativar medicamentos. Apenas administradores podem realizar esta ação.');
      return;
    }
    const newStatus = !medicine.ativo;
    const action = newStatus ? 'ativar' : 'inativar';
    const confirmButtonText = newStatus ? 'Sim, Ativar!' : 'Sim, Inativar!';
    
    this.notificationService.confirm(
      `Confirmar ${newStatus ? 'Ativação' : 'Inativação'}`,
      `Deseja ${action} o medicamento "${medicine.nome}"?`,
      confirmButtonText
    ).then((result) => {
      if (result.isConfirmed) {
        this.medicinesService.updateStatus(medicine.id, newStatus).subscribe({
          next: (updated) => {
            this.notificationService.success(
              'Status Atualizado!',
              `Medicamento "${medicine.nome}" foi ${newStatus ? 'ativado' : 'inativado'} com sucesso.`
            );
            this.logService.logAction(
              newStatus ? 'Reativação' : 'Atualização',
              'Medicamento',
              `Medicamento '${medicine.nome}' foi ${newStatus ? 'ativado' : 'inativado'}.`
            );
            // Atualiza o medicamento na lista
            this.medicines.update(list => 
              list.map(m => m.id === medicine.id ? updated : m)
            );
            this.applySearchFilter();
            this.cdr.detectChanges();
          },
          error: (err) => {
            const errorMessage = err?.error?.error || 'Não foi possível atualizar o status.';
            this.notificationService.error('Erro', errorMessage);
          }
        });
      }
    });
  }

  goBack() {
    this.router.navigate(['/categorias']);
  }

  getImageUrl(imagePath: string): string {
    if (!imagePath) return '';
    if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
      return imagePath;
    }
    return `http://localhost:8081${imagePath}`;
  }

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

  getCurrentImageIndex(medicineId: string): number {
    return this.currentImageIndex().get(medicineId) || 0;
  }

  setCurrentImageIndex(medicineId: string, index: number) {
    const newMap = new Map(this.currentImageIndex());
    newMap.set(medicineId, index);
    this.currentImageIndex.set(newMap);
  }

  nextImage(medicineId: string, totalImages: number) {
    const current = this.getCurrentImageIndex(medicineId);
    const next = (current + 1) % totalImages;
    this.setCurrentImageIndex(medicineId, next);
  }

  previousImage(medicineId: string, totalImages: number) {
    const current = this.getCurrentImageIndex(medicineId);
    const prev = (current - 1 + totalImages) % totalImages;
    this.setCurrentImageIndex(medicineId, prev);
  }

  isAdmin(): boolean {
    const user = this.authService.currentUser();
    return user?.role === 'ADMIN';
  }
}

