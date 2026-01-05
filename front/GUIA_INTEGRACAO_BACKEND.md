# 🔗 Guia Completo de Integração Frontend ↔ Backend

Este documento detalha passo a passo como integrar o frontend Angular com o backend Spring Boot.

## 📋 Pré-requisitos

1. **Backend rodando** na porta `8081`
2. **Frontend** com todas as dependências instaladas
3. **PostgreSQL** configurado e rodando

## 🚀 Passo 1: Instalar Dependências do Frontend

```bash
cd front
npm install
```

## 🔧 Passo 2: Configurar Variável de Ambiente

O arquivo `src/environments/environment.ts` já está configurado com:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8081/api'
};
```

**Se o backend estiver em outra porta ou servidor, altere a URL acima.**

## 📦 Passo 3: Estrutura de Arquivos Criados

A integração foi feita criando os seguintes arquivos:

### ✅ Arquivos Criados/Atualizados:

1. **`src/environments/environment.ts`** - Configuração da API
2. **`src/environments/environment.prod.ts`** - Configuração para produção
3. **`src/interceptors/auth.interceptor.ts`** - Interceptor para adicionar JWT
4. **`src/models/types.ts`** - Atualizado com interfaces do backend
5. **`src/services/auth.service.ts`** - Serviço de autenticação
6. **`src/services/medicines.service.ts`** - Serviço de medicamentos
7. **`src/services/categories.service.ts`** - Serviço de categorias
8. **`src/services/customers.service.ts`** - Serviço de clientes
9. **`src/services/stock.service.ts`** - Serviço de estoque
10. **`src/services/sales.service.ts`** - Serviço de vendas
11. **`src/services/logs.service.ts`** - Serviço de logs
12. **`src/services/alerts.service.ts`** - Serviço de alertas
13. **`src/services/api.service.ts`** - Serviço unificado (atualizado)
14. **`src/services/log.service.ts`** - Serviço de log simplificado
15. **`index.tsx`** - Configurado com interceptor HTTP

## 🔐 Passo 4: Como Funciona a Autenticação

### Login
O login é feito através do endpoint `POST /api/auth/login`:

```typescript
// Email padrão: admin@farmacia.com
// Senha padrão: admin123
```

O token JWT é armazenado automaticamente no `localStorage` com a chave `jwt_token`.

### Interceptor HTTP
Todas as requisições HTTP (exceto login) têm o header `Authorization: Bearer <token>` adicionado automaticamente pelo interceptor.

## 📝 Passo 5: Atualizar Componentes (Se Necessário)

A maioria dos componentes já está usando o `ApiService` que foi atualizado para usar os novos serviços. No entanto, alguns componentes podem precisar de pequenos ajustes.

### Componentes que já funcionam:
- ✅ Login
- ✅ Medicamentos
- ✅ Categorias
- ✅ Clientes
- ✅ Estoque
- ✅ Vendas
- ✅ Logs

## 🧪 Passo 6: Testar a Integração

### 6.1. Iniciar o Backend

```bash
cd java
mvn spring-boot:run
```

**Verifique se o backend está rodando em:** `http://localhost:8081`

### 6.2. Iniciar o Frontend

```bash
cd front
npm run dev
```

**O frontend deve iniciar em:** `http://localhost:3000` (ou porta configurada)

### 6.3. Fazer Login

1. Acesse `http://localhost:3000`
2. Você será redirecionado para `/login`
3. Use as credenciais:
   - **Email:** `admin@farmacia.com`
   - **Senha:** `admin123`
4. Após o login, você será redirecionado para `/inicio`

## 🔍 Passo 7: Verificar se Está Funcionando

### Verificar no Console do Navegador (F12)

Você deve ver requisições HTTP sendo feitas para `http://localhost:8081/api/...`

### Verificar no Network Tab

1. Abra o DevTools (F12)
2. Vá na aba "Network"
3. Faça uma operação (ex: listar medicamentos)
4. Você deve ver uma requisição para `GET http://localhost:8081/api/medicamentos`
5. Verifique se o header `Authorization: Bearer ...` está presente

## 📊 Passo 8: Endpoints Disponíveis

### Autenticação
- `POST /api/auth/login` - Login

