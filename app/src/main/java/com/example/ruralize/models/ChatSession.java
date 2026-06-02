package com.example.ruralize.models;

public class ChatSession {
    private String id;
    private String buyerName;
    private String empresaId;

    public ChatSession() {}

    public ChatSession(String id, String buyerName, String empresaId) {
        this.id = id;
        this.buyerName = buyerName;
        this.empresaId = empresaId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBuyerName() { return buyerName; }
    public String getEmpresaId() { return empresaId; }
}
