package com.exemplo;

public class ItemCarrinho {
    private int produtoId;
    private String nome;
    private String tamanho;
    private int quantidade;
    private double valorUnitario;

    public ItemCarrinho(int produtoId, String nome, String tamanho, int quantidade, double valorUnitario) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.tamanho = tamanho;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }

    public int getProdutoId() {
        return produtoId;
    }

    public String getNomeProduto() {
        return nome;
    }

    public String getTamanho() {
        return tamanho;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getValorUnitario() {
        return valorUnitario;
    }

    public double getSubtotal() {
        return quantidade * valorUnitario;
    }

    @Override
    public String toString() {
        return String.format("%s (Tam: %s, Qtd: %d) - R$%.2f", nome, tamanho, quantidade, getSubtotal());
    }

    public void setQuantidade(int novaQuantidade) {
        this.quantidade = novaQuantidade;
    }

    public void setTamanho(String novoTamanho) {
        this.tamanho = novoTamanho;
    }
}