import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  console.log('🔐 AuthInterceptor: Intercepting request:', req.method, req.url);
  console.log('🔐 AuthInterceptor: Token exists:', !!token);

  if (token && !req.url.includes('/auth/login')) {
    console.log('🔐 AuthInterceptor: Adding Authorization header');
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(cloned);
  }

  console.log('🔐 AuthInterceptor: No token, proceeding without auth');
  return next(req);
};


