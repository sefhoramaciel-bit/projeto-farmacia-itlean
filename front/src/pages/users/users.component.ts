import { ChangeDetectionStrategy, Component, inject, signal, afterNextRender, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UsersService, UserRequest } from '../../services/users.service';
import { NotificationService } from '../../services/notification.service';
import { User } from '../../models/types';
import { ModalComponent } from '../../components/modal/modal.component';
import { LogService } from '../../services/log.service';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule, ModalComponent],
})
export class UsersComponent {
  private usersService = inject(UsersService);
  private notificationService = inject(NotificationService);
  private logService = inject(LogService);
  private fb: FormBuilder = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  users = signal<User[]>([]);
  filteredUsers = signal<User[]>([]);
  searchTerm = signal<string>('');
  isLoading = signal(true);
  
  isModalOpen = signal(false);
  modalTitle = signal('Adicionar Usuário');
  currentUserId = signal<string | null>(null);
  
  selectedAvatar = signal<File | null>(null);
  avatarPreview = signal<string | null>(null);
  currentAvatarUrl = signal<string | null>(null);

  userForm = this.fb.group({
    nome: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    role: ['', [Validators.required]], // Removido valor padrão para forçar seleção
  });

  constructor() {
    afterNextRender(() => {
      this.loadUsers();
    });
  }

  loadUsers() {
    this.isLoading.set(true);
    this.usersService.getAll().subscribe({
      next: (data) => {
        // Ordena alfabeticamente por nome
        const sortedData = data.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' }));
        this.users.set(sortedData);
        this.applySearchFilter();
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading users:', err);
        this.notificationService.error('Erro', 'Não foi possível carregar os usuários.');
        this.isLoading.set(false);
      }
    });
  }

  openCreateModal() {
    this.currentUserId.set(null);
    this.modalTitle.set('Adicionar Usuário');
    this.userForm.reset({ role: '' }); // Reseta sem valor padrão para forçar seleção
    this.userForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
    this.userForm.get('password')?.updateValueAndValidity();
    this.userForm.get('role')?.setValidators([Validators.required]);
    this.userForm.get('role')?.updateValueAndValidity();
    this.selectedAvatar.set(null);
    this.avatarPreview.set(null);
    this.currentAvatarUrl.set(null);
    this.isModalOpen.set(true);
  }

  openEditModal(user: User) {
    this.currentUserId.set(user.id);
    this.modalTitle.set('Editar Usuário');
    this.userForm.patchValue({
      nome: user.nome,
      email: user.email,
      role: user.role,
      password: '', // Senha não é obrigatória na edição
    });
    // Na edição, senha é opcional, mas se informada deve ter no mínimo 6 caracteres
    this.userForm.get('password')?.setValidators([Validators.minLength(6)]);
    this.userForm.get('password')?.updateValueAndValidity();
    // Garante que role seja obrigatório também na edição
    this.userForm.get('role')?.setValidators([Validators.required]);
    this.userForm.get('role')?.updateValueAndValidity();
    
    // Carrega o avatar atual do usuário (se existir)
    this.selectedAvatar.set(null);
    this.avatarPreview.set(null);
    if (user.avatarUrl) {
      this.currentAvatarUrl.set(this.getAvatarUrl(user.avatarUrl));
    } else {
      this.currentAvatarUrl.set(null);
    }
    
    this.isModalOpen.set(true);
  }

  onAvatarSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      
      // Valida tipo de arquivo
      const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
      if (!validTypes.includes(file.type)) {
        this.notificationService.error('Formato Inválido', 'Por favor, selecione uma imagem JPG, PNG ou WebP.');
        input.value = '';
        return;
      }
      
