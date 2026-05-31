# RuralizeSeller - Instruções do Projeto

Este arquivo contém as diretrizes e o contexto necessário para trabalhar neste repositório.

## 🔗 Contexto Global do Ecossistema
Este projeto faz parte de um ecossistema de 3 repositórios. Sempre que precisar entender o impacto de uma mudança no sistema completo, utilize a ferramenta `web_fetch` para ler os arquivos de contexto dos outros repositórios:

- **Ruralize API (Backend):** `https://raw.githubusercontent.com/SEU_USUARIO/ruralize_api/main/RURALIZE_CONTEXT_API.md`
- **RuralizeShop (Web/Comprador):** `https://raw.githubusercontent.com/SEU_USUARIO/RuralizeShop/main/RURALIZE_CONTEXT_SHOP.md`

## 🏗 Arquitetura e Convenções
- **Padrão de UI:** O projeto utiliza XML Layouts. Novos componentes devem seguir os estilos definidos em `res/values/themes.xml` e `colors.xml`.
- **Networking:** Atualmente utiliza `OkHttp` bruto e `JSONObject` para parsing manual em muitas Activities. Existe uma intenção de migrar para `Retrofit` (dependência já instalada).
- **Autenticação:** O Firebase Auth é a fonte da verdade para o `uid` do usuário, que deve ser passado em todas as requisições para a API.
- **Imagens:** Devem ser enviadas para a API para upload no Cloudinary. O Firebase Storage não é utilizado para mídia.

## 📖 Documentação Local
Para detalhes técnicos específicos deste app mobile, consulte o arquivo:
- [RURALIZE_CONTEXT_SELLER.md](./RURALIZE_CONTEXT_SELLER.md)

---
*Nota: Substitua os links acima pelas URLs reais do seu GitHub para que eu possa acessá-los dinamicamente.*
