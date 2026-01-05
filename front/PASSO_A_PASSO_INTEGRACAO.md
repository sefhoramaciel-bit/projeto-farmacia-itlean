# 🎯 Passo a Passo Completo - Integração Frontend ↔ Backend

Este documento fornece instruções **meticulosas e passo a passo** para implementar a integração entre o frontend Angular e o backend Spring Boot.

---

## 📋 ÍNDICE

1. [Verificação de Pré-requisitos](#1-verificação-de-pré-requisitos)
2. [Preparação do Backend](#2-preparação-do-backend)
3. [Preparação do Frontend](#3-preparação-do-frontend)
4. [Execução e Teste](#4-execução-e-teste)
5. [Verificação de Funcionamento](#5-verificação-de-funcionamento)
6. [Solução de Problemas](#6-solução-de-problemas)

---

## 1. VERIFICAÇÃO DE PRÉ-REQUISITOS

### 1.1. Verificar Java 17

Abra o terminal e execute:

```bash
java -version
# ou
java --version
```

**Resultado esperado:** `openjdk version "17"` ou similar

**Se não estiver instalado:** Instale Java 17

### 1.2. Verificar Maven

```bash
mvn -version
# ou
mvn --version
```

**Resultado esperado:** Versão do Maven (ex: `Apache Maven 3.9.x`)

**Se não estiver instalado:** Instale Maven

### 1.3. Verificar Node.js

```bash
# Windows (use dois hífens ou -v)
node --version
# ou
node -v

# Linux/Mac (também pode usar --version ou -v)
node --version
# ou
node -v
```

**Resultado esperado:** Versão do Node (ex: `v18.x.x` ou superior)

**Se não estiver instalado:** Instale Node.js do site oficial

### 1.4. Verificar PostgreSQL

```bash
# Windows (PowerShell)
Get-Service -Name postgresql*

# Linux/Mac
sudo systemctl status postgresql
```

**Resultado esperado:** Serviço rodando

**Se não estiver instalado:** Instale PostgreSQL

### ✅ Checklist

- [ ] Java 17 instalado
- [ ] Maven instalado
- [ ] Node.js instalado (v18+)
- [ ] PostgreSQL instalado e rodando

---

## 2. PREPARAÇÃO DO BACKEND

### 2.1. Navegar para a pasta do backend

```bash
cd java
```

### 2.2. Verificar se o banco de dados existe

```bash
# Conectar ao PostgreSQL
psql -U postgres
```

Dentro do psql, execute:

```sql
-- Listar bancos de dados
\l

-- Se farmacia_db não existir, criar:
CREATE DATABASE farmacia_db;

-- Sair do psql
\q
```

### 2.3. Verificar configuração do banco de dados

Abra o arquivo: `java/src/main/resources/application.yml`

Verifique as configurações:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/farmacia_db
    username: postgres
    password: 1104  # Ajuste se necessário
```

**Se sua senha do PostgreSQL for diferente, altere a linha `password` acima.**

### 2.4. Compilar o backend (opcional, mas recomendado)

```bash
mvn clean install
```

**Aguarde até ver:** `BUILD SUCCESS`

### ✅ Checklist

- [ ] Pasta `java` acessada
- [ ] Banco de dados `farmacia_db` criado
- [ ] Configurações do banco verificadas
- [ ] Backend compilado com sucesso

---

## 3. PREPARAÇÃO DO FRONTEND

### 3.1. Navegar para a pasta do frontend

Em um **novo terminal**, execute:

```bash
cd front
```

### 3.2. Instalar dependências

```bash
npm install
```

**Se houver erro de conflito de dependências, use:**
```bash
npm install --legacy-peer-deps
```

**Aguarde até ver:** `added XXX packages`

### 3.3. Verificar arquivo de ambiente

Abra o arquivo: `front/src/environments/environment.ts`

Deve conter:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8081/api'
};
```

**Se o backend estiver em outra porta ou servidor, altere a URL acima.**

### ✅ Checklist

- [ ] Pasta `front` acessada
- [ ] Dependências instaladas (`npm install`)
- [ ] Arquivo `environment.ts` verificado

---

## 4. EXECUÇÃO E TESTE

### 4.1. Iniciar o Backend

No terminal da pasta `java`, execute:

```bash
mvn spring-boot:run
```

**Aguarde até ver:**
```
Started FarmaciaApplication in X.XXX seconds
```

**Mantenha este terminal aberto!**

### 4.2. Verificar se o Backend está rodando

Abra o navegador e acesse:

- **Swagger UI:** http://localhost:8081/swagger-ui.html

**Se conseguir acessar, o backend está funcionando!** ✅

### 4.3. Iniciar o Frontend

Em um **novo terminal**, na pasta `front`, execute:

```bash
npm run dev
```

**Aguarde até ver:**
```
Application server started on http://localhost:4200
```

**Mantenha este terminal aberto também!**

### 4.4. Acessar a aplicação

Abra o navegador e acesse: **http://localhost:4200**

Você deve ser redirecionado automaticamente para a tela de login.

### 4.5. Fazer Login

Na tela de login, use:

- **Email:** `admin@farmacia.com`
- **Senha:** `admin123`

Clique em **"Entrar"**.

**Se tudo estiver funcionando, você será redirecionado para a página inicial!** ✅

### ✅ Checklist

- [ ] Backend iniciado (terminal mostra "Started...")
- [ ] Swagger UI acessível
- [ ] Frontend iniciado (terminal mostra "Application server started...")
- [ ] Aplicação acessível em http://localhost:4200
- [ ] Login funcionando

---

## 5. VERIFICAÇÃO DE FUNCIONAMENTO

### 5.1. Verificar Token JWT no Local Storage

1. Abra o DevTools do navegador (pressione `F12`)
2. Vá na aba **"Application"** (ou **"Aplicativo"**)
3. No menu lateral, expanda **"Local Storage"**
4. Clique em `http://localhost:4200`
5. Você deve ver:
   - `jwt_token`: token JWT (string longa)
   - `currentUser`: objeto JSON com dados do usuário

**Se essas chaves existirem, a autenticação está funcionando!** ✅

### 5.2. Verificar Requisições HTTP

1. Com o DevTools aberto (F12), vá na aba **"Network"** (ou **"Rede"**)
2. Na tela da aplicação, clique em **"Medicamentos"** no menu
3. Na aba Network, você deve ver uma requisição:
   - **URL:** `http://localhost:8081/api/medicamentos`
   - **Método:** `GET`
   - **Status:** `200 OK`
4. Clique na requisição e verifique:
   - **Headers > Request Headers:** deve conter `Authorization: Bearer ...`
   - **Response:** deve mostrar um array JSON com medicamentos

**Se a requisição aparecer com status 200, a integração está funcionando!** ✅

### 5.3. Testar Funcionalidades

#### Teste 1: Listar Medicamentos

1. Clique em **"Medicamentos"** no menu
2. Você deve ver a lista de medicamentos carregando
3. Se aparecerem medicamentos (mesmo que vazia), está funcionando ✅

#### Teste 2: Listar Categorias

1. Clique em **"Categorias"** no menu
2. Você deve ver a lista de categorias
3. Se aparecerem categorias, está funcionando ✅

#### Teste 3: Criar uma Categoria (opcional)

1. Na tela de Categorias, clique em **"Adicionar Categoria"**
2. Preencha o nome: `Teste`
3. Clique em **"Salvar"**
4. Se aparecer mensagem de sucesso, está funcionando ✅

### ✅ Checklist

- [ ] Token JWT salvo no Local Storage
- [ ] Usuário salvo no Local Storage
- [ ] Requisições HTTP aparecem no Network tab
- [ ] Header `Authorization` presente nas requisições
- [ ] Status 200 nas requisições
- [ ] Dados sendo carregados do backend

---

## 6. SOLUÇÃO DE PROBLEMAS

### Problema 1: Backend não inicia

**Sintomas:**
- Erro ao executar `mvn spring-boot:run`
- Mensagem de erro sobre banco de dados

**Solução:**
1. Verifique se o PostgreSQL está rodando
2. Verifique se o banco `farmacia_db` foi criado
3. Verifique as credenciais em `application.yml`

### Problema 2: Frontend não inicia

**Sintomas:**
- Erro ao executar `npm run dev`
- Mensagem sobre dependências faltando

**Solução:**
```bash
cd front
rm -rf node_modules package-lock.json  # Linux/Mac
# ou no Windows PowerShell:
Remove-Item -Recurse -Force node_modules, package-lock.json

npm install
```

### Problema 3: Erro CORS

**Sintomas:**
- Erro no console: `Access to XMLHttpRequest... has been blocked by CORS policy`

**Solução:**
- O backend já está configurado para aceitar requisições de qualquer origem
- Se ainda assim houver erro, verifique se o backend está rodando na porta 8081

### Problema 4: Erro 401 Unauthorized

**Sintomas:**
- Requisições retornam status 401
- Mensagem de não autorizado

**Solução:**
1. Faça logout e login novamente
2. Verifique se o token está no Local Storage
3. Verifique se o interceptor está configurado em `index.tsx`

### Problema 5: Erro 403 Forbidden

**Sintomas:**
- Requisições retornam status 403
- Mensagem de acesso negado

**Solução:**
- Verifique se o usuário tem permissão adequada (role)
- Apenas ADMIN pode fazer certas operações (criar/editar/deletar)

### Problema 6: Erro 500 Internal Server Error

**Sintomas:**
- Requisições retornam status 500
- Erro no servidor

**Solução:**
1. Verifique os logs do backend no terminal
2. Verifique se o PostgreSQL está rodando
3. Verifique se as tabelas foram criadas (Flyway migrations)

### Problema 7: Dados não aparecem

**Sintomas:**
- Requisições retornam 200, mas a lista está vazia

**Solução:**
- Isso é normal se não houver dados no banco
- Crie alguns registros manualmente pelo Swagger ou pela aplicação

---

## 📚 DOCUMENTAÇÃO ADICIONAL

- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **API Docs:** http://localhost:8081/api-docs
- **Guia Completo:** Ver `GUIA_INTEGRACAO_BACKEND.md`
- **Comandos Rápidos:** Ver `COMANDOS_IMPLEMENTACAO.md`
- **Resumo:** Ver `RESUMO_INTEGRACAO.md`

---

## ✅ CHECKLIST FINAL

- [ ] Todos os pré-requisitos instalados
- [ ] Backend compilado e rodando
- [ ] Frontend com dependências instaladas
- [ ] Ambos os servidores rodando
- [ ] Login funcionando
- [ ] Token JWT sendo salvo
- [ ] Requisições HTTP funcionando
- [ ] Dados sendo carregados
- [ ] Todas as funcionalidades testadas

---

**🎉 PARABÉNS! Se você chegou até aqui, a integração está completa e funcionando!**

