package com.example.ruralize;

public class Venda {

    private final String id;
    private final String produtoId;
    private final String produtoTitulo;
    private final int quantidade;
    private final double valorTotal;
    private final double precoUnitario;
    private final String data;
    private final String metodoPagamento;
    private final String enderecoEntrega;

    public Venda(String id, String produtoId, String produtoTitulo, int quantidade,
                 double valorTotal, double precoUnitario, String data,
                 String metodoPagamento, String enderecoEntrega) {
        this.id = id;
        this.produtoId = produtoId;
        this.produtoTitulo = produtoTitulo;
        this.quantidade = quantidade;
        this.valorTotal = valorTotal;
        this.precoUnitario = precoUnitario;
        this.data = data;
        this.metodoPagamento = metodoPagamento;
        this.enderecoEntrega = enderecoEntrega;
    }

    public String getId() { return id; }
    public String getProdutoId() { return produtoId; }
    public String getProdutoTitulo() { return produtoTitulo; }
    public int getQuantidade() { return quantidade; }
    public double getValorTotal() { return valorTotal; }
    public double getPrecoUnitario() { return precoUnitario; }
    public String getData() { return data; }
    public String getMetodoPagamento() { return metodoPagamento; }
    public String getEnderecoEntrega() { return enderecoEntrega; }
}



