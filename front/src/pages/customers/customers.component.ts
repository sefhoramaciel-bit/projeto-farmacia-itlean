import { ChangeDetectionStrategy, Component, inject, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { Customer } from '../../models/types';
import { ModalComponent } from '../../components/modal/modal.component';
import { LogService } from '../../services/log.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-customers',
  templateUrl: './customers.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule, ModalComponent],
})
export class CustomersComponent {
  private apiService = inject(ApiService);
  private notificationService = inject(NotificationService);
  private logService = inject(LogService);
  private authService = inject(AuthService);
  // Fix: Explicitly type FormBuilder to resolve type inference issue with inject().
  private fb: FormBuilder = inject(FormBuilder);

  customers = signal<Customer[]>([]);
  filteredCustomers = signal<Customer[]>([]);
  searchTerm = signal<string>('');
  isLoading = signal(true);
  
  isModalOpen = signal(false);
  modalTitle = signal('Adicionar Cliente');
  currentCustomerId = signal<string | null>(null);

  customerForm = this.fb.group({
    nome: ['', [Validators.required]],
    cpf: ['', [Validators.required, Validators.pattern(/^\d{3}\.\d{3}\.\d{3}\-\d{2}$/)]],
    email: ['', [Validators.required, Validators.email]],
    telefone: [''],
    endereco: [''],
    dataNascimento: ['', [Validators.required]],
  });

  private cdr = inject(ChangeDetectorRef);

  constructor() {
    console.log('👥 CustomersComponent: Constructor called');
    this.loadCustomers();
  }

  loadCustomers() {
    console.log('👥 CustomersComponent: loadCustomers() called');
    this.isLoading.set(true);
    console.log('👥 CustomersComponent: Calling apiService.getCustomers()...');
    this.apiService.getCustomers().subscribe({
      next: (data) => {
        console.log('👥 CustomersComponent: getCustomers response received:', data);
        // Ordena alfabeticamente por nome
        const sortedData = data.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' }));
        this.customers.set(sortedData);
        this.applySearchFilter();
        this.isLoading.set(false);
        console.log('👥 CustomersComponent: Calling detectChanges()...');
        this.cdr.detectChanges();
        console.log('👥 CustomersComponent: detectChanges() completed');
      },
      error: (err) => {
        console.error('👥 CustomersComponent: ERROR loading customers:', err);
        console.error('👥 CustomersComponent: Error details:', JSON.stringify(err, null, 2));
        this.isLoading.set(false);
        this.cdr.detectChanges();
      }
    });
  }

  openCreateModal() {
    if (!this.isAdmin()) {
      this.notificationService.error('Acesso Negado', 'Você não tem permissão para criar clientes. Apenas administradores podem realizar esta ação.');
      return;
    }
    this.currentCustomerId.set(null);
    this.modalTitle.set('Adicionar Cliente');
    this.customerForm.reset();
    this.isModalOpen.set(true);
  }

  openEditModal(cust: Customer) {
    if (!this.isAdmin()) {
      this.notificationService.error('Acesso Negado', 'Você não tem permissão para editar clientes. Apenas administradores podem realizar esta ação.');
      return;
    }
    this.currentCustomerId.set(cust.id);
    this.modalTitle.set('Editar Cliente');
    
    // Formata a data de nascimento para o formato YYYY-MM-DD esperado pelo input type="date"
    let dataNascimentoFormatted = '';
    if (cust.dataNascimento) {
      if (cust.dataNascimento.includes('T')) {
        // Se vem no formato ISO (com 'T'), pega apenas a parte da data
        dataNascimentoFormatted = cust.dataNascimento.split('T')[0];
      } else if (cust.dataNascimento.match(/^\d{2}\/\d{2}\/\d{4}$/)) {
        // Se vem no formato dd/MM/yyyy, converte para YYYY-MM-DD
        const [dia, mes, ano] = cust.dataNascimento.split('/');
        dataNascimentoFormatted = `${ano}-${mes}-${dia}`;
      } else {
        // Se já está no formato YYYY-MM-DD, usa direto
        dataNascimentoFormatted = cust.dataNascimento;
      }
    }
    
    const formData: any = {
      nome: cust.nome,
      cpf: cust.cpf,
      email: cust.email,
      telefone: cust.telefone || '',
      endereco: cust.endereco || '',
      dataNascimento: dataNascimentoFormatted,
    };
    
    this.customerForm.patchValue(formData);
    this.isModalOpen.set(true);
  }

