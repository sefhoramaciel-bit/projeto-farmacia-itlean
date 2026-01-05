import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../environments/environment';
import { User, LoginRequest, LoginResponse } from '../models/types';
import { CryptoService } from './crypto.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private cryptoService = inject(CryptoService);
  
  currentUser = signal<User | null>(null);
  private readonly TOKEN_KEY = 'jwt_token';
  private readonly USER_KEY = 'currentUser_enc';

  constructor() {
    this.loadStoredUser();
  }

  private loadStoredUser(): void {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const encryptedUser = localStorage.getItem(this.USER_KEY);
    const oldUserKey = 'currentUser'; // Chave antiga sem criptografia
    
    if (token && encryptedUser) {
      try {
        // Descriptografa o objeto do usuário
        const user = this.cryptoService.decryptObject<User>(encryptedUser);
        if (user) {
          this.currentUser.set(user);
        } else {
          // Se falhar ao descriptografar, limpa o storage
          this.clearStorage();
        }
      } catch (e) {
        console.error('Erro ao carregar usuário do storage:', e);
        this.clearStorage();
      }
    } else if (token) {
      // Migração: Se existe token mas não usuário criptografado, verifica se existe versão antiga
      const oldUserStr = localStorage.getItem(oldUserKey);
      if (oldUserStr) {
        try {
          const user = JSON.parse(oldUserStr);
          // Migra para formato criptografado
          const encryptedUser = this.cryptoService.encryptObject(user);
          localStorage.setItem(this.USER_KEY, encryptedUser);
          localStorage.removeItem(oldUserKey); // Remove a versão antiga
          this.currentUser.set(user);
        } catch (e) {
          console.error('Erro ao migrar usuário do storage:', e);
          this.clearStorage();
        }
      }
    }
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    // O token JWT pode permanecer sem criptografia, pois ele é projetado para ser enviado
    // Mas você pode criptografá-lo também se desejar maior segurança
    return localStorage.getItem(this.TOKEN_KEY);
  }

  login(email: string, password: string): Observable<LoginResponse> {
    const request: LoginRequest = { email, password };
    
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, request).pipe(
      tap((response: LoginResponse) => {
        // Armazena o token (pode ser criptografado também, mas JWT é projetado para ser enviado)
        localStorage.setItem(this.TOKEN_KEY, response.token);
        
        // Criptografa e armazena o objeto do usuário
        const encryptedUser = this.cryptoService.encryptObject(response.usuario);
        localStorage.setItem(this.USER_KEY, encryptedUser);
        
        this.currentUser.set(response.usuario);
      })
    );
  }

  logout(): void {
    this.clearStorage();
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  private clearStorage(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  }
}
