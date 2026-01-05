import { ChangeDetectionStrategy, Component, inject, signal, computed, afterNextRender, ChangeDetectorRef } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { UsersService } from '../../services/users.service';
import { NotificationService } from '../../services/notification.service';
import { environment } from '../../environments/environment';
import { CryptoService } from '../../services/crypto.service';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterOutlet],
})
export class LayoutComponent {
  authService = inject(AuthService);
  router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private usersService = inject(UsersService);
  private notificationService = inject(NotificationService);
  private cryptoService = inject(CryptoService);

  currentUser = this.authService.currentUser;
  
  isMobileMenuOpen = signal(false);

  constructor() {
    afterNextRender(() => {
      // Garante que o currentUser seja detectado
      this.cdr.markForCheck();
    });
  }

  navLinks = computed(() => {
    const user = this.currentUser();
    const links = [
      { path: '/inicio', label: 'Início', icon: 'home' },
      { path: '/medicamentos', label: 'Medicamentos', icon: 'pill' },
      { path: '/categorias', label: 'Categorias', icon: 'tag' },
      { path: '/clientes', label: 'Clientes', icon: 'users' },
      { path: '/estoque', label: 'Estoque', icon: 'archive' },
    ];

    // Adiciona menu de usuários apenas para ADMIN
    if (user?.role === 'ADMIN') {
      links.push({ path: '/usuarios', label: 'Usuários', icon: 'user-circle' });
    }

    // Vendas vem depois de Usuários
    links.push({ path: '/vendas', label: 'Vendas', icon: 'cart' });

    // Adiciona menu de logs apenas para ADMIN (depois de Vendas)
    if (user?.role === 'ADMIN') {
      links.push({ path: '/logs', label: 'Logs', icon: 'clipboard' });
    }

    return links;
  });

  isAdmin(): boolean {
    const user = this.currentUser();
    return user?.role === 'ADMIN';
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen.update(value => !value);
  }

  navigateWithReload(path: string) {
    // Fecha o menu mobile se estiver aberto
    this.isMobileMenuOpen.set(false);
    // Navega para a rota
    this.router.navigate([path]).then(() => {
      // Recarrega a página após a navegação
      window.location.reload();
    });
  }

  isActiveRoute(path: string): boolean {
    return this.router.url === path || this.router.url.startsWith(path + '/');
  }

  logout() {
    this.authService.logout();
  }

  onAvatarClick() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/jpeg,image/jpg,image/png,image/webp';
    input.onchange = (event: Event) => {
      const target = event.target as HTMLInputElement;
      if (target.files && target.files.length > 0) {
        const file = target.files[0];
        this.uploadAvatar(file);
      }
    };
    input.click();
  }

  uploadAvatar(file: File) {
    const user = this.currentUser();
    if (!user) {
      return;
    }

    // Valida tipo de arquivo
    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
    if (!validTypes.includes(file.type)) {
      this.notificationService.error('Formato Inválido', 'Por favor, selecione uma imagem JPG, PNG ou WebP.');
      return;
    }

    // Valida tamanho (5MB)
    if (file.size > 5 * 1024 * 1024) {
      this.notificationService.error('Arquivo muito grande', 'O arquivo deve ter no máximo 5MB.');
      return;
    }

    this.usersService.uploadAvatar(user.id, file).subscribe({
      next: (updatedUser) => {
        // Atualiza o usuário no AuthService
        this.authService.currentUser.set(updatedUser);
        // Atualiza no localStorage também
        const encryptedUser = this.cryptoService.encryptObject(updatedUser);
        localStorage.setItem('currentUser_enc', encryptedUser);
        this.notificationService.success('Avatar atualizado!', 'Seu avatar foi atualizado com sucesso.');
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error uploading avatar:', err);
        this.notificationService.error('Erro ao atualizar avatar', err.error?.error || 'Ocorreu um erro. Tente novamente.');
      }
    });
  }

  getAvatarUrl(): string | null {
    const user = this.currentUser();
    if (!user || !user.avatarUrl) {
      return null;
    }
    // Se a URL já contém http, retorna como está
    if (user.avatarUrl.startsWith('http')) {
      return user.avatarUrl;
    }
    // Remove /api do environment.apiUrl e adiciona a URL do avatar
    const baseUrl = environment.apiUrl.replace('/api', '');
    return `${baseUrl}${user.avatarUrl}`;
  }
}