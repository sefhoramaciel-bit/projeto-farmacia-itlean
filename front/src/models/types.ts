// User Types
export interface User {
  id: string;
  nome: string;
  email: string;
  role: 'ADMIN' | 'VENDEDOR';
  avatarUrl?: string;
  createdAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  usuario: User;
}

// Category Types
export interface Category {
  id: string;
  nome: string;
  descricao?: string;
  createdAt?: string;
}

export interface CategoryRequest {
  nome: string;
  descricao?: string;
}

// Medicine Types
export interface Medicine {
  id: string;
  nome: string;
  descricao?: string;
  preco: number;
  quantidadeEstoque: number;
  validade?: string;
  ativo: boolean;
  categoria?: Category;
  imagens?: string[];
  createdAt?: string;
}

export interface MedicineRequest {
  nome: string;
  descricao?: string;
  preco: number;
  quantidadeEstoque: number;
  validade?: string;
  ativo?: boolean;
  categoriaId?: string;
}

// Customer Types
export interface Customer {
  id: string;
  nome: string;
  cpf: string;
  telefone?: string;
  email: string;
  endereco?: string;
  dataNascimento: string;
  createdAt?: string;
}

export interface CustomerRequest {
  nome: string;
  cpf: string;
  telefone?: string;
  email: string;
  endereco?: string;
  dataNascimento: string;
}

// Stock Types
export interface StockMovement {
  id: string;
  medicamentoId: string;
  quantidade: number;
  tipo: 'ENTRADA' | 'SAIDA';
  data: string;
  motivo?: string;
  estoqueTotal?: number;
}

export interface StockRequest {
  medicamentoId: string;
  quantidade: number;
  motivo?: string;
}

export interface StockResponse {
  medicamentoId: string;
  medicamentoNome: string;
  quantidadeEstoque: number;
}

export interface StockOperationResponse {
  mensagem: string;
  medicamentoId: string;
  medicamentoNome: string;
  quantidadeMovimentada: number;
  quantidadeEstoqueAtual: number;
  tipoOperacao: 'ENTRADA' | 'SAIDA';
}

// Sale Types
export interface SaleItem {
  medicamentoId: string;
  quantidade: number;
}

export interface SaleItemResponse {
  id: string;
  medicamentoId: string;
  medicamentoNome: string;
  quantidade: number;
  precoUnitario: number;
  subtotal: number;
}

export interface Sale {
  id: string;
  clienteId: string;
  clienteNome: string;
  usuarioId: string;
  usuarioNome: string;
  status: 'PENDENTE' | 'CONCLUIDA' | 'CANCELADA';
  valorTotal: number;
  itens: SaleItemResponse[];
  createdAt: string;
}

export interface SaleRequest {
  clienteId: string;
  itens: SaleItem[];
}

export interface CartItem extends Medicine {
  quantidadeCarrinho: number;
}

// Log Types
export interface Log {
  id: string;
  tipoOperacao: string;
  tipoEntidade: string;
  entidadeId: string;
  descricao: string;
  detalhes?: string;
  usuarioId: string;
  usuarioNome: string;
  usuarioEmail: string;
  dataHora: string;
}

// Alert Types
export interface Alert {
  id: string;
  medicamentoId: string;
  medicamentoNome: string;
  tipo: string;
  mensagem: string;
  lido: boolean;
  createdAt: string;
}

// Message Response
export interface MessageResponse {
  mensagem: string;
  id?: string;
}
