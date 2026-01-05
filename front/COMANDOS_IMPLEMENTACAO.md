# 🚀 Comandos para Implementação - Integração Frontend ↔ Backend

Este documento lista **todos os comandos** que você precisa executar para fazer a integração funcionar.

## 📋 Pré-requisitos

1. Node.js instalado (v18 ou superior)
2. Java 17 instalado
3. Maven instalado
4. PostgreSQL instalado e rodando

## 🔧 PASSO 1: Verificar/Instalar Dependências do Frontend

```bash
# Navegar para a pasta do frontend
cd front

# Instalar dependências
npm install

# Se houver erro de conflito de dependências, use:
npm install --legacy-peer-deps
```

## 🗄️ PASSO 2: Configurar e Iniciar o Backend

### 2.1. Navegar para a pasta do backend

```bash
cd java
```

### 2.2. Verificar se o PostgreSQL está rodando

```bash
# Windows (PowerShell)
Get-Service -Name postgresql*

# Linux/Mac
sudo systemctl status postgresql
```

### 2.3. Criar o banco de dados (se ainda não existir)

```bash
# Conectar ao PostgreSQL
psql -U postgres

# Criar banco de dados
CREATE DATABASE farmacia_db;

# Sair do psql
\q
```

### 2.4. Executar o backend

```bash
# Compilar e executar
mvn clean spring-boot:run
```

**Aguarde até ver:** `Started FarmaciaApplication in X seconds`

## 🌐 PASSO 3: Verificar se o Backend está Rodando

Abra o navegador e acesse:

- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **API Docs:** http://localhost:8081/api-docs

Se conseguir acessar, o backend está funcionando! ✅

## ⚛️ PASSO 4: Iniciar o Frontend

### 4.1. Em um novo terminal, navegar para a pasta do frontend

```bash
cd front
```

### 4.2. Iniciar o servidor de desenvolvimento

```bash
npm run dev
```

**Aguarde até ver:** `Application server started on http://localhost:3000`

## 🔐 PASSO 5: Testar o Login

1. Acesse: http://localhost:3000
2. Você será redirecionado para a tela de login
3. Use as credenciais:
   - **Email:** `admin@farmacia.com`
   - **Senha:** `admin123`
4. Clique em "Entrar"

**Se o login funcionar, você será redirecionado para a página inicial!** ✅

## 🧪 PASSO 6: Testar Funcionalidades

### Teste 1: Listar Medicamentos

1. Clique em "Medicamentos" no menu
2. Você deve ver a lista de medicamentos carregando do backend

### Teste 2: Criar um Medicamento

1. Na tela de Medicamentos, clique em "Adicionar Medicamento"
2. Preencha o formulário
3. Clique em "Salvar"
4. O medicamento deve ser criado no backend

### Teste 3: Listar Categorias

1. Clique em "Categorias" no menu
2. Você deve ver as categorias carregando do backend

### Teste 4: Criar uma Venda

1. Clique em "Vendas" no menu
2. Busque um cliente pelo CPF
3. Adicione medicamentos ao carrinho
4. Finalize a venda
5. A venda deve ser criada no backend

## 🔍 PASSO 7: Verificar no Console do Navegador

1. Abra o DevTools (F12)
2. Vá na aba "Console"
3. Faça algumas operações
4. Você deve ver logs das requisições HTTP

### Verificar Requisições HTTP

1. Abra o DevTools (F12)
2. Vá na aba "Network"
3. Faça uma operação (ex: listar medicamentos)
4. Você deve ver uma requisição para `GET http://localhost:8081/api/medicamentos`
5. Clique na requisição e verifique:
   - Status: 200 OK
   - Headers: `Authorization: Bearer ...` (deve estar presente)
   - Response: dados JSON do backend

## 🐛 Troubleshooting

### Problema: "Cannot GET /"

**Solução:** O frontend não está rodando. Execute:
```bash
cd front
npm run dev
```

### Problema: "Network Error" ou "Failed to fetch"

**Solução:** O backend não está rodando. Execute:
```bash
cd java
mvn spring-boot:run
```

### Problema: "401 Unauthorized"

**Solução:** 
1. Faça logout e login novamente
2. Verifique se o token está sendo salvo no localStorage:
   - Abra DevTools (F12)
   - Vá em "Application" > "Local Storage"
   - Verifique se existe a chave `jwt_token`

### Problema: "CORS Error"

**Solução:** O backend já está configurado para aceitar requisições do frontend. Se ainda assim houver erro, verifique se o backend está rodando na porta 8081.

### Problema: "500 Internal Server Error"

**Solução:**
1. Verifique os logs do backend no terminal
2. Verifique se o PostgreSQL está rodando
3. Verifique se o banco de dados foi criado corretamente

## 📊 Comandos Rápidos de Referência

```bash
# Iniciar Backend
cd java
mvn spring-boot:run

# Iniciar Frontend (em outro terminal)
cd front
npm run dev

# Parar Backend
Ctrl + C (no terminal do backend)

# Parar Frontend
Ctrl + C (no terminal do frontend)

# Limpar build do backend
cd java
mvn clean

# Reinstalar dependências do frontend
cd front
npm install
```

**Nota para Windows PowerShell:** Use comandos separados ou ponto e vírgula (`;`) ao invés de `&&`

## ✅ Checklist de Verificação

- [ ] PostgreSQL está rodando
- [ ] Banco de dados `farmacia_db` foi criado
- [ ] Backend está rodando em http://localhost:8081
- [ ] Swagger UI está acessível
- [ ] Frontend está rodando em http://localhost:3000
- [ ] Login funciona
- [ ] Token JWT é salvo no localStorage
- [ ] Requisições HTTP estão sendo feitas
- [ ] Dados estão sendo carregados do backend

---

**Pronto! Se todos os passos foram executados com sucesso, a integração está funcionando!** 🎉

