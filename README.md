# 📋 RESUMO COMPLETO DO PROJETO - SISTEMA DE GESTÃO DE FARMÁCIA

## 📌 VISÃO GERAL

Sistema completo de gestão de farmácia desenvolvido com arquitetura **Full Stack**, utilizando **Angular 21** no frontend e **Spring Boot 3.2.0** no backend. O sistema oferece uma solução robusta para gerenciamento de medicamentos, clientes, vendas, estoque, categorias, usuários e auditoria, com controle de acesso baseado em roles (ADMIN e VENDEDOR).

---

## 🏗️ ARQUITETURA DO PROJETO

### Estrutura de Diretórios

```
projeto-Farmacia/
├── front/                    # Aplicação Angular
│   ├── src/
│   │   ├── pages/          # Componentes de páginas
│   │   ├── services/       # Serviços Angular
│   │   ├── components/     # Componentes reutilizáveis
│   │   ├── interceptors/  # Interceptores HTTP
│   │   ├── models/         # Tipos TypeScript
│   │   └── environments/   # Configurações de ambiente
│   └── package.json
│
└── java/                    # Aplicação Spring Boot
    ├── src/main/java/com/farmacia/
    │   ├── controller/     # REST Controllers
    │   ├── service/        # Lógica de negócio
    │   ├── repository/     # JPA Repositories
    │   ├── domain/         # Entidades, DTOs, Enums
    │   ├── config/         # Configurações Spring
    │   ├── security/       # Segurança JWT
    │   └── exception/      # Tratamento de exceções
    ├── src/main/resources/
    │   ├── db/migration/   # Scripts Flyway
    │   └── application.yml # Configurações
    └── pom.xml
```

---

## 🎯 TECNOLOGIAS UTILIZADAS

### Frontend (Angular)

| Tecnologia | Versão | Uso |
|------------|--------|-----|
| **Angular** | 21.0.0 | Framework principal |
| **TypeScript** | 5.9.0 | Linguagem de programação |
| **RxJS** | 7.8.2 | Programação reativa |
| **TailwindCSS** | latest | Framework CSS utilitário |
| **SweetAlert2** | 11.26.17 | Notificações e modais |
| **Crypto-JS** | 4.2.0 | Criptografia de dados |
| **Vite** | 6.2.0 | Build tool |

**Características do Frontend:**
- ✅ **Angular Signals** para gerenciamento de estado reativo
- ✅ **Reactive Forms** para validação de formulários
- ✅ **Lazy Loading** de componentes
- ✅ **Guards** para proteção de rotas (AuthGuard, AdminGuard)
- ✅ **Interceptors** para autenticação automática
- ✅ **Change Detection OnPush** para otimização de performance
- ✅ **Standalone Components** (Angular 21)

### Backend (Spring Boot)

| Tecnologia | Versão | Uso |
|------------|--------|-----|
| **Spring Boot** | 3.2.0 | Framework principal |
| **Java** | 17 | Linguagem de programação |
| **Spring Security** | 3.2.0 | Autenticação e autorização |
| **Spring Data JPA** | 3.2.0 | Persistência de dados |
| **PostgreSQL** | - | Banco de dados relacional |
| **Flyway** | - | Migração de banco de dados |
| **JWT (JJWT)** | 0.12.3 | Tokens de autenticação |
| **Lombok** | 1.18.32 | Redução de boilerplate |
| **MapStruct** | 1.5.5 | Mapeamento de objetos |
| **Swagger/OpenAPI** | 2.3.0 | Documentação da API |
| **Maven** | - | Gerenciamento de dependências |

**Características do Backend:**
- ✅ **RESTful API** com endpoints padronizados
- ✅ **JWT Authentication** com tokens stateless
- ✅ **Role-Based Access Control (RBAC)** com @PreAuthorize
- ✅ **Transactional Management** para consistência de dados
- ✅ **Exception Handling** global com GlobalExceptionHandler
- ✅ **File Upload** para imagens (máx 5MB, múltiplas imagens)
- ✅ **Scheduled Tasks** para alertas automáticos
- ✅ **Audit Logging** completo de todas as operações
- ✅ **Flyway Migrations** para versionamento do banco
- ✅ **Swagger UI** para documentação interativa

---

## 📦 MÓDULOS E FUNCIONALIDADES

### 1. 🔐 Autenticação e Autorização

