# Sistema de Farmácia - Frontend Angular

Este projeto é a interface de usuário (frontend) para o Sistema de Gestão de Farmácia. Foi construído com Angular, utilizando as melhores práticas modernas, como componentes standalone, signals para gerenciamento de estado e Tailwind CSS para estilização.

## Descrição do Projeto

A aplicação fornece uma interface rica e reativa para interagir com a API de backend da farmácia. Permite o gerenciamento de:
- Medicamentos (CRUD, ativação/inativação)
- Categorias
- Clientes
- Estoque (entradas e saídas)
- Vendas (simulando um PDV/e-commerce)
- Alertas de estoque baixo e validade próxima.

O sistema possui dois níveis de acesso: `Admin` (acesso total) e `Vendedor` (focado em vendas e consultas).

## Pré-requisitos

Antes de começar, certifique-se de que você tem o seguinte instalado:
- **Node.js**: Versão 18.x ou superior.
- **npm** (Node Package Manager): Geralmente vem com o Node.js.
- **Angular CLI**: `npm install -g @angular/cli`

## Como Rodar a Aplicação

Siga os passos abaixo para instalar as dependências e iniciar o servidor de desenvolvimento.

1.  **Clonar o Repositório**
    ```bash
    git clone [URL_DO_SEU_REPOSITORIO_GIT]
    cd [NOME_DA_PASTA_DO_PROJETO]
    ```

2.  **Instalar Dependências**
    Execute o comando abaixo na raiz do projeto para instalar todos os pacotes necessários definidos no `package.json`.
    ```bash
    npm install
    ```
    *(Nota: Como este é um Applet, não há um `package.json` gerado, mas em um ambiente de desenvolvimento padrão, este seria o passo.)*

3.  **Configurar a API de Backend**
    Para que o frontend se comunique com o backend Java, é necessário configurar a URL base da API.

    - Abra o arquivo: `src/services/api.service.ts`
    - Encontre a variável `API_URL` e altere seu valor para a URL onde seu backend Spring Boot está rodando.
      ```typescript
      // src/services/api.service.ts
      private readonly API_URL = 'http://localhost:8081/api'; // <-- Altere para a URL do seu backend
      ```

4.  **Iniciar o Servidor de Desenvolvimento**
    Execute o comando abaixo para compilar a aplicação e iniciar um servidor de desenvolvimento local.
    ```bash
    ng serve
    ```
    - A aplicação será servida em `http://localhost:4200/`.
    - O servidor recarregará automaticamente a página sempre que você alterar um arquivo do projeto.

## Estrutura do Projeto

- **/src/app**: Contém os componentes principais, rotas e configuração da aplicação.
- **/src/assets**: Armazena arquivos estáticos, como o logo.
- **/src/components**: Componentes reutilizáveis, como o layout principal e modais.
- **/src/pages**: Componentes que representam as páginas da aplicação (Login, Home, Medicamentos, etc.).
- **/src/services**: Serviços responsáveis pela lógica de negócio, comunicação com a API e notificações.
- **/src/models**: Definições de tipos e interfaces (TypeScript).

## Exemplos de Endpoints (Backend)

O frontend está preparado para consumir os seguintes endpoints (conforme definido nos requisitos):

- **Medicamentos**: `GET /medicamentos`, `POST /medicamentos`, `PUT /medicamentos/{id}`
- **Clientes**: `GET /clientes`, `POST /clientes`, `PUT /clientes/{id}`
- **Vendas**: `POST /vendas`, `GET /vendas/{id}`
- ... e todos os outros definidos na especificação.
