Guarani Order System - Download e Instalação
📦 Download do Projeto
Opção 1: Download Direto
https://img.shields.io/badge/Download-Projeto_Completo-blue?style=for-the-badge&logo=github

Opção 2: Clone via Git
bash
git clone https://github.com/seu-usuario/guarani-order-system.git
cd guarani-order-system
🛠️ Instalação Rápida
Pré-requisitos
Java 17 ou superior

Maven 3.6+

Docker (opcional, para containerização)

PostgreSQL (ou use Docker)

📥 Passo a Passo para Instalação
1. Configuração do Ambiente
   bash
# Verifique a instalação do Java
java -version

# Verifique a instalação do Maven
mvn -version
2. Configuração do Banco de Dados
   Opção A: Usando Docker (Recomendado)

bash
# Execute o PostgreSQL com Docker
docker run -d --name postgres-guaraní \
-e POSTGRES_DB=guarani_orders \
-e POSTGRES_USER=postgres \
-e POSTGRES_PASSWORD=postgres \
-p 5432:5432 \
postgres:13
Opção B: Instalação Manual do PostgreSQL

Instale o PostgreSQL 13+

Crie o banco de dados:

sql
CREATE DATABASE guarani_orders;
CREATE USER guarani_user WITH PASSWORD 'guarani2024';
GRANT ALL PRIVILEGES ON DATABASE guarani_orders TO guarani_user;
3. Configuração da Aplicação
   Arquivo: src/main/resources/application.yml

yaml
spring:
datasource:
url: jdbc:postgresql://localhost:5432/guarani_orders
username: postgres
password: postgres
4. Build e Execução
   bash
# Navegue até a pasta do projeto
cd guarani

# Build do projeto
mvn clean install

# Executar a aplicação
mvn spring-boot:run
🐳 Execução com Docker (Mais Fácil)
Método Docker Compose (Recomendado)
bash
# Execute tudo com um comando
docker-compose up -d

# Verifique os containers
docker-compose ps

# Logs da aplicação
docker-compose logs -f app
Método Docker Manual
bash
# Build da imagem
docker build -t guarani-order-system .

# Executar container
docker run -p 8080:8080 \
-e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/guarani_orders \
-e SPRING_DATASOURCE_USERNAME=postgres \
-e SPRING_DATASOURCE_PASSWORD=postgres \
guarani-order-system
✅ Verificação da Instalação
Teste se a aplicação está rodando
bash
# Verifique a saúde da aplicação
curl http://localhost:8080/api/actuator/health

# Acesse a documentação Swagger
# Abra no navegador: http://localhost:8080/swagger-ui.html
Dados de Teste Incluídos
O projeto já vem com dados de exemplo:

Usuários:

admin@guarani.com / (senha no banco)

cliente@guarani.com

operador@guarani.com

Produtos: 15 produtos de exemplo

Pedidos: 3 pedidos de exemplo

🔧 Configurações Importantes
Portas Utilizadas
Aplicação: 8080

PostgreSQL: 5432

H2 Console (dev): 8080/h2-console

Variáveis de Ambiente
bash
# Banco de dados
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=guarani_orders
export DB_USERNAME=postgres
export DB_PASSWORD=postgres

# Segurança JWT
export JWT_SECRET=mySecretKeyForJWTGenerationInGuaraniApp2024
export JWT_EXPIRATION=86400000
🚀 Início Rápido
1. Primeiro Acesso
   Acesse: http://localhost:8080/swagger-ui.html

Registre um usuário:

json
{
"name": "Seu Nome",
"email": "seu@email.com",
"password": "suaSenha123"
}
Faça login e use o token JWT nos endpoints protegidos

2. Teste os Endpoints
   bash
# Listar produtos
curl -X GET "http://localhost:8080/api/products"

# Criar um pedido (com token JWT)
curl -X POST "http://localhost:8080/api/orders" \
-H "Authorization: Bearer SEU_TOKEN_JWT" \
-H "Content-Type: application/json" \
-d '{
"items": [
{
"productId": 1,
"quantity": 2
}
]
}'
📁 Estrutura de Arquivos para Download
text
guarani-order-system.zip
├── src/                    # Código fonte
├── .github/               # CI/CD
├── docker-compose.yml     # Orquestração Docker
├── Dockerfile            # Containerização
├── pom.xml              # Dependências Maven
├── README.md           # Documentação
└── scripts/            # Scripts de apoio
├── setup-database.sh
├── run-tests.sh
└── deploy.sh
🐛 Solução de Problemas
Problemas Comuns
Erro de Conexão com Banco:

bash
# Verifique se o PostgreSQL está rodando
sudo systemctl status postgresql

# Ou se o container Docker está ativo
docker ps | grep postgres
Porta 8080 Ocupada:

bash
# Altere a porta no application.yml
server:
port: 8081
Erro de Permissão:

bash
# Dê permissão de execução aos scripts
chmod +x scripts/*.sh
Logs e Debug
bash
# Ver logs da aplicação
tail -f logs/guarani-app.log

# Ou com Docker
docker-compose logs -f app
📞 Suporte
Se encontrar problemas na instalação:

Verifique os logs em logs/guarani-app.log

Confirme as versões:

bash
java -version  # Deve ser 17+
mvn -version   # Deve ser 3.6+
docker --version
Consulte a documentação em /swagger-ui.html

Abra uma issue no repositório