**Frontend:**
- Login com email e senha
- Armazenamento seguro de token (criptografado)
- Guards para proteção de rotas
- Interceptor HTTP para adicionar token automaticamente
- Logout com limpeza de dados

**Backend:**
- Endpoint `/api/auth/login` para autenticação
- Geração de JWT tokens com expiração (24h)
- BCrypt para hash de senhas
- UserDetailsService para carregamento de usuários
- Filtro JWT para validação de tokens

**Roles:**
- **ADMIN**: Acesso completo ao sistema
- **VENDEDOR**: Acesso limitado (leitura em alguns módulos, operações de estoque e vendas)

---

### 2. 💊 Gestão de Medicamentos

**Funcionalidades:**
- ✅ CRUD completo de medicamentos
- ✅ Upload de múltiplas imagens (1-3 imagens por medicamento)
- ✅ Carrossel de imagens no frontend
- ✅ Validações:
  - Nome obrigatório e único
  - Preço obrigatório (> 0)
  - Quantidade em estoque obrigatória (>= 0)
  - Data de validade obrigatória e futura
  - Soft delete (não permite exclusão se já foi vendido)
- ✅ Ativação/Inativação de medicamentos
- ✅ Filtro automático de medicamentos vencidos nas vendas
- ✅ Busca por nome
- ✅ Exibição de categoria associada

**Permissões:**
- **ADMIN**: Criar, editar, excluir, inativar
- **VENDEDOR**: Apenas consulta (read-only)

**Endpoints:**
- `GET /api/medicamentos` - Listar todos
- `GET /api/medicamentos/{id}` - Buscar por ID
- `POST /api/medicamentos` - Criar (multipart/form-data)
- `PUT /api/medicamentos/{id}` - Atualizar (multipart/form-data)
- `DELETE /api/medicamentos/{id}` - Excluir (soft delete)
- `PATCH /api/medicamentos/{id}/status` - Ativar/Inativar

---

### 3. 📁 Gestão de Categorias

**Funcionalidades:**
- ✅ CRUD completo de categorias
- ✅ Validação: Nome obrigatório e único
- ✅ Página de detalhes com todos os medicamentos da categoria
- ✅ Busca de medicamentos dentro da categoria
- ✅ Carrossel de imagens nos medicamentos da categoria

**Permissões:**
- **ADMIN**: Criar, editar, excluir
- **VENDEDOR**: Consulta e acesso aos detalhes (sem permissão para inativar medicamentos)

**Endpoints:**
- `GET /api/categorias` - Listar todas
- `GET /api/categorias/{id}` - Buscar por ID
- `GET /api/categorias/{id}/medicamentos` - Medicamentos da categoria
- `POST /api/categorias` - Criar
- `PUT /api/categorias/{id}` - Atualizar
- `DELETE /api/categorias/{id}` - Excluir

---

### 4. 👥 Gestão de Clientes

**Funcionalidades:**
- ✅ CRUD completo de clientes
- ✅ Validações:
  - Nome obrigatório
  - CPF obrigatório e único
  - Email obrigatório e válido
  - Data de nascimento obrigatória
- ✅ Exibição de ID truncado com opção de copiar completo
- ✅ Busca por nome, CPF, email

**Permissões:**
- **ADMIN**: Criar, editar, excluir
- **VENDEDOR**: Apenas consulta (read-only)

**Endpoints:**
- `GET /api/clientes` - Listar todos
- `GET /api/clientes/{id}` - Buscar por ID
- `POST /api/clientes` - Criar
- `PUT /api/clientes/{id}` - Atualizar
- `DELETE /api/clientes/{id}` - Excluir

---

### 5. 🛒 Ponto de Venda (Vendas)

**Funcionalidades:**
- ✅ Busca de cliente por CPF
- ✅ Exibição automática de todos os medicamentos válidos ao carregar cliente
- ✅ Filtro de medicamentos:
  - Apenas ativos
  - Apenas não vencidos
- ✅ Cards de medicamentos com:
  - Todas as informações (nome, categoria, descrição, estoque, validade, preço)
  - Carrossel de imagens (múltiplas imagens)
  - Formatação de data de validade
- ✅ Carrinho de compras:
  - Adicionar/remover itens
  - Atualizar quantidades
  - Exibir preço unitário e subtotal
  - Cálculo automático do total
- ✅ Finalização de venda:
  - Criação de venda com status CONCLUIDA
  - Redução automática de estoque
  - Registro completo em logs
