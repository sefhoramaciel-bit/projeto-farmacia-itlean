import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { User, MessageResponse } from '../models/types';

export interface UserRequest {
  nome: string;
  email: string;
  password?: string;
  role: 'ADMIN' | 'VENDEDOR';
  avatarUrl?: string;
}

@Injectable({
  providedIn: 'root',
})
export class UsersService {
  private http = inject(HttpClient);

  getAll(): Observable<User[]> {
    return this.http.get<User[]>(`${environment.apiUrl}/usuarios`);
  }

  getById(id: string): Observable<User> {
    return this.http.get<User>(`${environment.apiUrl}/usuarios/${id}`);
  }

  create(user: UserRequest): Observable<User> {
    return this.http.post<User>(`${environment.apiUrl}/usuarios`, user);
  }

  update(id: string, user: UserRequest): Observable<User> {
    return this.http.put<User>(`${environment.apiUrl}/usuarios/${id}`, user);
  }

  delete(id: string): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${environment.apiUrl}/usuarios/${id}`);
  }

  uploadAvatar(id: string, file: File): Observable<User> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<User>(`${environment.apiUrl}/usuarios/${id}/avatar`, formData);
  }

  createWithAvatar(user: UserRequest, avatarFile?: File): Observable<User> {
    const formData = new FormData();
    formData.append('usuario', JSON.stringify(user));
    if (avatarFile) {
      formData.append('avatar', avatarFile);
    }
    return this.http.post<User>(`${environment.apiUrl}/usuarios`, formData);
  }
}

