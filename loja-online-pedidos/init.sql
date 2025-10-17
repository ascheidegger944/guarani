-- init.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabela de usuários
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     username VARCHAR(255) UNIQUE NOT NULL,
                                     email VARCHAR(255) UNIQUE NOT NULL,
                                     password VARCHAR(255) NOT NULL,
                                     role VARCHAR(50) NOT NULL,
                                     enabled BOOLEAN DEFAULT TRUE,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de métodos de pagamento
CREATE TABLE IF NOT EXISTS payment_methods (
                                               id BIGSERIAL PRIMARY KEY,
                                               name VARCHAR(255) UNIQUE NOT NULL,
                                               type VARCHAR(50) NOT NULL,
                                               description TEXT,
                                               enabled BOOLEAN DEFAULT TRUE,
                                               config_json TEXT,
                                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de produtos
CREATE TABLE IF NOT EXISTS products (
                                        id BIGSERIAL PRIMARY KEY,
                                        name VARCHAR(255) NOT NULL,
                                        description TEXT,
                                        category VARCHAR(255),
                                        price DECIMAL(19,2) NOT NULL,
                                        stock_quantity INTEGER DEFAULT 0,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de pedidos
CREATE TABLE IF NOT EXISTS orders (
                                      id BIGSERIAL PRIMARY KEY,
                                      user_id BIGINT REFERENCES users(id),
                                      status VARCHAR(50) DEFAULT 'PENDING',
                                      total_amount DECIMAL(19,2) DEFAULT 0,
                                      discount DECIMAL(19,2) DEFAULT 0,
                                      shipping_fee DECIMAL(19,2) DEFAULT 0,
                                      final_amount DECIMAL(19,2) DEFAULT 0,
                                      payment_method_id BIGINT REFERENCES payment_methods(id),
                                      payment_id VARCHAR(255),
                                      payment_status VARCHAR(50),
                                      payment_date TIMESTAMP,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de itens do pedido
CREATE TABLE IF NOT EXISTS order_items (
                                           id BIGSERIAL PRIMARY KEY,
                                           order_id BIGINT REFERENCES orders(id),
                                           product_id BIGINT REFERENCES products(id),
                                           quantity INTEGER NOT NULL,
                                           unit_price DECIMAL(19,2) NOT NULL,
                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de histórico de pagamentos
CREATE TABLE IF NOT EXISTS payment_history (
                                               id BIGSERIAL PRIMARY KEY,
                                               order_id BIGINT REFERENCES orders(id),
                                               payment_method_id BIGINT REFERENCES payment_methods(id),
                                               transaction_id VARCHAR(255),
                                               amount DECIMAL(19,2),
                                               status VARCHAR(50),
                                               status_message TEXT,
                                               gateway_response TEXT,
                                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_id);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_price ON products(price);
CREATE INDEX IF NOT EXISTS idx_payment_history_order_id ON payment_history(order_id);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Inserir dados iniciais de métodos de pagamento
INSERT INTO payment_methods (name, type, description, enabled) VALUES
                                                                   ('Cartão de Crédito', 'CREDIT_CARD', 'Pagamento com cartão de crédito', true),
                                                                   ('Boleto Bancário', 'BOLETO', 'Pagamento com boleto bancário', true),
                                                                   ('PIX', 'PIX', 'Pagamento instantâneo via PIX', true),
                                                                   ('Transferência Bancária', 'BANK_TRANSFER', 'Transferência entre contas bancárias', true)
ON CONFLICT (name) DO NOTHING;

-- Inserir usuário admin inicial
INSERT INTO users (username, email, password, role) VALUES
    ('admin', 'admin@guarani.com', '$2a$10$ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ABCDEFGHIJK', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

-- Inserir alguns produtos de exemplo
INSERT INTO products (name, description, category, price, stock_quantity) VALUES
                                                                              ('Smartphone XYZ', 'Smartphone Android com 128GB', 'Eletrônicos', 999.99, 50),
                                                                              ('Notebook ABC', 'Notebook Intel i5, 8GB RAM', 'Eletrônicos', 1999.99, 25),
                                                                              ('Camiseta Básica', 'Camiseta 100% algodão', 'Roupas', 29.99, 100),
                                                                              ('Tênis Esportivo', 'Tênis para corrida', 'Calçados', 199.99, 30)
ON CONFLICT DO NOTHING;