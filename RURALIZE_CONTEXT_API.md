# Ruralize API - Contexto e Arquitetura

Este documento descreve detalhadamente o funcionamento, arquitetura e regras de negócio da **Ruralize API**, o núcleo de inteligência (backend) do ecossistema Ruralize.

## 1. Visão Geral
A Ruralize API foi construída utilizando o framework **NestJS** (Node.js). Ela é responsável por orquestrar toda a lógica de negócios para plataformas B2B do agronegócio, integrando os sistemas clientes:
- **RuralizeShop:** Plataforma Web (Gestão / Backoffice).
- **RuralizeSeller:** Aplicativo Mobile (Operacional e Vendas de campo).

## 2. Stack Tecnológico e Infraestrutura
- **Framework:** NestJS (v11).
- **Linguagem:** TypeScript.
- **Banco de Dados:** Google Firebase Firestore (NoSQL).
- **Autenticação:** Firebase Admin Auth (JWT).
- **Armazenamento de Mídia:** Cloudinary (upload e otimização de imagens).

## 3. Estrutura do Banco de Dados (Firestore)
O sistema utiliza uma modelagem hierárquica focada no isolamento dos dados (multi-tenant por empresa):

### Coleção: `users` (Empresas)
Esta é a coleção raiz. Cada documento representa uma Empresa/Produtor Rural.
- **Campos Principais:** `email`, `displayName`, `cnpj`, `createdAt`, `updatedAt`.

### Subcoleção: `users/{empresaId}/products` (Catálogo)
Produtos pertencentes a uma empresa específica.
- **Campos Principais:** `titulo`, `preco` (Number), `estoque` (Number), `fotos` (Array de URLs do Cloudinary), `createdAt`.
- *Nota Arquitetural:* Utiliza-se `collectionGroup('products')` na API para buscar todos os produtos do ecossistema quando necessário, ou o path direto para listar apenas os da empresa logada.

### Subcoleção: `users/{empresaId}/orders` (Pedidos)
Transações financeiras e de saída de estoque.
- **Campos Principais:** `items` (Array contendo `productId` e `quantidade`), `total` (Valor total da venda), `createdAt`.

## 4. Módulos e Regras de Negócio

### 4.1. Auth Module (`/auth`)
Gerencia o ciclo de vida dos usuários (empresas).
- **Registro (SignUp):** Cria o usuário no Firebase Auth (para credenciais) e simultaneamente cria o documento na coleção `users` no Firestore. Inclui validação rigorosa de formato de **CNPJ** via decorator customizado (`@isValidCnpj`).
- **Atualização:** Permite alterar `displayName`, `email` e `password`.

### 4.2. Products Module (`/products`)
Gerencia o inventário das empresas.
- **CRUD Padrão:** Criar, Ler, Atualizar e Deletar produtos, sempre passando o `empresaId` para garantir que o escopo fique contido no usuário correto.
- **Upload de Imagens:** Possui um controller dedicado para upload. A imagem é enviada via `multer`, processada, enviada ao **Cloudinary**, e a URL segura (`secure_url`) é injetada no array de `fotos` do documento do produto correspondente no Firestore.
- **Métricas:** Possui métodos para calcular o volume total em dinheiro dos produtos cadastrados (Total Sales potencial).

### 4.3. Orders Module (`/orders`)
O módulo mais crítico para a consistência do ecossistema.
- **Criação de Pedidos (Transações Atômicas):** Quando um pedido é feito (ex: pelo Mobile), a API utiliza `db.runTransaction()`. 
  - **Passo 1:** Lê o estoque atual de todos os produtos do pedido.
  - **Passo 2:** Verifica se há estoque suficiente (`estoqueAtual >= quantidade`). Se não houver, a transação é abortada (lança `BadRequestException`).
  - **Passo 3:** Subtrai a quantidade vendida do estoque do produto.
  - **Passo 4:** Grava o documento do pedido na subcoleção `orders`.
  - *Resultado:* Isso garante que problemas de concorrência (duas pessoas comprando o último item ao mesmo tempo) não quebrem o estoque.
- **Métricas de Vendas:** Endpoints para consolidar o total faturado, quantidade de pedidos e quantidade de itens vendidos por uma empresa específica.

### 4.4. Helpers & Utils
- **Validação de CNPJ:** Decorator customizado (`isValidCNPJ.decorator.ts`) acoplado ao `class-validator` para barrar cadastros de CNPJs inválidos logo na camada de DTO (Data Transfer Object).

## 5. Como a API atende o Web e o Mobile
- **Mobile (RuralizeSeller):** Consome pesadamente o `OrdersModule` para criar transações e o `ProductsModule` para ler o catálogo e estoque em tempo real.
- **Web (RuralizeShop):** Focado no consumo de métricas (`getTotalSales`), CRUD intensivo de produtos (Upload de fotos no `ProductsModule`) e gestão da conta (`AuthModule`).