  saveCustomer() {
    if (!this.isAdmin()) {
      this.notificationService.error('Acesso Negado', 'Você não tem permissão para salvar clientes. Apenas administradores podem realizar esta ação.');
      return;
    }
    if (this.customerForm.invalid) {
      // Verifica campos específicos e mostra mensagens específicas
      const nomeControl = this.customerForm.get('nome');
      const cpfControl = this.customerForm.get('cpf');
      const emailControl = this.customerForm.get('email');
      const dataNascimentoControl = this.customerForm.get('dataNascimento');
      
      if (nomeControl?.hasError('required')) {
        this.notificationService.error('Campo Obrigatório', 'O campo Nome é obrigatório. Por favor, preencha o nome.');
        return;
      }
      
      if (cpfControl?.hasError('required')) {
        this.notificationService.error('Campo Obrigatório', 'O campo CPF é obrigatório. Por favor, preencha o CPF.');
        return;
      }
      
      if (cpfControl?.hasError('pattern')) {
        this.notificationService.error('CPF Inválido', 'O CPF deve estar no formato 000.000.000-00. Por favor, verifique o CPF.');
        return;
      }
      
      if (emailControl?.hasError('required')) {
        this.notificationService.error('Campo Obrigatório', 'O campo E-mail é obrigatório. Por favor, preencha o e-mail.');
        return;
      }
      
      if (emailControl?.hasError('email')) {
        this.notificationService.error('E-mail Inválido', 'O e-mail informado é inválido. Por favor, informe um e-mail válido.');
        return;
      }
      
      if (dataNascimentoControl?.hasError('required')) {
        this.notificationService.error('Campo Obrigatório', 'O campo Data de Nascimento é obrigatório. Por favor, preencha a data de nascimento.');
        return;
      }
      
      // Mensagem genérica caso algum outro erro não capturado
      this.notificationService.error('Formulário Inválido', 'Por favor, preencha todos os campos obrigatórios corretamente.');
      return;
    }
    
    const formValue = this.customerForm.value;
    const id = this.currentCustomerId();
    const action = id ? 'update' : 'create';
    const apiCall = id 
      ? this.apiService.update('clientes', id, formValue)
      : this.apiService.create('clientes', formValue);

    apiCall.subscribe({
      next: () => {
        const actionText = action === 'create' ? 'criado' : 'atualizado';
        this.notificationService.success(`Cliente ${actionText}!`, 'O registro foi salvo com sucesso.');
        this.logService.logAction(
          action === 'create' ? 'Criação' : 'Atualização',
          'Cliente',
          `Cliente '${formValue.nome}' foi ${actionText}.`
        );
        this.loadCustomers();
        this.isModalOpen.set(false);
      },
      error: (err) => {
        console.error('👥 CustomersComponent: ERROR saving customer:', err);
        console.error('👥 CustomersComponent: Error details:', JSON.stringify(err, null, 2));
        
        // Extrai a mensagem de erro do backend
        let errorMessage = 'Ocorreu um erro. Tente novamente.';
        
        if (err?.error) {
          // Caso 1: err.error é uma string direta
          if (typeof err.error === 'string') {
            errorMessage = err.error;
          }
          // Caso 2: err.error é um objeto
          else if (typeof err.error === 'object') {
            if (err.error.error && err.error.error !== 'Erro interno do servidor') {
              errorMessage = err.error.error;
            } else if (err.error.message) {
              errorMessage = err.error.message;
            }
          }
        }
        
        this.notificationService.error('Erro ao Salvar', errorMessage);
      }
    });
  }

  deleteCustomer(id: string) {
    if (!this.isAdmin()) {
      this.notificationService.error('Acesso Negado', 'Você não tem permissão para excluir clientes. Apenas administradores podem realizar esta ação.');
      return;
    }
    this.notificationService.confirm('Confirmar Exclusão', 'Você tem certeza que deseja excluir este cliente?')
      .then((result) => {
        if (result.isConfirmed) {
          const custName = this.customers().find(c => c.id === id)?.nome || `ID ${id}`;
          this.apiService.delete('clientes', id).subscribe({
            next: () => {
              this.notificationService.success('Excluído!', 'O cliente foi excluído com sucesso.');
              this.logService.logAction('Exclusão', 'Cliente', `Cliente '${custName}' foi excluído.`);
              this.loadCustomers();
            },
            error: () => this.notificationService.error('Erro ao Excluir', 'Não foi possível excluir o cliente.')
          });
        }
      });
  }

