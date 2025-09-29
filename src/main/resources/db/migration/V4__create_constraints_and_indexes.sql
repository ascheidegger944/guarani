-- Migration: V4__create_constraints_and_indexes.sql
-- Description: Cria constraints adicionais e índices para otimização

-- Adicionar constraints de domínio para status
ALTER TABLE orders 
ADD CONSTRAINT chk_orders_status CHECK (status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'));

ALTER TABLE orders 
ADD CONSTRAINT chk_orders_payment_status CHECK (payment_status IN ('PENDING', 'APPROVED', 'REJECTED', 'REFUNDED'));

ALTER TABLE orders 
ADD CONSTRAINT chk_orders_payment_method CHECK (payment_method IN ('CREDIT_CARD', 'BOLETO', 'BANK_TRANSFER', 'PIX'));

ALTER TABLE stock_movements 
ADD CONSTRAINT chk_stock_movement_type CHECK (movement_type IN ('ENTRADA', 'SAIDA', 'AJUSTE'));

-- Adicionar constraint para garantir que o estoque não fique negativo
ALTER TABLE products 
ADD CONSTRAINT chk_products_stock_non_negative CHECK (stock_quantity >= 0);

-- Criar índices compostos para consultas frequentes
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_orders_created_status ON orders(created_at, status);
CREATE INDEX idx_products_category_price ON products(category, price) WHERE active = true;
CREATE INDEX idx_order_items_product_order ON order_items(product_id, order_id);

-- Criar função para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Criar triggers para updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Criar view para relatório de vendas
CREATE VIEW sales_report AS
SELECT 
    o.id as order_id,
    o.created_at as order_date,
    u.name as customer_name,
    o.total_amount,
    o.status,
    o.payment_method,
    o.payment_status,
    COUNT(oi.id) as items_count,
    SUM(oi.quantity) as total_quantity
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN order_items oi ON o.id = oi.order_id
GROUP BY o.id, u.name, o.created_at, o.total_amount, o.status, o.payment_method, o.payment_status;

-- Criar view para estoque crítico
CREATE VIEW critical_stock AS
SELECT 
    p.id,
    p.name,
    p.category,
    p.stock_quantity,
    p.price
FROM products p
WHERE p.active = true AND p.stock_quantity <= 10
ORDER BY p.stock_quantity ASC;