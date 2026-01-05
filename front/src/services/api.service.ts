import { Injectable, inject } from '@angular/core';
import { MedicinesService } from './medicines.service';
import { CategoriesService } from './categories.service';
import { CustomersService } from './customers.service';
import { StockService } from './stock.service';
import { SalesService } from './sales.service';
import { LogsService } from './logs.service';
import { AlertsService } from './alerts.service';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { Medicine, Category, Customer, Sale, Log, Alert } from '../models/types';

/**
 * Serviço API unificado que usa os serviços específicos
 * Mantido para compatibilidade com componentes existentes
 */
@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private medicinesService = inject(MedicinesService);
  private categoriesService = inject(CategoriesService);
  private customersService = inject(CustomersService);
  private stockService = inject(StockService);
  private salesService = inject(SalesService);
  private logsService = inject(LogsService);
  private alertsService = inject(AlertsService);

  // Medicine methods
  getMedicines(active: boolean): Observable<Medicine[]> {
    console.log('🔌 ApiService: getMedicines() called, active:', active);
    const result = active ? this.medicinesService.getActive() : this.medicinesService.getAll();
    console.log('🔌 ApiService: getMedicines() returning Observable');
    return result;
  }

  searchMedicines(term: string): Observable<Medicine[]> {
    // Filtra no frontend - backend não tem endpoint de busca específico
    return this.medicinesService.getActive().pipe(
      map(medicines => {
        const lowerTerm = term.toLowerCase();
        return medicines.filter(m => 
          m.ativo && (
            m.nome.toLowerCase().includes(lowerTerm) ||
            (m.categoria?.nome.toLowerCase().includes(lowerTerm))
          )
        );
      })
    );
  }

  getLowStockAlerts(): Observable<Alert[]> {
    return this.alertsService.getEstoqueBaixo();
  }

  getExpiringSoonAlerts(): Observable<Alert[]> {
    return this.alertsService.getValidadeProxima();
  }

  getExpiredAlerts(): Observable<Alert[]> {
    return this.alertsService.getValidadeVencida();
  }

  gerarAlertas(): Observable<string> {
    return this.alertsService.gerarAlertas();
  }

  // Category methods
  getCategories(): Observable<Category[]> {
    console.log('🔌 ApiService: getCategories() called');
    const result = this.categoriesService.getAll();
    console.log('🔌 ApiService: getCategories() returning Observable');
    return result;
  }

  // Customer methods
  getCustomers(): Observable<Customer[]> {
    console.log('🔌 ApiService: getCustomers() called');
    const result = this.customersService.getAll();
    console.log('🔌 ApiService: getCustomers() returning Observable');
    return result;
  }

  findCustomerByCpf(cpf: string): Observable<Customer | undefined> {
    return this.customersService.getByCpf(cpf).pipe(
      map(customer => customer || undefined)
    );
  }

  // Generic CRUD methods (for backward compatibility)
  create(entity: string, data: any, files?: File[]): Observable<any> {
    switch (entity) {
      case 'medicamentos':
        return this.medicinesService.create(data, files || []);
      case 'categorias':
        return this.categoriesService.create(data);
      case 'clientes':
        return this.customersService.create(data);
      case 'estoque/entrada':
        return this.stockService.entrada(data);
      case 'estoque/saida':
        return this.stockService.saida(data);
      default:
        return of(null);
    }
  }

  update(entity: string, id: string, data: any, files?: File[] | null): Observable<any> {
    switch (entity) {
      case 'medicamentos':
        return this.medicinesService.update(id, data, files || null);
      case 'categorias':
        return this.categoriesService.update(id, data);
      case 'clientes':
        return this.customersService.update(id, data);
      default:
        return of(null);
    }
  }

  delete(entity: string, id: string): Observable<any> {
    switch (entity) {
      case 'medicamentos':
        return this.medicinesService.delete(id);
      case 'categorias':
        return this.categoriesService.delete(id);
      case 'clientes':
        return this.customersService.delete(id);
      default:
        return of({});
    }
  }

  // Sale methods
  createSale(sale: Partial<Sale>): Observable<Sale> {
    // Converte para SaleRequest (remove campos desnecessários)
    const saleRequest: any = {
      clienteId: sale.clienteId,
      itens: (sale as any).itens?.map((item: any) => ({
        medicamentoId: item.medicamentoId,
        quantidade: item.quantidade,
      })) || []
    };
    return this.salesService.create(saleRequest);
  }

  // Log methods
  createLog(logData: Omit<Log, 'id' | 'dataHora'>): Observable<Log> {
    // Backend não tem endpoint para criar logs manualmente
    // Logs são criados automaticamente pelo backend
    console.warn('Logs são criados automaticamente pelo backend');
    return of({} as Log);
  }

  getLogs(limit: number = 100): Observable<Log[]> {
    return this.logsService.getUltimos100();
  }

  getAllLogs(): Observable<Log[]> {
    // Para exportação, vamos usar o endpoint de exportação
    return this.logsService.getUltimos100();
  }
}