- ✅ Cancelamento de venda:
  - Criação de venda com status CANCELADA
  - Restauração automática de estoque
  - Registro completo em logs

**Permissões:**
- **ADMIN e VENDEDOR**: Acesso completo

**Endpoints:**
- `POST /api/vendas` - Criar venda
- `POST /api/vendas/cancelada` - Criar venda cancelada
- `POST /api/vendas/{id}/cancelar` - Cancelar venda existente

---

### 6. 📦 Controle de Estoque

**Funcionalidades:**
- ✅ Entrada de estoque (aumentar quantidade)
- ✅ Saída de estoque (diminuir quantidade)
- ✅ Histórico de movimentações
- ✅ Registro de motivo da movimentação
- ✅ Tipo de operação (ENTRADA/SAIDA)
- ✅ Validação de estoque mínimo
- ✅ Logs automáticos de todas as movimentações

**Permissões:**
- **ADMIN e VENDEDOR**: Acesso completo

**Endpoints:**
- `POST /api/estoque/entrada` - Entrada de estoque
- `POST /api/estoque/saida` - Saída de estoque
- `GET /api/estoque/movimentacoes` - Histórico de movimentações

---

### 7. 🚨 Sistema de Alertas

**Funcionalidades:**
- ✅ **Alerta de Estoque Baixo**: Medicamentos com estoque <= 10 unidades
- ✅ **Alerta de Validade Próxima**: Medicamentos que vencem em até 30 dias
- ✅ **Alerta de Validade Vencida**: Medicamentos já vencidos
- ✅ Geração automática de alertas (tarefa agendada às 8h diariamente)
- ✅ Marcação de alertas como "visto"
- ✅ Reaparição de alertas se a condição persistir
- ✅ Filtro automático: apenas medicamentos ativos aparecem nos alertas

**Permissões:**
- **ADMIN e VENDEDOR**: Visualização e marcação como visto

**Endpoints:**
- `GET /api/alertas` - Listar todos os alertas
- `GET /api/alertas/estoque-baixo` - Alertas de estoque baixo
- `GET /api/alertas/validade-proxima` - Alertas de validade próxima
- `GET /api/alertas/vencidos` - Alertas de vencidos
- `PATCH /api/alertas/{id}/marcar-lido` - Marcar como lido

---

### 8. 📊 Logs de Auditoria

**Funcionalidades:**
- ✅ Registro automático de todas as operações:
  - Criação, atualização, exclusão de entidades
  - Login de usuários
  - Vendas (concluídas e canceladas)
  - Movimentações de estoque
- ✅ Detalhes completos em JSON:
  - Dados da entidade
  - Data e hora da operação (formato: dd/MM/yyyy HH:mm:ss)
  - Usuário responsável
  - Status da venda
  - Itens da venda (nome, quantidade, preço unitário, subtotal)
  - Cliente ID (truncado com opção de copiar completo)
- ✅ Exibição dos últimos 100 logs numerados
- ✅ Exportação CSV de todos os registros
- ✅ Busca e filtros

**Permissões:**
- **ADMIN**: Acesso completo

**Endpoints:**
- `GET /api/logs/ultimos-100` - Últimos 100 logs
- `GET /api/logs/todos` - Todos os logs
- `GET /api/logs/exportar-csv` - Exportar CSV

---

### 9. 👤 Gestão de Usuários

**Funcionalidades:**
- ✅ CRUD completo de usuários
- ✅ Upload de avatar (opcional)
- ✅ Validações:
  - Nome obrigatório
  - Email obrigatório e válido
  - Senha obrigatória (mínimo 6 caracteres)
  - Perfil (role) obrigatório
- ✅ Permissão para VENDEDOR alterar seu próprio avatar
- ✅ Exibição de avatar atual na edição
- ✅ Preview de novo avatar antes de salvar

**Permissões:**
- **ADMIN**: Criar, editar, excluir, alterar qualquer avatar
- **VENDEDOR**: Alterar apenas seu próprio avatar

**Endpoints:**
- `GET /api/usuarios` - Listar todos
- `GET /api/usuarios/{id}` - Buscar por ID
- `POST /api/usuarios` - Criar (multipart/form-data)
- `PUT /api/usuarios/{id}` - Atualizar
- `DELETE /api/usuarios/{id}` - Excluir
- `POST /api/usuarios/{id}/avatar` - Upload de avatar

---

## 🔒 SEGURANÇA

