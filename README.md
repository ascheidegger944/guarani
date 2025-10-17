# 🛍️ Sistema de Gerenciamento de Pedidos - API RESTful

## 📋 Sobre o Projeto

Sistema completo de gerenciamento de pedidos para e-commerce desenvolvido em Java Spring Boot com arquitetura RESTful. Oferece funcionalidades completas de CRUD, autenticação segura, integração com pagamentos e geração de relatórios.

## 🚀 Tecnologias Utilizadas

### Backend
- **Java 17** - Linguagem de programação
- **Spring Boot 3.x** - Framework principal
- **Spring Security + JWT** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **Maven** - Gerenciamento de dependências

### Banco de Dados
- **PostgreSQL** - Produção
- **H2** - Testes e desenvolvimento

### Ferramentas
- **Docker & Docker Compose** - Containerização
- **SpringDoc OpenAPI** - Documentação da API
- **JUnit 5 & Mockito** - Testes automatizados
- **JaCoCo** - Cobertura de testes
- **GitHub Actions** - CI/CD

## ⚙️ Funcionalidades

### 🛒 Gestão de Pedidos
- ✅ CRUD completo de pedidos
- ✅ Filtros por status, data e valor
- ✅ Cálculo automático de totais
- ✅ Gestão de status (PENDENTE, CONCLUÍDO, CANCELADO)

### 📦 Gestão de Produtos
- ✅ Cadastro e atualização de produtos
- ✅ Controle de estoque
- ✅ Filtros por nome, categoria e preço

### 🔐 Segurança
- ✅ Autenticação JWT
- ✅ Autorização por roles (ADMIN, CLIENT, OPERATOR)
- ✅ Controle de acesso baseado em permissões

### 💳 Pagamentos
- ✅ Múltiplas formas de pagamento
- ✅ Integração com gateways
- ✅ Webhooks para confirmação

## 🚀 Como Executar

### Pré-requisitos
- Java 17 ou superior
- Maven 3.6+
- Docker e Docker Compose

### 🐳 Execução com Docker
