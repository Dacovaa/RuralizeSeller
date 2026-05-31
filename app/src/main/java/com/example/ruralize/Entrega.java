package com.example.ruralize;

import com.google.gson.annotations.SerializedName;

public class Entrega {

    @SerializedName("id")
    private final String id;

    @SerializedName("pedidoId")
    private final String pedidoId;

    @SerializedName("clienteNome")
    private final String clienteNome;

    @SerializedName("status")
    private final String status;

    @SerializedName("dataEntrega")
    private final String dataEntrega;

    @SerializedName("endereco")
    private final String endereco;

    @SerializedName("valorTotal")
    private final double valorTotal;

    @SerializedName("observacao")
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