### Autenticação
- **JWT Tokens**: Tokens stateless com expiração de 24 horas
- **BCrypt**: Hash de senhas com BCrypt
- **Criptografia Local**: Dados sensíveis criptografados no localStorage (Crypto-JS)

### Autorização
- **Role-Based Access Control (RBAC)**:
  - `@PreAuthorize("hasRole('ADMIN')")` - Apenas administradores
  - `@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")` - Ambos os roles
  - Guards no frontend (AuthGuard, AdminGuard)

### Validações
- **Backend**: Bean Validation (@NotNull, @NotBlank, @Size, @Email, @Positive, @Min, @Future)
- **Frontend**: Reactive Forms com Validators (required, email, minLength, min)
- **Mensagens específicas** para cada campo obrigatório

---

## 🗄️ BANCO DE DADOS

### PostgreSQL
- **Banco**: `farmacia_db`
- **Porta**: 5432
- **Timezone**: America/Sao_Paulo

### Migrações (Flyway)
- `V1__create_tables.sql` - Criação das tabelas principais
- `V2__add_imagens_medicamento.sql` - Suporte a múltiplas imagens
- `V3__add_data_nascimento_cliente.sql` - Campo data de nascimento
- `V4__add_estoque_total_movimentacoes.sql` - Tabela de movimentações
- `V5__create_logs_table.sql` - Tabela de logs de auditoria
- `V6__add_descricao_medicamento.sql` - Campo descrição em medicamentos

### Entidades Principais
- **usuarios**: Usuários do sistema (ADMIN/VENDEDOR)
- **categorias**: Categorias de medicamentos
- **medicamentos**: Medicamentos cadastrados
- **medicamento_imagens**: Imagens dos medicamentos (1-N)
- **clientes**: Clientes cadastrados
- **vendas**: Vendas realizadas
- **itens_venda**: Itens de cada venda
- **movimentacoes_estoque**: Histórico de movimentações
- **alertas**: Alertas gerados automaticamente
- **logs**: Logs de auditoria

---

## 📡 API REST

### Padrões de Resposta
- **200 OK**: Operação bem-sucedida
- **201 Created**: Recurso criado
- **400 Bad Request**: Erro de validação
- **401 Unauthorized**: Não autenticado
- **403 Forbidden**: Sem permissão
- **404 Not Found**: Recurso não encontrado
- **500 Internal Server Error**: Erro do servidor

### Documentação Swagger
- **URL**: `http://localhost:8081/swagger-ui.html`
- **API Docs**: `http://localhost:8081/v3/api-docs`
- **Autenticação**: Botão "Authorize" com token JWT
- **Exemplos**: Todos os endpoints possuem exemplos detalhados de uso

## 👤 Usuário Seed

Ao iniciar a aplicação pela primeira vez, um usuário administrador é criado automaticamente:

- **Email**: `admin@farmacia.com`
- **Senha**: `admin123`
- **Perfil**: `ADMIN`

⚠️ **IMPORTANTE**: Altere a senha após o primeiro acesso em produção!

---

## 🎨 INTERFACE DO USUÁRIO

### Design System
- **Framework CSS**: TailwindCSS
- **Cores Principais**:
  - Navbar/Sidebar: `#2D3345`
  - Modal de Login: `#2D3345`
  - Botões: Gradiente `#2D3345` → `#4A5568`
  - Ações: `#FE5D5C` (vermelho para ações destrutivas)

### Componentes Reutilizáveis
- **ModalComponent**: Modal genérico para formulários
- **LayoutComponent**: Layout principal com navbar e sidebar
- **NotificationService**: Serviço para notificações (SweetAlert2)

### Responsividade
- ✅ Design responsivo com TailwindCSS
- ✅ Menu mobile com hambúrguer
- ✅ Cards adaptáveis para diferentes tamanhos de tela

---

## 🔄 FLUXOS PRINCIPAIS

### Fluxo de Venda
1. Usuário busca cliente por CPF
2. Sistema carrega automaticamente todos os medicamentos válidos
3. Usuário adiciona medicamentos ao carrinho
4. Sistema calcula total automaticamente
5. Usuário finaliza venda
6. Sistema:
   - Cria venda com status CONCLUIDA
   - Reduz estoque dos medicamentos
   - Registra log completo
   - Limpa carrinho

