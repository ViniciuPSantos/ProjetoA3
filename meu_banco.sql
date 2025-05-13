create database meu_banco;
use meu_banco;
create table usuario (
	id int auto_increment primary key,
    nome varchar(100) not null,
    email varchar(100) not null unique,
    senha varchar(255) not null, 
    criado_em timestamp default current_timestamp
);
select * from usuario;
