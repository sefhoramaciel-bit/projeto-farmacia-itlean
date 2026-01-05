import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { StockRequest, StockResponse, StockOperationResponse } from '../models/types';

@Injectable({
  providedIn: 'root',
})
export class StockService {
  private http = inject(HttpClient);

  entrada(request: StockRequest): Observable<StockOperationResponse> {
    return this.http.post<StockOperationResponse>(`${environment.apiUrl}/estoque/entrada`, request);
  }

  saida(request: StockRequest): Observable<StockOperationResponse> {
    return this.http.post<StockOperationResponse>(`${environment.apiUrl}/estoque/saida`, request);
  }

  getByMedicamento(medicamentoId: string): Observable<StockResponse> {
    return this.http.get<StockResponse>(`${environment.apiUrl}/estoque/${medicamentoId}`);
  }
}