  applySearchFilter() {
    const term = this.searchTerm().toLowerCase().trim();
    const allCustomers = this.customers();
    
    if (!term) {
      this.filteredCustomers.set(allCustomers);
      return;
    }
    
    const filtered = allCustomers.filter(cust => 
      cust.nome.toLowerCase().includes(term) ||
      cust.cpf.toLowerCase().includes(term) ||
      cust.email.toLowerCase().includes(term) ||
      cust.id.toLowerCase().includes(term)
    );
    this.filteredCustomers.set(filtered);
  }

  onSearchChange(term: string) {
    this.searchTerm.set(term);
    this.applySearchFilter();
  }

  formatCpf(event: Event) {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');
    if (value.length > 11) value = value.slice(0, 11);

    if (value.length > 9) {
      value = value.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    } else if (value.length > 6) {
      value = value.replace(/(\d{3})(\d{3})(\d{0,3})/, '$1.$2.$3');
    } else if (value.length > 3) {
      value = value.replace(/(\d{3})(\d{0,3})/, '$1.$2');
    }
    this.customerForm.controls.cpf.setValue(value, { emitEvent: false });
  }

  formatTelefone(event: Event) {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');
    
    // Limita a 11 dígitos (DDD + 9 dígitos para celular ou 8 para fixo)
    if (value.length > 11) value = value.slice(0, 11);

    // Formata conforme a quantidade de dígitos
    if (value.length > 10) {
      // Celular: (00) 00000-0000
      value = value.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    } else if (value.length > 6) {
      // Telefone fixo: (00) 0000-0000
      value = value.replace(/(\d{2})(\d{4})(\d{0,4})/, '($1) $2-$3');
    } else if (value.length > 2) {
      // Apenas DDD digitado: (00) 
      value = value.replace(/(\d{2})(\d{0,})/, '($1) $2');
    } else if (value.length > 0) {
      // Início: (0
      value = `(${value}`;
    }
    
    this.customerForm.controls.telefone.setValue(value, { emitEvent: false });
  }

  formatDate(dateString: string): string {
    if (!dateString) return '-';
    
    // Se já está no formato dd/MM/yyyy, retorna direto
    if (dateString.match(/^\d{2}\/\d{2}\/\d{4}$/)) {
      return dateString;
    }
    
    // Se está no formato ISO (YYYY-MM-DD ou com 'T'), converte para dd/MM/yyyy
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return dateString;
      
      const day = String(date.getDate()).padStart(2, '0');
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const year = date.getFullYear();
      
      return `${day}/${month}/${year}`;
    } catch {
      return dateString;
    }
  }

  isAdmin(): boolean {
    const user = this.authService.currentUser();
    return user?.role === 'ADMIN';
  }

  getShortId(id: string): string {
    if (!id) {
      console.log('👥 CustomersComponent: getShortId() called with empty ID');
      return '-';
    }
    const shortId = id.substring(0, 8) + '...';
    console.log('👥 CustomersComponent: getShortId() - Original:', id, 'Short:', shortId);
    return shortId;
  }

  copyToClipboard(text: string) {
    console.log('👥 CustomersComponent: copyToClipboard() called');
    console.log('👥 CustomersComponent: Text to copy:', text);
    console.log('👥 CustomersComponent: Attempting to copy to clipboard...');
    
    navigator.clipboard.writeText(text).then(() => {
      console.log('👥 CustomersComponent: Text copied to clipboard successfully (modern API)');
      this.notificationService.success('Copiado!', 'ID copiado para a área de transferência.');
    }).catch((err) => {
      console.log('👥 CustomersComponent: Modern clipboard API failed, trying fallback method');
      console.log('👥 CustomersComponent: Fallback error:', err);
      // Fallback para navegadores mais antigos
      const textArea = document.createElement('textarea');
      textArea.value = text;
      textArea.style.position = 'fixed';
      textArea.style.opacity = '0';
      document.body.appendChild(textArea);
      textArea.select();
      try {
        document.execCommand('copy');
        console.log('👥 CustomersComponent: Text copied to clipboard successfully (fallback method)');
        this.notificationService.success('Copiado!', 'ID copiado para a área de transferência.');
      } catch (fallbackErr) {
        console.error('👥 CustomersComponent: ERROR copying to clipboard (both methods failed):', fallbackErr);
        this.notificationService.error('Erro', 'Não foi possível copiar o ID.');
      }
      document.body.removeChild(textArea);
    });
  }
}