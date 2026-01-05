import { ChangeDetectionStrategy, Component, inject, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LogsService } from '../../services/logs.service';
import { NotificationService } from '../../services/notification.service';
import { Log } from '../../models/types';

@Component({
  selector: 'app-logs',
  templateUrl: './logs.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
})
export class LogsComponent {
  private logsService = inject(LogsService);
  private notificationService = inject(NotificationService);
  private cdr = inject(ChangeDetectorRef);

  logs = signal<Log[]>([]);
  filteredLogs = signal<Log[]>([]);
  searchTerm = signal<string>('');
  isLoading = signal(true);
  isExporting = signal(false);
  
  // Cache para armazenar detalhes parseados de cada log
  parsedDetails = signal<Map<string, any>>(new Map());

  constructor() {
    console.log('📋 LogsComponent: Constructor called');
    this.loadLogs();
  }

  loadLogs() {
    console.log('📋 LogsComponent: loadLogs() called');
    this.isLoading.set(true);
    console.log('📋 LogsComponent: Calling logsService.getUltimos100()...');
    this.logsService.getUltimos100().subscribe({
      next: (data) => {
        console.log('📋 LogsComponent: getUltimos100 response received:', data);
        console.log('📋 LogsComponent: Total de logs recebidos:', data.length);
        this.logs.set(data);
        this.applySearchFilter();
        console.log('📋 LogsComponent: Total de logs após filtro:', this.filteredLogs().length);
        this.isLoading.set(false);
        console.log('📋 LogsComponent: Calling detectChanges()...');
        this.cdr.detectChanges();
        console.log('📋 LogsComponent: detectChanges() completed');
      },
      error: (err) => {
        console.error('📋 LogsComponent: ERROR loading logs:', err);
        console.error('📋 LogsComponent: Error details:', JSON.stringify(err, null, 2));
        this.notificationService.error('Erro', 'Não foi possível carregar os logs.');
        this.isLoading.set(false);
        this.cdr.detectChanges();
      }
    });
  }

  exportCsv() {
    this.isExporting.set(true);
    this.logsService.exportCsv().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `logs_auditoria_${new Date().toISOString().split('T')[0]}.csv`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        this.isExporting.set(false);
        this.notificationService.success('Exportação Concluída', 'Arquivo CSV baixado com sucesso!');
      },
      error: (err) => {
        console.error('Erro ao exportar logs:', err);
        this.notificationService.error('Erro', 'Não foi possível exportar os logs.');
        this.isExporting.set(false);
      }
    });
  }

  applySearchFilter() {
    const term = this.searchTerm().toLowerCase().trim();
    const allLogs = this.logs();
    
    if (!term) {
      this.filteredLogs.set(allLogs);
      return;
    }
    
    const filtered = allLogs.filter(log => 
      log.tipoOperacao?.toLowerCase().includes(term) ||
      log.tipoEntidade?.toLowerCase().includes(term) ||
      log.descricao?.toLowerCase().includes(term) ||
      log.usuarioNome?.toLowerCase().includes(term) ||
      log.usuarioEmail?.toLowerCase().includes(term) ||
      log.entidadeId?.toLowerCase().includes(term) ||
      log.id?.toLowerCase().includes(term)
    );
    this.filteredLogs.set(filtered);
  }

  onSearchChange(term: string) {
    this.searchTerm.set(term);
    this.applySearchFilter();
  }

  /**
   * Formata os detalhes do log, especialmente para vendas
   */
  formatDetalhes(log: Log): string {
    if (!log.detalhes) {
      return '';
    }

    // Se for uma venda, tenta parsear o JSON e formatar
    if (log.tipoEntidade === 'Venda' || log.tipoEntidade === 'VENDA') {
      try {
        const detalhes = JSON.parse(log.detalhes);
        
        // Armazena os detalhes parseados no cache
        const currentCache = new Map(this.parsedDetails());
        currentCache.set(log.id, detalhes);
        this.parsedDetails.set(currentCache);
        
        const partes: string[] = [];

        // Adiciona status da venda
        if (detalhes.status !== undefined) {
          const statusMap: { [key: string]: string } = {
            'CONCLUIDA': 'Concluída',
            'CANCELADA': 'Cancelada',
            'PENDENTE': 'Pendente'
          };
          const statusLabel = statusMap[detalhes.status] || detalhes.status;
          partes.push(`Status: ${statusLabel}`);
        }

        // Adiciona valor total
        if (detalhes.valorTotal !== undefined) {
          partes.push(`Valor Total: R$ ${Number(detalhes.valorTotal).toFixed(2).replace('.', ',')}`);
        }

        // Adiciona ID do cliente truncado (será renderizado com tooltip no HTML)
        if (detalhes.clienteId !== undefined) {
          const clienteId = detalhes.clienteId;
          const idTruncado = clienteId.length > 8 ? clienteId.substring(0, 8) + '...' : clienteId;
          partes.push(`Cliente ID: ${idTruncado}`);
        }

        // Adiciona itens
        if (detalhes.itens && Array.isArray(detalhes.itens) && detalhes.itens.length > 0) {
          detalhes.itens.forEach((item: any, index: number) => {
            if (index > 0) partes.push(''); // Linha em branco entre itens
            partes.push(`Item ${index + 1}: ${item.medicamentoNome || 'N/A'}`);
            if (item.quantidade !== undefined) {
              partes.push(`  • Quantidade: ${item.quantidade}`);
            }
            if (item.precoUnitario !== undefined) {
              partes.push(`  • Valor Unitário: R$ ${Number(item.precoUnitario).toFixed(2).replace('.', ',')}`);
            }
            if (item.subtotal !== undefined) {
              partes.push(`  • Subtotal: R$ ${Number(item.subtotal).toFixed(2).replace('.', ',')}`);
            }
          });
        }

        return partes.join('\n');
      } catch (error) {
        // Se não conseguir parsear, tenta exibir os detalhes originais
        console.error('Erro ao parsear detalhes do log:', error, log.detalhes);
        // Se não conseguir parsear, retorna os detalhes originais
        return log.detalhes;
      }
    }

    // Para outros tipos, retorna os detalhes como estão
    return log.detalhes;
  }

  /**
   * Obtém o ID completo do cliente de um log
   */
  getClienteId(log: Log): string | null {
    if (!log.detalhes) return null;
    
    try {
      // Tenta obter do cache primeiro
      const cached = this.parsedDetails().get(log.id);
      if (cached && cached.clienteId) {
        return cached.clienteId;
      }
      
      // Se não estiver no cache, tenta parsear
      const detalhes = JSON.parse(log.detalhes);
      if (detalhes.clienteId) {
        // Armazena no cache
        const currentCache = new Map(this.parsedDetails());
        currentCache.set(log.id, detalhes);
        this.parsedDetails.set(currentCache);
        return detalhes.clienteId;
      }
      
      return null;
    } catch {
      return null;
    }
  }

  /**
   * Copia o ID do cliente para a área de transferência
   */
  copyClienteId(log: Log, event: Event) {
    event.stopPropagation();
    const clienteId = this.getClienteId(log);
    if (clienteId) {
      navigator.clipboard.writeText(clienteId).then(() => {
        this.notificationService.success('Copiado!', 'ID do cliente copiado para a área de transferência.');
      }).catch(() => {
        this.notificationService.error('Erro', 'Não foi possível copiar o ID.');
      });
    }
  }
}
