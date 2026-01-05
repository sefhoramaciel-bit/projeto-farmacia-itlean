import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Log } from '../models/types';

@Injectable({
  providedIn: 'root',
})
export class LogsService {
  private http = inject(HttpClient);

  getUltimos100(): Observable<Log[]> {
    console.log('🔌 LogsService: getUltimos100() called');
    console.log('🔌 LogsService: URL:', `${environment.apiUrl}/logs`);
    return this.http.get<Log[]>(`${environment.apiUrl}/logs`);
  }

  exportCsv(): Observable<Blob> {
    return this.http.get(`${environment.apiUrl}/logs/export`, {
      responseType: 'blob'
    });
  }
}


