import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Alert } from '../models/types';

@Injectable({
  providedIn: 'root',
})
export class AlertsService {
  private http = inject(HttpClient);

  getAll(): Observable<Alert[]> {
    return this.http.get<Alert[]>(`${environment.apiUrl}/alertas`);
  }

  getNaoLidos(): Observable<Alert[]> {
    return this.http.get<Alert[]>(`${environment.apiUrl}/alertas/nao-lidos`);
  }

  getEstoqueBaixo(): Observable<Alert[]> {
    return this.http.get<Alert[]>(`${environment.apiUrl}/alertas/estoque-baixo`);
  }

  getValidadeProxima(): Observable<Alert[]> {
    return this.http.get<Alert[]>(`${environment.apiUrl}/alertas/validade-proxima`);
  }

  getValidadeVencida(): Observable<Alert[]> {
    return this.http.get<Alert[]>(`${environment.apiUrl}/alertas/validade-vencida`);
  }

  gerarAlertas(): Observable<string> {
    return this.http.post(`${environment.apiUrl}/alertas/gerar`, {}, { responseType: 'text' }) as Observable<string>;
  }

  marcarComoLido(id: string): Observable<Alert> {
    return this.http.put<Alert>(`${environment.apiUrl}/alertas/${id}/ler`, {});
  }
}


