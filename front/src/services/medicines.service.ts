import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Medicine, MedicineRequest, MessageResponse } from '../models/types';

@Injectable({
  providedIn: 'root',
})
export class MedicinesService {
  private http = inject(HttpClient);

  getAll(): Observable<Medicine[]> {
    console.log('💊 MedicinesService: getAll() called');
    console.log('💊 MedicinesService: URL:', `${environment.apiUrl}/medicamentos`);
    return this.http.get<Medicine[]>(`${environment.apiUrl}/medicamentos`);
  }

  getActive(): Observable<Medicine[]> {
    console.log('💊 MedicinesService: getActive() called');
    console.log('💊 MedicinesService: URL:', `${environment.apiUrl}/medicamentos/ativos`);
    return this.http.get<Medicine[]>(`${environment.apiUrl}/medicamentos/ativos`);
  }

  getById(id: string): Observable<Medicine> {
    return this.http.get<Medicine>(`${environment.apiUrl}/medicamentos/${id}`);
  }

  create(medicine: MedicineRequest, files: File[]): Observable<Medicine> {
    const formData = new FormData();
    formData.append('medicamento', JSON.stringify(medicine));
    if (files && files.length > 0) {
      files.forEach(file => formData.append('files', file));
    }
    return this.http.post<Medicine>(`${environment.apiUrl}/medicamentos`, formData);
  }

  update(id: string, medicine: MedicineRequest, files: File[] | null): Observable<Medicine> {
    const formData = new FormData();
    formData.append('medicamento', JSON.stringify(medicine));
    if (files && files.length > 0) {
      files.forEach(file => formData.append('files', file));
    }
    return this.http.put<Medicine>(`${environment.apiUrl}/medicamentos/${id}`, formData);
  }

  delete(id: string): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${environment.apiUrl}/medicamentos/${id}`);
  }

  updateStatus(id: string, ativo: boolean): Observable<Medicine> {
    return this.http.patch<Medicine>(`${environment.apiUrl}/medicamentos/${id}/status`, null, {
      params: { ativo: ativo.toString() }
    });
  }

  uploadImages(id: string, files: File[]): Observable<Medicine> {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));
    return this.http.post<Medicine>(`${environment.apiUrl}/medicamentos/${id}/imagens`, formData);
  }

  removeImages(id: string): Observable<Medicine> {
    return this.http.delete<Medicine>(`${environment.apiUrl}/medicamentos/${id}/imagens`);
  }
}


