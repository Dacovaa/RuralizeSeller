# Ruralize Ecosystem — Documentação Técnica (Shop)

Este documento fornece uma visão detalhada do projeto **RuralizeShop** e sua integração dentro do ecossistema Ruralize, que inclui a `ruralize_api` (backend) e o `RuralizeSeller` (aplicativo mobile para vendedores).

---

## 1. Visão Geral

O **RuralizeShop** é a plataforma de marketplace voltada para o consumidor final (comprador). É uma aplicação web moderna construída com **Next.js 16**, focada na venda de produtos rurais, oferecendo uma experiência de compra fluida com suporte a múltiplos vendedores.

### Papéis no Ecossistema:
- **RuralizeShop (Web):** Interface do comprador. Navegação de produtos, carrinho, busca, favoritos e checkout.
- **RuralizeSeller (Mobile):** Interface do produtor/vendedor. Gestão de estoque, pedidos e perfil da empresa.
- **Ruralize API (Backend):** Núcleo de processamento, banco de dados e autenticação que serve ambas as plataformas.

---

## 2. Stack Tecnológica (RuralizeShop)

- **Framework:** Next.js 16 (App Router)
- **Linguagem:** TypeScript 5
- **Estilização:** Tailwind CSS 4 + Radix UI (via shadcn/ui)
- **Estado Global:** React Context API (Cart, Auth, Products, Favorites, Toast)
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
}
```

### Endpoints Consumidos

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/products` | Lista todos os produtos (Polling de 10s no Shop) |
| `GET` | `/products/{empresaId}/{id}` | Detalhes de um produto específico |
| `POST` | `/auth/signup` | Registro com campo adicional `role: "customer"`. **Nota:** O campo `cnpj` deve ser omitido para este papel para evitar erros de validação no backend. |
| `POST` | `/orders` | Criação de pedidos após fluxo de checkout |
| `GET` | `/orders/comprador/{uid}` | Histórico de pedidos para o comprador |

---

## 4. Funcionalidades e Fluxos Críticos

### 4.1. Busca e Filtros
A página inicial implementa uma **Barra de Busca** por nome e filtros avançados:
- **Filtro por Loja:** Carrega nomes reais via `/auth/stores`.
- **Filtro por Categoria:** Sincronizado com o App Mobile. Categorias permitidas: `Rações e Concentrados`, `Suplementos e Vitaminas`, `Ferraduras e Ferramentas`, `Selaria e Equipamentos`, `Higiene e Cuidados`, `Medicamentos Veterinários`, `Acessórios para Estábulo` e `Outros`.
A listagem é paginada (12 itens por página) para otimização de layout.

### 4.2. Sistema de Favoritos (Wishlist)
Utiliza o `FavoritesProvider` com persistência em **LocalStorage**. Permite ao usuário salvar produtos para visualização posterior na página `/favoritos`. (Implementação de backend futura).

### 4.3. Avaliações (Reviews)
Sistema de prova social plenamente integrado. As notas e comentários são persistidos no banco de dados via endpoint `/reviews`, permitindo que o feedback seja visível para todos os usuários.

### 4.4. Fluxo de Checkout e Pedidos (RF11/RF12)
- **Carrinho Agrupado:** Os itens no carrinho são agora agrupados automaticamente por **Produtor/Loja**, permitindo uma visão clara de quais produtos vêm de cada vendedor.
- **Checkout (RF11):** Permite selecionar método de pagamento e endereço.
- **Dashboard do Consumidor:** A página `/perfil` agora exibe métricas em tempo real, como total de pedidos realizados e quantidade de itens favoritos.

---

## 5. UI/UX e Performance
- **Skeleton Screens:** Implementados estados de carregamento pulsantes para a vitrine de produtos e avaliações, melhorando a percepção de performance.
- **Feedback Visual:** Toasts integrados para todas as ações críticas (carrinho, perfil, pedidos).

---

## 6. Estrutura do Repositório (Shop)

```text
app/
├── (auth)/             # Proteção de rotas
├── carrinho/           # Listagem do carrinho
├── checkout/           # Fluxo de finalização (API integrada)
├── favoritos/          # Wishlist (LocalStorage)
├── perfil/             # Gestão de perfil (Firebase)
├── pedidos/            # Histórico Real de Pedidos (API integrada)
├── components/         # Componentes (Navbar, ProductCard, Reviews)
├── context/            # Estado (Auth, Cart, Products, Favorites)
├── produto/            # Detalhes e Reviews persistentes
└── services/           # Firebase e API
```

---

## 6. Configuração de Ambiente

Necessário configurar o `.env.local` com as seguintes chaves:

```env
NEXT_PUBLIC_API_URL=https://ruralize-api.vercel.app
NEXT_PUBLIC_FIREBASE_API_KEY=...
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=...
NEXT_PUBLIC_FIREBASE_PROJECT_ID=...
# ... demais chaves do Firebase
```

---

## 📌 TO-DO / Próximos Passos (Web Shop)
- [x] **Checkout Fluido:** Concluído com integração real.
- [x] **Área do Usuário:** Perfil e Meus Pedidos totalmente funcionais.
- [x] **Busca e Filtros:** Home com busca e nomes REAIS de lojas (via `/auth/stores`).
- [x] **Sistema de Favoritos:** Funcional via LocalStorage.
- [x] **Persistência de Reviews:** Agora persistido no banco de dados via `/reviews`.
- [x] **Notificações em Tempo Real (RF15):** Implementado `NotificationProvider` que ouve mudanças no Firestore e exibe Toasts instantâneos.
- [x] **Chat Integrado (RF16):** Implementada interface de chat em tempo real na página do produto e central de mensagens em `/mensagens`.
- [x] **Sincronização de Favoritos:** Agora persistido no banco de dados via `/favorites`. (API integrada)
- [ ] **Testes de Aceitação (Cucumber):** Impleme