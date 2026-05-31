# Ruralize Ecosystem — Documentação Técnica

Este documento fornece uma visão detalhada do projeto **RuralizeShop** e sua integração dentro do ecossistema Ruralize, que inclui a `ruralize_api` (backend) e o `RuralizeSeller` (aplicativo mobile para vendedores).

---

## 1. Visão Geral

O **RuralizeShop** é a plataforma de marketplace voltada para o consumidor final (comprador). É uma aplicação web moderna construída com **Next.js 16**, focada na venda de produtos rurais, oferecendo uma experiência de compra fluida com suporte a múltiplos vendedores.

### Papéis no Ecossistema:
- **RuralizeShop (Web):** Interface do comprador. Navegação de produtos, carrinho e checkout.
- **RuralizeSeller (Mobile):** Interface do produtor/vendedor. Gestão de estoque, pedidos e perfil da empresa.
- **Ruralize API (Backend):** Núcleo de processamento, banco de dados e autenticação que serve ambas as plataformas.

---

## 2. Stack Tecnológica (RuralizeShop)

- **Framework:** Next.js 16 (App Router)
- **Linguagem:** TypeScript 5
- **Estilização:** Tailwind CSS 4 + Radix UI (via shadcn/ui)
- **Estado Global:** React Context API (Cart, Auth, Products, Toast)
- **Autenticação:** Firebase Auth + Custom API Integration
- **Imagens:** Cloudinary (CDN)
- **Ícones:** Lucide React

---

## 3. Arquitetura de Dados e Integração API

O projeto consome dados da `ruralize_api` hospedada na Vercel: `https://ruralize-api.vercel.app`.

### Modelos de Dados Principais

#### Produto (`Product`)
Campos utilizados no frontend:
```typescript
{
  id: string;
  titulo: string;
  foto?: string;       // URL principal
  fotos?: string[];    // Galeria de fotos
  descricao?: string;
  preco: number;       // Valor em decimal/float
  categoria: string;
  estoque: number;
  empresaId: string;   // Vínculo com o vendedor (RuralizeSeller)
  options?: {          // Variações de produto (ex: tamanho, peso)
    id: string;
    name: string;
    suboptions: { id: string; name: string; }[];
  }[];
}
```

### Endpoints Consumidos

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/products` | Lista todos os produtos (Polling de 10s no Shop) |
| `GET` | `/products/{empresaId}/{id}` | Detalhes de um produto específico |
| `POST` | `/auth/signup` | Registro de novo usuário (integração Firebase + DB) |
| `POST` | `/orders` | Finalização de compra (Checkout) |

---

## 4. Fluxos Críticos

### 4.1. Sincronização de Produtos
O `ProductsProvider` utiliza um mecanismo de **Polling com ETag**. A cada 10 segundos, o frontend verifica se houve mudanças no catálogo. Se o status for `304 Not Modified`, os dados locais são mantidos, economizando banda.

### 4.2. Sistema de Carrinho
- Gerenciado pelo `CartContext`.
- Suporta múltiplos itens de diferentes vendedores (`empresaId`).
- Persistência atual: Em memória (planejado para LocalStorage/API).

### 4.3. Autenticação e Registro
1. O usuário se registra na página `/login`.
2. O frontend chama a API externa `POST /auth/signup` enviando o perfil.
3. Após sucesso na API, o Firebase Auth cria a credencial de acesso.
4. O `AuthContext` monitora o estado do Firebase para manter a sessão ativa.

---

## 5. Estrutura do Repositório (Shop)

```text
app/
├── (auth)/             # Lógica de login e proteção de rotas
├── carrinho/           # Página de checkout
├── components/         # Componentes de negócio (Navbar, ProductCard, etc.)
├── context/            # Gerenciamento de estado (Auth, Cart, Products)
├── produto/            # Rotas dinâmicas: [empresaId]/[id]
├── services/           # Configuração Firebase e chamadas API
└── types/              # Definições de tipos TypeScript
components/ui/          # Primitivos visuais (shadcn/ui)
public/                 # Assets estáticos e ícones
```

---

## 6. Configuração de Ambiente

Necessário configurar o `.env.local` com as seguintes chaves:

```env
# API Backend
NEXT_PUBLIC_API_URL=https://ruralize-api.example.com

# Firebase Configuration
NEXT_PUBLIC_FIREBASE_API_KEY=...
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=...
NEXT_PUBLIC_FIREBASE_PROJECT_ID=...
NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET=...
NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=...
NEXT_PUBLIC_FIREBASE_APP_ID=...
```

---

## 7. Notas para os Outros Repositórios

### Para `ruralize_api`:
- O Shop espera o cabeçalho `ETag` na rota `/products` para otimização.
- O payload de `/orders` segue a estrutura do `CartItem` (id, quantidade, preço unitário).

### Para `RuralizeSeller`:
- Os produtos criados no app mobile devem obrigatoriamente incluir `empresaId` e pelo menos uma imagem no Cloudinary para aparecerem corretamente no Shop.
- A rota no Shop é construída como `/produto/${empresaId}/${productId}`.

---

## 📌 TO-DO / Próximos Passos (Web Shop)
- [ ] **Checkout Fluido:** Implementar o formulário de pagamento e integração com a rota `/orders` da API.
- [ ] **Persistência de Carrinho:** Mover o estado do carrinho para LocalStorage ou sincronizar com a API/Auth.
- [ ] **Busca Global:** Melhorar o filtro de produtos por categoria e nome.
- [ ] **Perfil do Comprador:** Criar a área logada para o comprador ver seus pedidos realizados.

---

**Última Atualização:** Maio de 2026.
