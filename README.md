# 🛒 Ecommerce API

API REST completa para e-commerce desenvolvida com Java 21 e Spring Boot 3.3.5, com autenticação JWT, controle de acesso por roles, catálogo de produtos, carrinho de compras e gestão de pedidos com workflow de status. A aplicação é containerizada com Docker e pronta para deploy em nuvem.

---

## 📋 Índice

- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Funcionalidades](#-funcionalidades)
- [Pré-requisitos](#-pré-requisitos)
- [Como Rodar](#-como-rodar)
- [Endpoints](#-endpoints)
- [Workflow de Pedidos](#-workflow-de-pedidos)
- [Estrutura do Projeto](#-estrutura-do-projeto)

---

## 🚀 Tecnologias

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Segurança | Spring Security + JWT (JJWT 0.12.6) |
| Banco de Dados | PostgreSQL 16 |
| ORM | Hibernate / Spring Data JPA |
| Migrations | Flyway |
| Build | Gradle 9.5 |
| Containerização | Docker + Docker Compose |
| Deploy | AWS EC2 + RDS (planejado) |

---

## 🏗️ Arquitetura

A aplicação segue uma arquitetura em camadas (Layered Architecture) com separação clara de responsabilidades:

```text
src/main/java/com/jeffry/ecommerce/
├── config/       # Configurações de segurança e filtros JWT
├── controller/   # Endpoints REST
├── service/      # Regras de negócio
├── repository/   # Acesso ao banco de dados
├── entity/       # Entidades JPA
├── dto/          # Objetos de transferência de dados
└── exception/    # Tratamento global de erros
```

### Modelo de Dados

```text
users ──────────────────────────────────────┐
  │                                         │
  ├──< cart_items >── products ──< categories
  │                                         │
  └──< orders >──< order_items >── products │
                                            │
users <─────────────────────────────────────┘
```

---

## ✅ Funcionalidades

### Autenticação e Autorização
- Cadastro de usuários com senha criptografada (BCrypt)
- Login com geração de token JWT (validade de 24h)
- Controle de acesso por roles: `USER` e `ADMIN`
- Filtro JWT em todas as requisições autenticadas

### Catálogo
- Listagem pública de produtos (sem autenticação)
- Busca por nome e filtragem por categoria
- CRUD completo de produtos e categorias (restrito a ADMIN)

### Carrinho de Compras
- Adição de itens com validação de estoque disponível
- Incremento automático de quantidade se produto já existe no carrinho
- Atualização de quantidade e remoção de itens individuais
- Limpeza completa do carrinho
- Cálculo automático de subtotal por item e total do carrinho

### Pedidos
- Criação de pedido diretamente a partir do carrinho
- Baixa automática de estoque ao confirmar pedido
- Limpeza automática do carrinho após criação do pedido
- Workflow de status com transições válidas
- Devolução automática de estoque ao cancelar pedido
- Histórico de pedidos por usuário

### Tratamento de Erros
- Respostas padronizadas em JSON para todos os erros
- Distinção entre erros de negócio (400) e recursos não encontrados (404)
- Validação de campos com mensagens em português

---

## 📦 Pré-requisitos

Para rodar localmente sem Docker:
- Java 21+
- PostgreSQL 16+
- Gradle 9+

Para rodar com Docker:
- Docker Desktop

---

## ▶️ Como Rodar

### Opção 1 — Docker (recomendado)

```bash
# Clonar o repositório
git clone https://github.com/JeffrySmith117/ecommerce-api.git
cd ecommerce-api

# Subir a aplicação e o banco de dados
docker compose up --build
```

A API ficará disponível em `http://localhost:8080`

### Opção 2 — Local (sem Docker)

1. Crie o banco de dados PostgreSQL:

```sql
CREATE DATABASE ecommerce_db;
```

2. Configure as variáveis de ambiente ou edite `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce_db
    username: seu_usuario
    password: sua_senha
```

3. Gere o JAR e execute:

```bash
./gradlew bootJar
java -jar build/libs/ecommerce-api-0.0.1-SNAPSHOT.jar
```

---

## 🔌 Endpoints

### Autenticação — `/auth`

| Método | Endpoint | Autenticação | Descrição |
|--------|----------|-------------|-----------|
| POST | `/auth/register` | Pública | Cadastrar novo usuário |
| POST | `/auth/login` | Pública | Autenticar e obter token JWT |

**Exemplo de cadastro:**

```json
POST /auth/register
{
  "name": "João Silva",
  "email": "joao@email.com",
  "password": "minhasenha123"
}
```

**Resposta:**

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "name": "João Silva",
  "email": "joao@email.com",
  "role": "USER"
}
```

---

### Categorias — `/categories`

| Método | Endpoint | Autenticação | Descrição |
|--------|----------|-------------|-----------|
| GET | `/categories` | Pública | Listar todas as categorias |
| GET | `/categories/{id}` | Pública | Buscar categoria por ID |
| POST | `/categories` | ADMIN | Criar categoria |
| PUT | `/categories/{id}` | ADMIN | Atualizar categoria |
| DELETE | `/categories/{id}` | ADMIN | Remover categoria |

---

### Produtos — `/products`

| Método | Endpoint | Autenticação | Descrição |
|--------|----------|-------------|-----------|
| GET | `/products` | Pública | Listar todos os produtos |
| GET | `/products?search=nome` | Pública | Buscar produtos por nome |
| GET | `/products?categoryId=1` | Pública | Filtrar por categoria |
| GET | `/products/{id}` | Pública | Buscar produto por ID |
| POST | `/products` | ADMIN | Criar produto |
| PUT | `/products/{id}` | ADMIN | Atualizar produto |
| DELETE | `/products/{id}` | ADMIN | Remover produto |

**Exemplo de criação de produto:**

```json
POST /products
Authorization: Bearer {token}
{
  "name": "Notebook Dell Inspiron",
  "description": "Notebook com processador Intel Core i7",
  "price": 3499.99,
  "stockQty": 10,
  "categoryId": 1
}
```

---

### Carrinho — `/cart`

| Método | Endpoint | Autenticação | Descrição |
|--------|----------|-------------|-----------|
| GET | `/cart` | USER | Ver carrinho do usuário logado |
| POST | `/cart/items` | USER | Adicionar item ao carrinho |
| PUT | `/cart/items/{id}` | USER | Atualizar quantidade do item |
| DELETE | `/cart/items/{id}` | USER | Remover item do carrinho |
| DELETE | `/cart` | USER | Limpar o carrinho |

**Exemplo de adição ao carrinho:**

```json
POST /cart/items
Authorization: Bearer {token}
{
  "productId": 1,
  "quantity": 2
}
```

**Resposta:**

```json
{
  "items": [
    {
      "id": 1,
      "product": { "id": 1, "name": "Notebook Dell Inspiron", "price": 3499.99 },
      "quantity": 2,
      "subtotal": 6999.98
    }
  ],
  "total": 6999.98
}
```

---

### Pedidos — `/orders`

| Método | Endpoint | Autenticação | Descrição |
|--------|----------|-------------|-----------|
| POST | `/orders` | USER | Criar pedido a partir do carrinho |
| GET | `/orders` | USER | Listar pedidos do usuário |
| GET | `/orders/{id}` | USER/ADMIN | Buscar pedido por ID |
| PATCH | `/orders/{id}/status` | ADMIN | Atualizar status do pedido |

**Exemplo de atualização de status:**

```json
PATCH /orders/1/status
Authorization: Bearer {token}
{
  "status": "CONFIRMED"
}
```

---

## 🔄 Workflow de Pedidos

Os pedidos seguem um ciclo de vida com transições de status controladas:

```text
PENDING ──► CONFIRMED ──► SHIPPED ──► DELIVERED
   │              │
   └──────────────┴──► CANCELLED
```

| Transição | Permitida |
|-----------|-----------|
| PENDING → CONFIRMED | ✅ |
| PENDING → CANCELLED | ✅ |
| CONFIRMED → SHIPPED | ✅ |
| CONFIRMED → CANCELLED | ✅ |
| SHIPPED → DELIVERED | ✅ |
| DELIVERED → qualquer | ❌ |
| CANCELLED → qualquer | ❌ |

> ⚠️ Ao cancelar um pedido, o estoque dos produtos é restaurado automaticamente.

---

## 📁 Estrutura do Projeto

```text
ecommerce-api/
├── src/
│   └── main/
│       ├── java/com/jeffry/ecommerce/
│       │   ├── config/
│       │   │   ├── JwtAuthFilter.java
│       │   │   ├── SecurityConfig.java
│       │   │   └── UserDetailsServiceImpl.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   ├── CategoryController.java
│       │   │   ├── ProductController.java
│       │   │   ├── CartController.java
│       │   │   └── OrderController.java
│       │   ├── service/
│       │   │   ├── AuthService.java
│       │   │   ├── JwtService.java
│       │   │   ├── CategoryService.java
│       │   │   ├── ProductService.java
│       │   │   ├── CartService.java
│       │   │   └── OrderService.java
│       │   ├── repository/
│       │   │   ├── UserRepository.java
│       │   │   ├── CategoryRepository.java
│       │   │   ├── ProductRepository.java
│       │   │   ├── CartItemRepository.java
│       │   │   ├── OrderRepository.java
│       │   │   └── OrderItemRepository.java
│       │   ├── entity/
│       │   │   ├── User.java
│       │   │   ├── Category.java
│       │   │   ├── Product.java
│       │   │   ├── CartItem.java
│       │   │   ├── Order.java
│       │   │   └── OrderItem.java
│       │   ├── dto/
│       │   │   ├── AuthResponse.java
│       │   │   ├── LoginRequest.java
│       │   │   ├── RegisterRequest.java
│       │   │   ├── CategoryRequest.java
│       │   │   ├── CategoryResponse.java
│       │   │   ├── ProductRequest.java
│       │   │   ├── ProductResponse.java
│       │   │   ├── CartItemRequest.java
│       │   │   ├── CartItemResponse.java
│       │   │   ├── CartResponse.java
│       │   │   ├── OrderItemResponse.java
│       │   │   ├── OrderResponse.java
│       │   │   └── OrderStatusUpdateRequest.java
│       │   └── exception/
│       │       ├── BusinessException.java
│       │       ├── ResourceNotFoundException.java
│       │       └── GlobalExceptionHandler.java
│       └── resources/
│           ├── application.yml
│           └── db/migration/
│               └── V1__create_tables.sql
├── Dockerfile
├── docker-compose.yml
├── build.gradle
└── gradle.properties

## 👨‍💻 Autor

**Jeffry Smith**

[![GitHub](https://img.shields.io/badge/GitHub-JeffrySmith117-black?style=flat&logo=github)](https://github.com/JeffrySmith117)