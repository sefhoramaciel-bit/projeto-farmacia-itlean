import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { NotificationService } from './notification.service';

export const AdminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const notificationService = inject(NotificationService);

  const currentUser = authService.currentUser();

  if (!currentUser) {
    notificationService.error('Acesso Negado', 'Você precisa estar autenticado para acessar esta página.');
    router.navigate(['/login']);
    return false;
  }

  if (currentUser.role !== 'ADMIN') {
    notificationService.error('Acesso Negado', 'Você não tem permissão para acessar esta página. Apenas administradores podem acessar.');
    router.navigate(['/inicio']);
    return false;
  }

  return true;
};

