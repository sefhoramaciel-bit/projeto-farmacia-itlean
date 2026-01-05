import { ChangeDetectionStrategy, Component, inject, signal, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AlertsService } from '../../services/alerts.service';
import { Alert } from '../../models/types';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { Subscription, filter } from 'rxjs';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterModule],
})
export class HomeComponent implements OnInit, OnDestroy {
  private apiService = inject(ApiService);
  private alertsService = inject(AlertsService);
  private router = inject(Router);
  private notificationService = inject(NotificationService);
  private cdr = inject(ChangeDetectorRef);
  authService = inject(AuthService);

  lowStockAlerts = signal<Alert[]>([]);
  expiringSoonAlerts = signal<Alert[]>([]);
  expiredAlerts = signal<Alert[]>([]);
  isLoading = signal(true);
  
  private routerSubscription?: Subscription;

  ngOnInit() {
    // Observa navegação para recarregar alertas quando voltar para esta página
    this.routerSubscription = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      // Se a navegação foi para a página inicial (/inicio ou /), recarrega os alertas
      if (event.url === '/inicio' || event.url === '/' || event.urlAfterRedirects === '/inicio' || event.urlAfterRedirects === '/') {
        console.log('🏠 HomeComponent: Navigation detected to home page, reloading alerts');
        this.gerarEcarregarAlertas();
      }
    });
    
    // Carrega alertas na inicialização
    this.gerarEcarregarAlertas();
  }

  ngOnDestroy() {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
  }

  private gerarEcarregarAlertas() {
    // Gera alertas primeiro, depois carrega
    this.apiService.gerarAlertas().subscribe({
      next: () => {
        console.log('🏠 HomeComponent: Alertas gerados com sucesso');
        this.loadAlerts();
      },
      error: (err) => {
        console.error('🏠 HomeComponent: Error generating alerts:', err);
        console.error('🏠 HomeComponent: Error details:', JSON.stringify(err, null, 2));
        // Mesmo com erro, tenta carregar os alertas existentes
        this.loadAlerts();
      }
    });
  }

  loadAlerts(): void {
    console.log('🏠 HomeComponent: loadAlerts() called');
    this.isLoading.set(true);
    let loadedCount = 0;
    const totalLoads = 3;

    const checkComplete = () => {
      loadedCount++;
      console.log(`🏠 HomeComponent: checkComplete - ${loadedCount}/${totalLoads} alerts loaded`);
      if (loadedCount === totalLoads) {
        this.isLoading.set(false);
        console.log('🏠 HomeComponent: All alerts loaded, isLoading set to false');
        this.cdr.detectChanges();
      }
    };

    console.log('🏠 HomeComponent: Loading low stock alerts...');
    this.apiService.getLowStockAlerts().subscribe({
      next: (data) => {
        console.log('🏠 HomeComponent: Low stock alerts loaded:', data);
        console.log('🏠 HomeComponent: Low stock alerts count:', data.length);
        this.lowStockAlerts.set(data);
        checkComplete();
      },
      error: (err) => {
        console.error('🏠 HomeComponent: Error loading low stock alerts:', err);
        console.error('🏠 HomeComponent: Error details:', JSON.stringify(err, null, 2));
        checkComplete();
      }
    });

    console.log('🏠 HomeComponent: Loading expiring soon alerts...');
    this.apiService.getExpiringSoonAlerts().subscribe({
      next: (data) => {
        console.log('🏠 HomeComponent: Expiring soon alerts loaded:', data);
        console.log('🏠 HomeComponent: Expiring soon alerts count:', data.length);
        this.expiringSoonAlerts.set(data);
        checkComplete();
      },
      error: (err) => {
        console.error('🏠 HomeComponent: Error loading expiring soon alerts:', err);
        console.error('🏠 HomeComponent: Error details:', JSON.stringify(err, null, 2));
        checkComplete();
      }
    });

    console.log('🏠 HomeComponent: Loading expired alerts...');
    this.apiService.getExpiredAlerts().subscribe({
      next: (data) => {
        console.log('🏠 HomeComponent: Expired alerts loaded:', data);
        console.log('🏠 HomeComponent: Expired alerts count:', data.length);
        this.expiredAlerts.set(data);
        checkComplete();
      },
      error: (err) => {
        console.error('🏠 HomeComponent: Error loading expired alerts:', err);
        console.error('🏠 HomeComponent: Error details:', JSON.stringify(err, null, 2));
        checkComplete();
      }
    });
  }

  marcarComoVisto(alerta: Alert): void {
    this.alertsService.marcarComoLido(alerta.id).subscribe({
      next: () => {
        this.notificationService.success('Alerta marcado como visto', 'O alerta foi removido do painel.');
        // Remove o alerta da lista
        if (alerta.tipo === 'ESTOQUE_BAIXO') {
          this.lowStockAlerts.update(alerts => alerts.filter(a => a.id !== alerta.id));
        } else if (alerta.tipo === 'VALIDADE_PROXIMA') {
          this.expiringSoonAlerts.update(alerts => alerts.filter(a => a.id !== alerta.id));
        } else if (alerta.tipo === 'VALIDADE_VENCIDA') {
          this.expiredAlerts.update(alerts => alerts.filter(a => a.id !== alerta.id));
        }
      },
      error: (err) => {
        console.error("Error marking alert as read:", err);
        this.notificationService.error('Erro', 'Não foi possível marcar o alerta como visto.');
      }
    });
  }

  navegarParaEstoque(medicamentoId: string): void {
    console.log('🏠 HomeComponent: Navegando para estoque com medicamentoId:', medicamentoId);
    this.router.navigate(['/estoque'], { queryParams: { medicamento: medicamentoId } });
  }

  navegarParaMedicamento(medicamentoId: string): void {
    console.log('🏠 HomeComponent: Navegando para medicamentos com medicamentoId:', medicamentoId);
    this.router.navigate(['/medicamentos'], { queryParams: { id: medicamentoId } });
  }
}