### Medicamentos
- `GET /api/medicamentos` - Listar todos
- `GET /api/medicamentos/ativos` - Listar ativos
- `GET /api/medicamentos/{id}` - Buscar por ID
- `POST /api/medicamentos` - Criar
- `PUT /api/medicamentos/{id}` - Atualizar
- `DELETE /api/medicamentos/{id}` - Deletar
- `PATCH /api/medicamentos/{id}/status` - Atualizar status

### Categorias
- `GET /api/categorias` - Listar todas
- `GET /api/categorias/{id}` - Buscar por ID
- `POST /api/categorias` - Criar
- `PUT /api/categorias/{id}` - Atualizar
- `DELETE /api/categorias/{id}` - Deletar

### Clientes
- `GET /api/clientes` - Listar todos
- `GET /api/clientes/{id}` - Buscar por ID
- `POST /api/clientes` - Criar
- `PUT /api/clientes/{id}` - Atualizar
- `DELETE /api/clientes/{id}` - Deletar

### Estoque
- `POST /api/estoque/entrada` - Entrada de estoque
- `POST /api/estoque/saida` - Saída de estoque
- `GET /api/estoque/{medicamentoId}` - Consultar estoque

### Vendas
- `GET /api/vendas` - Listar todas
- `GET /api/vendas/{id}` - Buscar por ID
- `GET /api/vendas/cliente/{clienteId}` - Vendas por cliente
- `POST /api/vendas` - Criar venda
- `POST /api/vendas/{id}/cancelar` - Cancelar venda

### Logs
- `GET /api/logs` - Últimos 100 logs
- `GET /api/logs/export` - Exportar CSV

### Alertas
- `GET /api/alertas` - Listar todos
- `GET /api/alertas/nao-lidos` - Listar não lidos
- `GET /api/alertas/estoque-baixo` - Estoque baixo
- `GET /api/alertas/validade-proxima` - Validade próxima
- `PUT /api/alertas/{id}/ler` - Marcar como lido

## ⚠️ Possíveis Problemas e Soluções

### Problema 1: CORS Error

**Erro:** `Access to XMLHttpRequest at 'http://localhost:8081/api/...' from origin 'http://localhost:3000' has been blocked by CORS policy`

**Solução:** O backend já deve estar configurado para aceitar requisições do frontend. Se não estiver, verifique a classe `CorsConfig` no backend.

### Problema 2: 401 Unauthorized

**Erro:** `401 Unauthorized`

**Solução:**
1. Verifique se o token JWT está sendo salvo no `localStorage`
2. Verifique se o interceptor está adicionando o header `Authorization`
3. Faça login novamente

### Problema 3: 403 Forbidden

**Erro:** `403 Forbidden`

**Solução:**
1. Verifique se o usuário tem permissão (role) adequada
2. Apenas ADMIN pode fazer certas operações (criar/editar/deletar)

### Problema 4: Backend não está rodando

**Erro:** `Failed to fetch` ou `Network error`

**Solução:**
1. Verifique se o backend está rodando: `http://localhost:8081`
2. Verifique se o PostgreSQL está rodando
3. Verifique os logs do backend

### Problema 5: Formato de Data

**Erro:** Datas não estão sendo formatadas corretamente

**Solução:** O backend espera datas no formato brasileiro `dd/MM/yyyy`. Os componentes do frontend devem enviar as datas já formatadas ou usar o formato ISO `yyyy-MM-dd`.

## 🎯 Próximos Passos

1. ✅ Integração básica completa
2. 🔄 Testar todas as funcionalidades
3. 🎨 Ajustar formatos de data nos formulários
4. 🖼️ Implementar upload de imagens (medicamentos e avatares)
5. 📱 Adicionar tratamento de erros mais robusto
6. 🔔 Melhorar feedback visual para o usuário

## 📚 Documentação Adicional

- **Swagger UI:** `http://localhost:8081/swagger-ui.html`
- **API Docs:** `http://localhost:8081/api-docs`

## ✅ Checklist Final

- [ ] Backend rodando na porta 8081
- [ ] Frontend rodando na porta 3000
- [ ] Login funcionando
- [ ] Token JWT sendo salvo
- [ ] Interceptor HTTP configurado
- [ ] Requisições HTTP funcionando
- [ ] Todas as telas carregando dados do backend

---

**Pronto! A integração está completa.** 🎉






