# 📝 Comandos Úteis

## ⚠️ Instalação do Maven (Windows)

Se você receber o erro `'mvn' não é reconhecido como comando`, siga estes passos:

### Opção 1: Instalar Maven via Chocolatey (Recomendado)
```powershell
# Instalar Chocolatey (se não tiver)
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Instalar Maven
choco install maven
```

### Opção 2: Instalação Manual
1. Baixe o Maven: https://maven.apache.org/download.cgi (apache-maven-3.9.x-bin.zip)
2. Extraia para `C:\Program Files\Apache\maven`
3. Adicione ao PATH:
   - Pressione `Win + R`, digite `sysdm.cpl` e pressione Enter
   - Aba "Avançado" → "Variáveis de Ambiente"
   - Em "Variáveis do sistema", edite "Path"
   - Adicione: `C:\Program Files\Apache\maven\bin`
4. Abra um novo terminal e verifique: `mvn -version`

### Opção 3: Usar Maven Wrapper (se disponível)
```powershell
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

## Build e Execução

```bash
# Build do projeto
mvn clean install

# Executar aplicação
mvn spring-boot:run

# Executar testes
mvn test

# Gerar JAR
mvn clean package
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

# Maven
mvn -version

# PostgreSQL
psql --version
```







