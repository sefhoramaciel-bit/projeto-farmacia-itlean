import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Category, CategoryRequest, MessageResponse } from '../models/types';

@Injectable({
  providedIn: 'root',
})
export class CategoriesService {
  private http = inject(HttpClient);

  getAll(): Observable<Category[]> {
    console.log('📁 CategoriesService: getAll() called');
    console.log('📁 CategoriesService: URL:', `${environment.apiUrl}/categorias`);
    return this.http.get<Category[]>(`${environment.apiUrl}/categorias`);
  }

  getById(id: string): Observable<Category> {
    return this.http.get<Category>(`${environment.apiUrl}/categorias/${id}`);
  }

  create(category: CategoryRequest): Observable<Category> {
    return this.http.post<Category>(`${environment.apiUrl}/categorias`, category);
  }

  update(id: string, category: CategoryRequest): Observable<Category> {
    return this.http.put<Category>(`${environment.apiUrl}/categorias/${id}`, category);
  }

  delete(id: string): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${environment.apiUrl}/categorias/${id}`);
  }
}


