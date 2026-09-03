# Ecommerce — Front-end

Front-end em React + TypeScript para a [Ecommerce API](https://github.com/JeffrySmith117/ecommerce-api), um backend em Spring Boot com autenticação JWT, catálogo de produtos, carrinho e pedidos.

Este projeto consome a API para oferecer: cadastro/login, listagem de produtos com busca e filtro por categoria (paginada), carrinho de compras e histórico de pedidos.

## Stack

- React 19 + TypeScript
- Vite
- React Router
- Axios

## Rodando localmente

Pré-requisitos: Node.js 20+ e a [Ecommerce API](https://github.com/JeffrySmith117/ecommerce-api) rodando localmente (por padrão em `http://localhost:8080`).

```bash
npm install
cp .env.example .env
npm run dev
```

A aplicação sobe em `http://localhost:5173` (origem já liberada por padrão no CORS da API).

## Build de produção

```bash
npm run build
```

## Estrutura

```
src/
  api/          # chamadas HTTP para a API (axios)
  components/   # componentes reutilizáveis (navbar, paginação, rota protegida)
  context/      # contexto de autenticação (token JWT + usuário logado)
  pages/        # páginas: login, cadastro, produtos, carrinho, pedidos
  types/        # tipos TypeScript espelhando os DTOs da API
```

## Próximos passos

- Tela de administração para gerenciar produtos e categorias (endpoints `ADMIN` já existem na API)
- Testes de componentes (Vitest + Testing Library)
- Deploy (Vercel/Netlify) com a API publicada
