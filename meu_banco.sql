create DATABASE meu_banco;
use meu_banco;
CREATE TABLE Usuario (
     id INT AUTO_INCREMENT PRIMARY KEY,
     nome VARCHAR(100) not null,
     email VARCHAR(100) not null UNIQUE,
     senha VARCHAR(255) not null
);