      // Valida tamanho (5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.notificationService.error('Arquivo muito grande', 'O arquivo deve ter no máximo 5MB.');
        input.value = '';
        return;
      }
      
      this.selectedAvatar.set(file);
      
      // Cria preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.avatarPreview.set(e.target?.result as string);
        this.cdr.detectChanges();
      };
      reader.readAsDataURL(file);
    }
  }

  removeAvatar() {
    this.selectedAvatar.set(null);
    this.avatarPreview.set(null);
  }

  saveUser() {
    if (this.userForm.invalid) {
      // Verifica campos específicos e mostra mensagens específicas
      const nomeControl = this.userForm.get('nome');
      const emailControl = this.userForm.get('email');
      const passwordControl = this.userForm.get('password');
      const roleControl = this.userForm.get('role');
      
      if (nomeControl?.hasError('required')) {
        this.notificationService.error('Campo Obrigatório', 'O campo Nome é obrigatório. Por favor, preencha o nome.');
        return;
      }
      
      if (emailControl?.hasError('required')) {
        this.notificationService.error('Campo Obrigatório', 'O campo Email é obrigatório. Por favor, preencha o email.');
        return;
      }
      
      if (emailControl?.hasError('email')) {
        this.notificationService.error('Email Inválido', 'O email informado é inválido. Por favor, informe um email válido.');
        return;
      }
      
      if (passwordControl?.hasError('required')) {
        this.notificationService.error('Campo Obrigatório', 'O campo Senha é obrigatório. Por favor, preencha a senha.');
        return;
      }
      
      if (passwordControl?.hasError('minlength')) {
        this.notificationService.error('Senha Inválida', 'O campo Senha deve ter no mínimo 6 caracteres. Por favor, alterar.');
        return;
      }
      
      if (roleControl?.hasError('required')) {
        this.notificationService.error('Campo Obrigatório', 'O campo Perfil é obrigatório. Por favor, selecione um perfil.');
        return;
      }
      
      // Mensagem genérica caso algum outro erro não capturado
      this.notificationService.error('Formulário Inválido', 'Por favor, preencha todos os campos corretamente.');
      return;
    }
    
    const formValue = this.userForm.value;
    const id = this.currentUserId();
    const action = id ? 'update' : 'create';
    
    const userData: UserRequest = {
      nome: formValue.nome!,
      email: formValue.email!,
      role: formValue.role as 'ADMIN' | 'VENDEDOR',
      password: formValue.password || undefined,
    };

    const avatarFile = this.selectedAvatar();
    
    // Se for criação e tiver avatar, usa createWithAvatar
    if (id) {
      // Edição: primeiro atualiza os dados do usuário, depois atualiza o avatar se houver
      this.usersService.update(id, userData).subscribe({
        next: () => {
          if (avatarFile) {
            // Se há novo avatar, faz upload
            this.usersService.uploadAvatar(id, avatarFile).subscribe({
              next: () => {
                this.notificationService.success('Usuário atualizado!', 'O registro foi salvo com sucesso.');
                this.logService.logAction('Atualização', 'Usuário', `Usuário '${formValue.nome}' foi atualizado.`);
                this.loadUsers();
                this.isModalOpen.set(false);
                this.selectedAvatar.set(null);
                this.avatarPreview.set(null);
                this.currentAvatarUrl.set(null);
              },
              error: (err) => {
                console.error('Avatar upload error:', err);
                // Mesmo com erro no avatar, o usuário foi atualizado
                this.notificationService.success('Usuário atualizado!', 'O registro foi salvo, mas houve um erro ao atualizar o avatar.');
                this.loadUsers();
                this.isModalOpen.set(false);
                this.selectedAvatar.set(null);
                this.avatarPreview.set(null);
                this.currentAvatarUrl.set(null);
              }
            });
          } else {
            // Sem novo avatar, apenas atualiza os dados
            this.notificationService.success('Usuário atualizado!', 'O registro foi salvo com sucesso.');
            this.logService.logAction('Atualização', 'Usuário', `Usuário '${formValue.nome}' foi atualizado.`);
            this.loadUsers();
            this.isModalOpen.set(false);
            this.selectedAvatar.set(null);
            this.avatarPreview.set(null);
            this.currentAvatarUrl.set(null);
          }
        },
        error: (err) => {
          console.error('User update error:', err);
          const errorMessage = err?.error?.error || 'Ocorreu um erro. Tente novamente.';
          this.notificationService.error('Erro ao Salvar', errorMessage);
        }
      });
    } else {
      // Criação
      const apiCall = avatarFile 
        ? this.usersService.createWithAvatar(userData, avatarFile)
        : this.usersService.create(userData);

      apiCall.subscribe({
        next: () => {
          this.notificationService.success('Usuário criado!', 'O registro foi salvo com sucesso.');
          this.logService.logAction('Criação', 'Usuário', `Usuário '${formValue.nome}' foi criado.`);
          this.loadUsers();
          this.isModalOpen.set(false);
          this.selectedAvatar.set(null);
          this.avatarPreview.set(null);
          this.currentAvatarUrl.set(null);
        },
        error: (err) => {
          console.error('User create error:', err);
          const errorMessage = err?.error?.error || 'Ocorreu um erro. Tente novamente.';
          this.notificationService.error('Erro ao Salvar', errorMessage);
        }
      });
    }
  }

  deleteUser(id: string) {
    this.notificationService.confirm('Confirmar Exclusão', 'Você tem certeza que deseja excluir este usuário? Esta ação é irreversível.')
      .then((result) => {
        if (result.isConfirmed) {
          const userName = this.users().find(u => u.id === id)?.nome || `ID ${id}`;
          this.usersService.delete(id).subscribe({
            next: () => {
              this.notificationService.success('Excluído!', 'O usuário foi excluído com sucesso.');
              this.logService.logAction('Exclusão', 'Usuário', `Usuário '${userName}' foi excluído.`);
              this.loadUsers();
            },
            error: (err) => {
              console.error('Delete user error:', err);
              this.notificationService.error('Erro ao Excluir', err.error?.error || 'Não foi possível excluir o usuário.');
            }
          });
        }
      });
  }

  getRoleLabel(role: string): string {
    return role === 'ADMIN' ? 'Administrador' : 'Vendedor';
  }

  applySearchFilter() {
    const term = this.searchTerm().toLowerCase().trim();
    const allUsers = this.users();
    
    if (!term) {
      this.filteredUsers.set(allUsers);
      return;
    }
    
    const filtered = allUsers.filter(user => 
      user.nome.toLowerCase().includes(term) ||
      user.email.toLowerCase().includes(term) ||
      user.role.toLowerCase().includes(term) ||
      user.id.toLowerCase().includes(term)
    );
    this.filteredUsers.set(filtered);
  }

  onSearchChange(term: string) {
    this.searchTerm.set(term);
    this.applySearchFilter();
  }

  getRoleBadgeClass(role: string): string {
    return role === 'ADMIN' 
      ? 'px-2 py-1 text-xs font-semibold rounded-full bg-purple-100 text-purple-800'
      : 'px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800';
  }

  getAvatarUrl(avatarUrl: string): string {
    if (!avatarUrl) return '';
    if (avatarUrl.startsWith('http://') || avatarUrl.startsWith('https://')) {
      return avatarUrl;
    }
    const baseUrl = environment.apiUrl.replace('/api', '');
    return `${baseUrl}${avatarUrl}`;
  }
}

