-- Migration: V2__insert_initial_data.sql
-- Description: Inserção de dados iniciais para desenvolvimento

-- Inserir usuários iniciais
INSERT INTO users (email, password, name) VALUES
('admin@guarani.com', '$2a$10$ABCDE12345FGHIJ67890KLMNOPQRSTUVWXYZ01234', 'Administrador do Sistema'),
('cliente@guarani.com', '$2a$10$ABCDE12345FGHIJ67890KLMNOPQRSTUVWXYZ01234', 'Cliente Teste'),
('operador@guarani.com', '$2a$10$ABCDE12345FGHIJ67890KLMNOPQRSTUVWXYZ01234', 'Operador do Sistema');

-- Inserir roles dos usuários
INSERT INTO user_roles (user_id, role) VALUES
(1, 'ADMIN'),
(1, 'OPERATOR'),
(2, 'CLIENT'),
(3, 'OPERATOR');

-- Inserir produtos iniciais
INSERT INTO products (name, description, price, category, stock_quantity) VALUES
('Smartphone Samsung Galaxy S23', 'Smartphone Android com 128GB, 8GB RAM, câmera tripla', 2999.99, 'ELETRONICOS', 50),
('Notebook Dell Inspiron 15', 'Notebook Intel i5, 8GB RAM, SSD 256GB, 15.6 pol', 3499.99, 'INFORMATICA', 30),
('Tablet Apple iPad 10ª Geração', 'Tablet Apple 10.9 pol, 64GB, Wi-Fi, Touch ID', 4299.99, 'ELETRONICOS', 25),
('Fone de Ouvido Sony WH-1000XM4', 'Fone over-ear wireless com cancelamento de ruído', 1499.99, 'AUDIO', 100),
('Smart TV LG 55" 4K UHD', 'Smart TV 55 polegadas, 4K UHD, WebOS, 3 HDMI', 3299.99, 'TV_VIDEO', 20),
('Monitor Gamer ASUS 24"', 'Monitor 24 polegadas, 144Hz, 1ms, Full HD', 1299.99, 'INFORMATICA', 40),
('Teclado Mecânico Redragon', 'Teclado mecânico RGB, switches Outemu Blue', 299.99, 'PERIFERICOS', 75),
('Mouse Logitech G502 Hero', 'Mouse gamer com sensor Hero 25K DPI', 349.99, 'PERIFERICOS', 60),
('Cadeira Gamer ThunderX3', 'Cadeira gamer ergonômica, reclinável, almofadas', 899.99, 'MOVEIS', 15),
('Webcam Logitech C920', 'Webcam Full HD 1080p, microfone stereo', 499.99, 'PERIFERICOS', 35),
('Impressora Multifuncional Epson', 'Impressora jato de tinta, Wi-Fi, scanner', 799.99, 'INFORMATICA', 25),
('SSD Kingston 1TB NVMe', 'SSD 1TB, NVMe, M.2, leitura 3500MB/s', 499.99, 'COMPONENTES', 80),
('Processador Intel Core i7', 'Processador Intel Core i7 12ª geração', 1899.99, 'COMPONENTES', 30),
('Placa de Vídeo RTX 3060', 'Placa de vídeo NVIDIA GeForce RTX 3060 12GB', 2899.99, 'COMPONENTES', 10),
('Headset HyperX Cloud II', 'Headset gamer com som surround 7.1', 599.99, 'AUDIO', 45);

-- Inserir alguns pedidos de exemplo
INSERT INTO orders (user_id, total_amount, status) VALUES
(2, 4499.98, 'CONFIRMED'),
(2, 1299.99, 'PENDING'),
(2, 599.99, 'DELIVERED');

-- Inserir itens dos pedidos
INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_price) VALUES
(1, 1, 1, 2999.99, 2999.99),  -- Smartphone
(1, 4, 1, 1499.99, 1499.99),  -- Fone de ouvido
(2, 6, 1, 1299.99, 1299.99),  -- Monitor Gamer
(3, 15, 1, 599.99, 599.99);   -- Headset