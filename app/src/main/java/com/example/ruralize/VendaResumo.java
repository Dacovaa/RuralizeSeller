package com.example.ruralize;

public class VendaResumo {

    private final String produtoId;
    private final String produtoTitulo;
    private int unidadesVendidas;
    private double receitaGerada;
    private String dataUltimaVenda;

    public VendaResumo(String produtoId, String produtoTitulo) {
        this.produtoId = produtoId;
        this.produtoTitulo = produtoTitulo;
        this.unidadesVendidas = 0;
        this.receitaGerada = 0.0;
        this.dataUltimaVenda = "";
    }

    public void acumularVenda(Venda venda) {
        if (venda == null) {
            return;
        }
        unidadesVendidas += venda.getQuantidade();
        receitaGerada += venda.getValorTotal();
        dataUltimaVenda = venda.getData();
    }

    public String getProdutoId() {
        return produtoId;
    }

    public String getProdutoTitulo() {
        return produtoTitulo;
    }

    public int getUnidadesVendidas() {
        return unidadesVendidas;
    }

    public double getReceitaGerada() {
        return receitaGerada;
    }

    public String getDataUltimaVenda() {
        return dataUltimaVenda;
    }
}



