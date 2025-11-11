package com.example.ruralize;

public class Entrega {

    private final String id;
    private final String pedidoId;
    private final String clienteNome;
    private final String status;
    private final String dataEntrega;
    private final String endereco;
    private final double valorTotal;
    private final String observacao;

    public Entrega(String id,
                   String pedidoId,
                   String clienteNome,
                   String status,
                   String dataEntrega,
                   String endereco,
                   double valorTotal,
                   String observacao) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.clienteNome = clienteNome;
        this.status = status;
        this.dataEntrega = dataEntrega;
        this.endereco = endereco;
        this.valorTotal = valorTotal;
        this.observacao = observacao;
    }

    public String getId() {
        return id;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public String getStatus() {
        return status;
    }

    public String getDataEntrega() {
        return dataEntrega;
    }

    public String getEndereco() {
        return endereco;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public String getObservacao() {
        return observacao;
    }
}

