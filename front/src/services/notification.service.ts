import { Injectable } from '@angular/core';
import Swal, { SweetAlertResult } from 'sweetalert2';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private swal = Swal.mixin({
    customClass: {
      confirmButton: 'px-4 py-2 mx-2 bg-gradient-to-r from-[#2D3345] to-[#4A5568] text-white font-semibold rounded-lg shadow-md hover:scale-105 transition-transform',
      cancelButton: 'px-4 py-2 mx-2 bg-gradient-to-r from-[#FE5D5C] to-[#E53E3E] text-white font-semibold rounded-lg shadow-md hover:scale-105 transition-transform',
      popup: 'rounded-2xl shadow-lg',
      title: 'text-2xl font-bold text-[#2D3345]',
    },
    buttonsStyling: false,
  });

  success(title: string, text: string): void {
    this.swal.fire({
      icon: 'success',
      title,
      text,
      background: 'radial-gradient(circle, #f0f9ff 0%, #99E0FF 100%)',
    });
    console.log(`Success: ${title} - ${text}`);
  }

  error(title: string, text?: string): void {
    this.swal.fire({
      icon: 'error',
      title,
      text: text || '',
      background: 'radial-gradient(circle, #ffebee 0%, #ef9a9a 100%)',
    });
    console.error(`Error: ${title} - ${text || ''}`);
  }

  confirm(
    title: string,
    text: string,
    confirmButtonText: string = 'Sim, deletar!'
  ): Promise<SweetAlertResult> {
    return this.swal.fire({
      title,
      text,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText,
      cancelButtonText: 'Cancelar',
      reverseButtons: true,
      background: 'radial-gradient(circle, #ffebee 0%, #ef9a9a 100%)',
    });
  }
}
