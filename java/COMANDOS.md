# 📝 Comandos Úteis

## ✅ Maven Wrapper

O projeto utiliza **Maven Wrapper (mvnw)**, então **não é necessário instalar o Maven** ou configurar variáveis de sistema. O Maven será baixado automaticamente na primeira execução.

## Build e Execução

### Windows

```powershell
# Build do projeto
.\mvnw.cmd clean install

# Executar aplicação
.\mvnw.cmd spring-boot:run

# Executar testes
.\mvnw.cmd test

# Gerar JAR
.\mvnw.cmd clean package
java -jar target/farmacia-api-1.0.0.jar
```

### Linux/macOS

```bash
# Build do projeto
./mvnw clean install

# Executar aplicação
./mvnw spring-boot:run

# Executar testes
./mvnw test

# Gerar JAR
./mvnw clean package
java -jar target/farmacia-api-1.0.0.jar
```

## Banco de Dados PostgreSQL

```sql
-- Criar banco de dados
CREATE DATABASE farmacia_db;

-- Conectar ao banco
\c farmacia_db;

-- Verificar tabelas
\dt
```

## Testes com cURL

### Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@farmacia.com", "password": "admin123"}'
```

### Criar Medicamento (requer token)
```bash
# Primeiro faça login e copie o token
TOKEN="seu_token_aqui"

curl -X POST http://localhost:8081/api/medicamentos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "nome": "Dipirona 500mg",
    "preco": 15.50,
    "quantidadeEstoque": 100,
    "validade": "2025-12-31",
    "ativo": true
  }'
```

### Listar Medicamentos
```bash
curl -X GET http://localhost:8081/api/medicamentos \
  -H "Authorization: Bearer $TOKEN"
```

## Variáveis de Ambiente (Windows PowerShell)

```powershell
# Configurar variáveis
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/farmacia_db"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="sua_senha"
$env:JWT_SECRET="sua-chave-secreta"
$env:JWT_EXPIRATION="86400000"
```

## Verificar Versões

```bash
# Java
java -version

# Maven (via wrapper)
# Windows
.\mvnw.cmd -version

# Linux/macOS
./mvnw -version

# PostgreSQL
psql --version
```