### Fluxo de Cancelamento
1. Usuário clica em "Cancelar Venda"
2. Sistema:
   - Cria venda com status CANCELADA
   - Restaura estoque dos medicamentos
   - Registra log completo
   - Limpa carrinho

### Fluxo de Autenticação
1. Usuário faz login com email e senha
2. Backend valida credenciais
3. Backend gera JWT token
4. Frontend armazena token (criptografado)
5. Interceptor adiciona token em todas as requisições
6. Backend valida token em cada requisição

---

## 🚀 CONFIGURAÇÃO E EXECUÇÃO

### Backend (Spring Boot)
```bash
# Compilar
mvn clean install

# Executar
mvn spring-boot:run

# Porta padrão: 8081
```

### Frontend (Angular)
```bash
# Instalar dependências
npm install

# Executar em desenvolvimento
npm run dev

# Build para produção
npm run build

# Porta padrão: 4200
```

### Banco de Dados
```sql
-- Criar banco
CREATE DATABASE farmacia_db;

-- Flyway executa migrações automaticamente na inicialização
```

---

## 📝 VALIDAÇÕES IMPLEMENTADAS

### Medicamentos
- ✅ Nome obrigatório e único
- ✅ Preço obrigatório e > 0
- ✅ Quantidade em estoque obrigatória e >= 0
- ✅ Data de validade obrigatória e futura
- ✅ Não permite exclusão se já foi vendido

### Categorias
- ✅ Nome obrigatório e único

### Clientes
- ✅ Nome obrigatório
- ✅ CPF obrigatório e único
- ✅ Email obrigatório e válido
- ✅ Data de nascimento obrigatória

### Usuários
- ✅ Nome obrigatório
- ✅ Email obrigatório e válido
- ✅ Senha obrigatória (mínimo 6 caracteres)
- ✅ Perfil (role) obrigatório

---

## 🎯 PONTOS DE DESTAQUE

### 1. Arquitetura Limpa
- Separação clara de responsabilidades (Controller → Service → Repository)
- DTOs para transferência de dados
- Tratamento centralizado de exceções

### 2. Segurança Robusta
- JWT stateless
- RBAC completo
- Validações em múltiplas camadas
- Criptografia de dados sensíveis

### 3. Auditoria Completa
- Logs de todas as operações
- Detalhes completos em JSON
- Data e hora em todos os logs
- Exportação CSV

### 4. UX/UI Moderna
- Interface responsiva
- Feedback visual imediato
- Validações em tempo real
- Notificações claras

### 5. Performance
- Lazy loading de componentes
- Change detection OnPush
- Queries otimizadas
- Índices no banco de dados

### 6. Manutenibilidade
- Código organizado e documentado
- Padrões consistentes
- Swagger com exemplos detalhados
- Migrações versionadas

---

## 📚 DOCUMENTAÇÃO ADICIONAL

- `README-FRONT.md` - Documentação do frontend
- `README-JAVA.md` - Documentação do backend
- `java/COMANDOS.md` - Comandos úteis do backend
- `front/COMANDOS_IMPLEMENTACAO.md` - Comandos do frontend
- `java/EXEMPLO_VENDA_MULTIPLOS_MEDICAMENTOS.md` - Exemplo de venda

---

## 🔮 MELHORIAS FUTURAS (SUGESTÕES)

1. **Testes Automatizados**
   - Unit tests (JUnit, Jest)
   - Integration tests
   - E2E tests (Cypress, Playwright)

2. **Relatórios**
   - Relatórios de vendas
   - Gráficos e dashboards
   - Exportação em PDF

3. **Notificações em Tempo Real**
   - WebSockets para alertas
   - Notificações push

4. **Multi-tenancy**
   - Suporte a múltiplas farmácias
   - Isolamento de dados

5. **API de Integração**
   - Integração com sistemas externos
   - Webhooks

---

## 📞 INFORMAÇÕES TÉCNICAS

### Versões
- **Angular**: 21.0.0
- **Spring Boot**: 3.2.0
- **Java**: 17
- **PostgreSQL**: (versão do servidor)
- **Node.js**: (versão recomendada: 18+)

### Portas
- **Frontend**: 4200
- **Backend**: 8081
- **PostgreSQL**: 5432

### Variáveis de Ambiente
- Backend: `application.yml`
- Frontend: `environment.ts` / `environment.prod.ts`

---

**Desenvolvido com ❤️ para gestão eficiente de farmácias**

---

*Última atualização: Dezembro 2024*

