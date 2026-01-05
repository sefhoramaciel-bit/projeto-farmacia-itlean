import { Injectable } from '@angular/core';

/**
 * Serviço de log mantido para compatibilidade
 * Os logs são criados automaticamente pelo backend
 */
@Injectable({
  providedIn: 'root',
})
export class LogService {
  logAction(action: string, entityType: string, details: string) {
    // Logs são criados automaticamente pelo backend
    // Este método é mantido apenas para compatibilidade
    console.log(`[Log] ${action} - ${entityType}: ${details}`);
  }
}
