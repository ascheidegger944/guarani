-- Migration: V3__add_payment_and_audit_fields.sql
-- Description: Adiciona campos de pagamento e auditoria

-- Adicionar campos de pagamento na tabela orders
ALTER TABLE orders 
ADD COLUMN payment_method VARCHAR(50),
ADD COLUMN payment_status VARCHAR(50) DEFAULT 'PENDING',
ADD COLUMN payment_date TIMESTAMP,
ADD COLUMN transaction_id VARCHAR(255);

-- Adicionar campos de auditoria
ALTER TABLE users 
ADD COLUMN created_by VARCHAR(255),
ADD COLUMN last_modified_by VARCHAR(255);

ALTER TABLE products 
ADD COLUMN created_by VARCHAR(255),
ADD COLUMN last_modified_by VARCHAR(255);

ALTER TABLE orders 
ADD COLUMN created_by VARCHAR(255),
ADD COLUMN last_modified_by VARCHAR(255);

-- Criar tabela de histórico de preços de produtos
CREATE TABLE product_price_history (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    old_price DECIMAL(19,2) NOT NULL,
    new_price DECIMAL(19,2) NOT NULL,
    changed_by VARCHAR(255) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_price_history_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Criar tabela de estoque
CREATE TABLE stock_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    movement_type VARCHAR(50) NOT NULL, -- ENTRADA, SAIDA, AJUSTE
    quantity INTEGER NOT NULL,
    previous_stock INTEGER NOT NULL,
    new_stock INTEGER NOT NULL,
    reason VARCHAR(255),
    movement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    CONSTRAINT fk_stock_movements_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Índices para as novas tabelas
CREATE INDEX idx_product_price_history_product_id ON product_price_history(product_id);
CREATE INDEX idx_product_price_history_changed_at ON product_price_history(changed_at);
CREATE INDEX idx_stock_movements_product_id ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_movement_date ON stock_movements(movement_date);
CREATE INDEX idx_stock_movements_movement_type ON stock_movements(movement_type);

-- Comentários nas tabelas
COMMENT ON TABLE users IS 'Tabela de usuários do sistema';
COMMENT ON TABLE user_roles IS 'Tabela de roles dos usuários';
COMMENT ON TABLE products IS 'Tabela de produtos disponíveis para venda';
COMMENT ON TABLE orders IS 'Tabela de pedidos realizados';
COMMENT ON TABLE order_items IS 'Tabela de itens dos pedidos';
COMMENT ON TABLE product_price_history IS 'Tabela de histórico de alterações de preço';
COMMENT ON TABLE stock_movements IS 'Tabela de movimentações de estoque';