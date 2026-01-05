# 🏥 Sistema de Gestão de Farmácia - API REST

API REST profissional desenvolvida com Spring Boot para gestão completa de farmácia, incluindo controle de estoque, vendas, clientes, medicamentos e alertas automáticos.

## 📋 Índice

- [Stack Tecnológica](#-stack-tecnológica)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação e Configuração](#-instalação-e-configuração)
- [Como Rodar](#-como-rodar)
- [Documentação da API](#-documentação-da-api)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Autenticação e Autorização](#-autenticação-e-autorização)
- [Endpoints Principais](#-endpoints-principais)
- [Usuário Seed](#-usuário-seed)
- [Testes](#-testes)

## 🛠 Stack Tecnológica

### Core
- **Java 17** - Linguagem de programação
- **Spring Boot 3.2.0** - Framework principal
- **Maven** - Gerenciador de dependências

### Segurança
- **Spring Security** - Framework de segurança
- **JWT (JSON Web Token)** - Autenticação stateless
- **BCrypt** - Hash de senhas

### Persistência
- **Spring Data JPA** - Abstração de dados
- **PostgreSQL** - Banco de dados principal
- **Flyway** - Migrations de banco de dados

### Documentação
- **Swagger/OpenAPI 3** - Documentação interativa da API

### Utilitários
- **Lombok** - Redução de boilerplate
- **MapStruct** - Mapeamento de DTOs (configurado)
- **Bean Validation** - Validação de dados

## 📦 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

1. **Java JDK 17**
   ```bash
   # Windows (via winget)
   winget install Oracle.JDK.17
   
   # Verificar instalação
   java -version
   ```

2. **Maven Wrapper (mvnw)**

   ✅ **Não é necessário instalar o Maven!** O projeto já inclui o **Maven Wrapper (mvnw)**, que baixa e gerencia automaticamente a versão correta do Maven sem necessidade de configuração de variáveis de sistema.

   O Maven Wrapper está incluído no projeto nos arquivos:
   - `mvnw` (Linux/macOS)
   - `mvnw.cmd` (Windows)
   - `.mvn/wrapper/` (configurações)

3. **PostgreSQL**
   - Download: https://www.postgresql.org/download/
   - Criar banco de dados:
   ```sql
   CREATE DATABASE farmacia_db;
   ```

4. **Git** (opcional)
   ```bash
   # Windows (via winget)
   winget install Git.Git
   ```

## ⚙️ Instalação e Configuração

### 1. Clone o repositório (se aplicável)
```bash
git clone <url-do-repositorio>
cd projeto-Farmacia/java
```

### 2. Configure o banco de dados

Edite o arquivo `src/main/resources/application.yml` e ajuste as credenciais do PostgreSQL:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/farmacia_db
    username: postgres
    password: sua_senha_aqui
```

### 3. Build do projeto

**No Windows:**
```bash
.\mvnw.cmd clean install
```

**No Linux/macOS:**
```bash
./mvnw clean install
```

## 🚀 Como Rodar

### Opção 1: Maven Wrapper (Recomendado)

**No Windows:**
```bash
.\mvnw.cmd spring-boot:run
```

**No Linux/macOS:**
```bash
./mvnw spring-boot:run
```

### Opção 2: Executar JAR

```bash
# Após o build
java -jar target/farmacia-api-1.0.0.jar
```

### Comandos Úteis

**No Windows:**
```bash
.\mvnw.cmd clean
.\mvnw.cmd package
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
```

**No Linux/macOS:**
```bash
./mvnw clean
./mvnw package
./mvnw spring-boot:run
./mvnw test
```

A aplicação estará disponível em: **http://localhost:8081**

## 📚 Documentação da API

### Swagger UI

Após iniciar a aplicação, acesse:

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **API Docs**: http://localhost:8081/v3/api-docs

O Swagger fornece interface interativa para testar todos os endpoints da API.

## 📁 Estrutura do Projeto

```
com.farmacia
├── config/
│   ├── SecurityConfig.java          # Configuração de segurança
│   ├── SwaggerConfig.java           # Configuração Swagger/OpenAPI
│   ├── JwtConfig.java               # Configuração JWT
│   ├── DataLoader.java              # Seed inicial de dados
│   └── SchedulingConfig.java        # Agendamento de tarefas
│
├── controller/
│   ├── AuthController.java          # Autenticação
│   ├── UsuarioController.java       # Usuários (ADMIN)
│   ├── MedicamentoController.java   # Medicamentos
│   ├── CategoriaController.java     # Categorias (ADMIN)
│   ├── ClienteController.java       # Clientes (ADMIN)
│   ├── EstoqueController.java       # Estoque (ADMIN)
│   ├── VendaController.java         # Vendas
│   └── AlertaController.java        # Alertas
│
├── service/
│   ├── AuthService.java
│   ├── UsuarioService.java
│   ├── MedicamentoService.java
│   ├── CategoriaService.java
│   ├── ClienteService.java
│   ├── VendaService.java
│   ├── EstoqueService.java
│   └── AlertaService.java
│
├── repository/
│   └── [Interfaces JPA Repository]
│
├── domain/
│   ├── entity/                      # Entidades JPA
│   ├── enums/                       # Enumeradores
│   └── dto/                         # Data Transfer Objects
│
├── security/
│   ├── JwtFilter.java               # Filtro JWT
│   ├── JwtService.java              # Serviço JWT
│   └── UserDetailsServiceImpl.java  # UserDetailsService
│
└── exception/
    ├── GlobalExceptionHandler.java  # Tratamento global de exceções
    └── BusinessException.java       # Exceção de negócio
```

## 🔐 Autenticação e Autorização

### Autenticação

A API usa **JWT (JSON Web Token)** para autenticação stateless.

**Fluxo:**
1. Realizar login em `POST /api/auth/login`
2. Receber o token JWT
3. Incluir o token no header das requisições: `Authorization: Bearer <token>`

### Autorização (RBAC)

A API implementa **Role-Based Access Control** com dois perfis:

| Perfil | Descrição |
|--------|-----------|
| **ADMIN** | Acesso total ao sistema |
| **VENDEDOR** | Acesso limitado a vendas e visualização |

### Matriz de Permissões

| Endpoint | ADMIN | VENDEDOR |
|----------|-------|----------|
| `/api/usuarios/**` | ✅ | ❌ |
| `/api/medicamentos` (GET) | ✅ | ✅ |
| `/api/medicamentos` (POST/PUT/DELETE) | ✅ | ❌ |
| `/api/categorias/**` | ✅ | ❌ |
| `/api/clientes/**` | ✅ | ❌ |
| `/api/vendas/**` | ✅ | ✅ |
| `/api/estoque/**` | ✅ | ❌ |
| `/api/alertas` (GET) | ✅ | ✅ |

### Exemplo de Uso

```bash
# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@farmacia.com",
    "password": "admin123"
  }'

# Resposta
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "usuario": { ... }
}

# Usar o token em requisições
curl -X GET http://localhost:8081/api/medicamentos \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## 🎯 Endpoints Principais

### Autenticação
- `POST /api/auth/login` - Realizar login

### Usuários (ADMIN)
- `GET /api/usuarios` - Listar usuários
- `GET /api/usuarios/{id}` - Buscar usuário
- `POST /api/usuarios` - Criar usuário
- `PUT /api/usuarios/{id}` - Atualizar usuário
- `DELETE /api/usuarios/{id}` - Deletar usuário

### Medicamentos
- `GET /api/medicamentos` - Listar todos
- `GET /api/medicamentos/ativos` - Listar ativos
- `GET /api/medicamentos/{id}` - Buscar por ID
- `POST /api/medicamentos` - Criar (ADMIN)
- `PUT /api/medicamentos/{id}` - Atualizar (ADMIN)
- `DELETE /api/medicamentos/{id}` - Deletar (ADMIN)

### Vendas
- `GET /api/vendas` - Listar vendas
- `GET /api/vendas/{id}` - Buscar venda
- `POST /api/vendas` - Criar venda
- `POST /api/vendas/{id}/cancelar` - Cancelar venda

### Alertas
- `GET /api/alertas` - Listar todos
- `GET /api/alertas/nao-lidos` - Listar não lidos
- `PUT /api/alertas/{id}/ler` - Marcar como lido

> Consulte a documentação Swagger para ver todos os endpoints e exemplos de requisições.

## 👤 Usuário Seed

Ao iniciar a aplicação pela primeira vez, um usuário administrador é criado automaticamente:

- **Email**: `admin@farmacia.com`
- **Senha**: `admin123`
- **Perfil**: `ADMIN`

⚠️ **IMPORTANTE**: Altere a senha após o primeiro acesso em produção!

## 🧪 Testes

### Executar testes unitários

**No Windows:**
```bash
.\mvnw.cmd test
```

**No Linux/macOS:**
```bash
./mvnw test
```

### Executar testes com cobertura (requer plugin adicional)

**No Windows:**
```bash
.\mvnw.cmd test jacoco:report
```

**No Linux/macOS:**
```bash
./mvnw test jacoco:report
```

## 🔧 Configurações Adicionais

### Variáveis de Ambiente

Você pode sobrescrever configurações usando variáveis de ambiente:

```bash
# Windows PowerShell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/farmacia_db"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="senha"
$env:JWT_SECRET="sua-chave-secreta"
$env:JWT_EXPIRATION="86400000"
```

### Configuração JWT

No arquivo `application.yml`:

```yaml
jwt:
  secret: sua-chave-super-secreta
  expiration: 86400000  # 24 horas em milissegundos
```

## 🚨 Funcionalidades Principais

### Gestão de Estoque
- Controle de entrada e saída
- Histórico de movimentações
- Alertas de estoque baixo

### Gestão de Vendas
- Criação de vendas com múltiplos itens
- Validação automática:
  - Estoque disponível
  - Validade do medicamento
  - Status ativo
- Cancelamento com estorno de estoque

### Sistema de Alertas
- **Estoque baixo**: Alertas automáticos quando quantidade < 10 unidades
- **Validade próxima**: Alertas para medicamentos que vencem em até 30 dias
- Execução automática diária às 8h (via @Scheduled)

## 📝 Notas de Desenvolvimento

### Uso com Cursor IDE

Aproveite os recursos de IA do Cursor:

**Exemplos de prompts úteis:**
- "Crie o controller REST para Medicamento com validações, tratamento de erro e Swagger"
- "Crie testes unitários para VendaService considerando regras de estoque"
- "Adicione endpoint para relatório de vendas por período"

### Boas Práticas Implementadas

✅ **Separação de responsabilidades** (Controller → Service → Repository)  
✅ **DTOs para isolamento de entidades**  
✅ **Tratamento centralizado de exceções**  
✅ **Validação de dados com Bean Validation**  
✅ **Transações gerenciadas pelo Spring**  
✅ **Segurança baseada em roles**  
✅ **Documentação automática com Swagger**  
✅ **Migrations versionadas com Flyway**

## 🐛 Troubleshooting

### Erro de conexão com banco de dados
- Verifique se o PostgreSQL está rodando
- Confirme as credenciais no `application.yml`
- Certifique-se que o banco `farmacia_db` existe

### Erro de porta em uso
- Altere a porta no `application.yml`:
  ```yaml
  server:
    port: 8081
  ```

### Token JWT inválido
- Verifique se está incluindo o header `Authorization: Bearer <token>`
- Certifique-se que o token não expirou (padrão: 24 horas)

## 📄 Licença

Este projeto está sob a licença Apache 2.0.

## 👥 Contribuição

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou pull requests.

---

**Desenvolvido com ❤️ usando Spring Boot e Cursor IDE**

