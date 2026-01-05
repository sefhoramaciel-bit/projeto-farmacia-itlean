import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Sale, SaleRequest, MessageResponse } from '../models/types';

@Injectable({
  providedIn: 'root',
})
export class SalesService {
  private http = inject(HttpClient);

  getAll(): Observable<Sale[]> {
    return this.http.get<Sale[]>(`${environment.apiUrl}/vendas`);
  }

  getById(id: string): Observable<Sale> {
    return this.http.get<Sale>(`${environment.apiUrl}/vendas/${id}`);
  }

  getByCliente(clienteId: string): Observable<Sale[]> {
    return this.http.get<Sale[]>(`${environment.apiUrl}/vendas/cliente/${clienteId}`);
  }

  create(sale: SaleRequest): Observable<Sale> {
    return this.http.post<Sale>(`${environment.apiUrl}/vendas`, sale);
  }

  cancelar(id: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${environment.apiUrl}/vendas/${id}/cancelar`, {});
  }

  createCancelada(sale: SaleRequest): Observable<Sale> {
    return this.http.post<Sale>(`${environment.apiUrl}/vendas/cancelada`, sale);
  }
}




