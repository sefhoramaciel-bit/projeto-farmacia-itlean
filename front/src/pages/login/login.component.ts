import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule],
})
export class LoginComponent {
  // Fix: Explicitly type FormBuilder to resolve type inference issue with inject().
  private fb: FormBuilder = inject(FormBuilder);
  private router = inject(Router);
  private authService = inject(AuthService);
  private notificationService = inject(NotificationService);

  isLoading = signal(false);

  loginForm = this.fb.group({
    email: ['admin@farmacia.com', [Validators.required, Validators.email]],
    password: ['admin123', [Validators.required]],
  });

  get email() { return this.loginForm.get('email'); }
  get password() { return this.loginForm.get('password'); }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      this.notificationService.error('Formulário Inválido', 'Por favor, preencha todos os campos corretamente.');
      return;
    }

    this.isLoading.set(true);
    const { email, password } = this.loginForm.value;

    this.authService.login(email!, password!).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        if (response?.usuario) {
          this.notificationService.success('Login bem-sucedido!', `Bem-vindo, ${response.usuario.nome}!`);
          this.router.navigate(['/inicio']);
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 401 || err.status === 403) {
          this.notificationService.error('Erro de Autenticação', 'Login ou Senha Não Identificados!');
        } else {
          this.notificationService.error('Erro no Servidor', 'Não foi possível conectar ao servidor. Tente novamente mais tarde.');
        }
        console.error('Login error:', err);
      }
    });
  }
}
