import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../environments/environment';
import { Customer, CustomerRequest, MessageResponse } from '../models/types';

@Injectable({
  providedIn: 'root',
})
export class CustomersService {
  private http = inject(HttpClient);

  getAll(): Observable<Customer[]> {
    console.log('👥 CustomersService: getAll() called');
    console.log('👥 CustomersService: URL:', `${environment.apiUrl}/clientes`);
    return this.http.get<Customer[]>(`${environment.apiUrl}/clientes`);
  }

  getById(id: string): Observable<Customer> {
    return this.http.get<Customer>(`${environment.apiUrl}/clientes/${id}`);
  }

  getByCpf(cpf: string): Observable<Customer | null> {
    // Remove formatação do CPF para buscar
    const cleanCpf = cpf.replace(/\D/g, '');
    // Busca todos e filtra no frontend (backend não tem endpoint específico)
    return this.http.get<Customer[]>(`${environment.apiUrl}/clientes`).pipe(
      map(customers => {
        const customer = customers.find(c => c.cpf.replace(/\D/g, '') === cleanCpf);
        return customer || null;
      })
    );
  }

  create(customer: CustomerRequest): Observable<Customer> {
    return this.http.post<Customer>(`${environment.apiUrl}/clientes`, customer);
  }

  update(id: string, customer: CustomerRequest): Observable<Customer> {
    return this.http.put<Customer>(`${environment.apiUrl}/clientes/${id}`, customer);
  }

  delete(id: string): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${environment.apiUrl}/clientes/${id}`);
  }
}

