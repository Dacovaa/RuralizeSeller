# RuralizeSeller - Contexto do Projeto

Este documento fornece uma visão detalhada do aplicativo mobile **RuralizeSeller**, parte do ecossistema Ruralize (junto com RuralizeShop e ruralize_api). Ele serve como referência para desenvolvedores e agentes de IA entenderem a arquitetura, funcionalidades e integrações do app.

---

## 🚀 Visão Geral

O **RuralizeSeller** é um aplicativo Android desenvolvido para produtores rurais (vendedores) gerenciarem seus produtos, estoques, vendas e entregas de forma simplificada.

## 🛠 Stack Tecnológica
- **Linguagem Principal:** Java (com componentes em Kotlin).
- **Interface:** XML Layouts tradicional + Custom Views para Gráficos.
- **Autenticação:** Firebase Auth.
- **Networking:** OkHttp para chamadas REST à API customizada.
- **Persistência de Imagens:** Cloudinary (via API) + Glide para carregamento.
- **Serialização:** GSON e JSON nativo.

## 🏗 Arquitetura
O app segue um padrão baseado em **Activities**, com uma `BaseDrawerActivity` que centraliza a navegação lateral (Navigation Drawer).

### Componentes Chave:
- `Activity.java`: Tela de login e ponto de entrada.
- `DashboardActivity.java`: Visão geral do negócio com gráficos de vendas e pedidos.
- `GerenciarProdutosActivity.java` & `NovoProdutoActivity.java`: CRUD completo de produtos.
- `EstoqueActivity.java`: Gerenciamento focado em níveis de estoque.
- `VendasActivity.java`: Histórico e resumo de transações.
- `EntregasActivity.java`: Gestão logística de pedidos.
- `network/ApiConfig.java`: Centraliza todos os endpoints da API.

---

## 🔌 Integração com API (ruralize_api)
O app se comunica com o backend hospedado em `https://ruralize-api.vercel.app`.

### Principais Endpoints Consumidos:
- **Auth:** `/auth/signup`, `/auth/update`, `/auth/updatePassword`.
- **Produtos:** `/products/empresa/{uid}` (GET/POST/PUT/DELETE).
- **Vendas:** `/orders/totalVendas/{uid}`.
- **Entregas:** `/deliveries/{uid}`.

### Fluxo de Dados:
1. O usuário se autentica via Firebase.
2. O `uid` do Firebase é usado como identificador único nas chamadas para a `ruralize_api`.
3. As imagens são enviadas para a API, que processa o upload para o **Cloudinary** (não utilizamos Firebase Storage para imagens devido a custos).


---

## 📦 Modelos de Dados (Entidades)

- **Produto:** `id`, `titulo`, `descricao`, `preco`, `estoque`, `categoria`, `fotosUrls`.
- **Venda:** `id`, `cliente`, `valorTotal`, `data`, `status`.
- **Entrega:** `id`, `endereco`, `prazo`, `status`.

## 🎨 Elementos Visuais e Temas

- **Temas:** Localizados em `res/values/themes.xml`. Utiliza Material Components.
- **Gráficos:** Implementados via `MiniBarChartView` e `MiniLineChartView` (Custom Views desenhadas em Canvas).
- **Cores:** Paleta focada em verde/rural (definida em `colors.xml`).

---

## 🚩 Pendências e Pontos de Atenção

- **Refatoração para Retrofit:** O app possui a dependência do Retrofit no `build.gradle`, mas atualmente utiliza `OkHttp` puro com `JSONObject`.
- **Sincronização:** Garantir que as atualizações de estoque no app mobile reflitam instantaneamente no `RuralizeShop`.
- **Jetpack Compose:** Existe configuração para Compose, mas a interface atual é majoritariamente XML. Futuras features podem adotar Compose.

---

Este arquivo deve ser mantido atualizado sempre que houver mudanças estruturais na comunicação com a API ou no modelo de dados do vendedor.
