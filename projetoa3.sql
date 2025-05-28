CREATE DATABASE IF NOT EXISTS projetoa3;
USE projetoa3;

-- Tabela de Usuários (para bazares, e potencialmente clientes)
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    tipo ENUM('bazar', 'cliente') NOT NULL, -- Tipo de usuário
    -- Outros campos como endereço, telefone, etc., podem ser adicionados aqui
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_email (email(191)) -- Cria um índice único nos primeiros 191 caracteres do email
);

-- Tabela de Produtos
CREATE TABLE IF NOT EXISTS produtos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255),
    descricao TEXT,
    valor DECIMAL(10, 2) NOT NULL,
    quantidade_p INT DEFAULT 0,
    quantidade_m INT DEFAULT 0,
    quantidade_g INT DEFAULT 0,
    imagens1_path VARCHAR(255),
    imagens2_path VARCHAR(255),
    imagens3_path VARCHAR(255),
    bazar_id INT, -- Chave estrangeira para o usuário (bazar) que cadastrou o produto
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (bazar_id) REFERENCES usuarios(id)
);

-- Tabela de Pedidos
CREATE TABLE IF NOT EXISTS pedidos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL, -- Cliente que fez o pedido
    data_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('pendente', 'processando', 'enviado', 'entregue', 'cancelado') DEFAULT 'pendente',
    endereco_entrega TEXT NOT NULL,
    forma_pagamento VARCHAR(50),
    total DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Tabela de Itens do Pedido (detalhes dos produtos em cada pedido)
CREATE TABLE IF NOT EXISTS itens_pedido (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id INT NOT NULL,
    produto_id INT NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10, 2) NOT NULL, -- Preço do produto no momento do pedido
    subtotal DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
    FOREIGN KEY (produto_id) REFERENCES produtos(id)
);
SELECT id, nome, valor, imagens1_path FROM produtos;
ALTER TABLE itens_pedido ADD COLUMN tamanho VARCHAR(50);
ALTER TABLE pedidos ADD COLUMN email VARCHAR(255);
select * from usuarios;

