package com.example.ruralize.models;
import com.google.firebase.Timestamp;

public class Message {
    private String text;
    private String senderId;
    private Timestamp createdAt;
    private String buyerName;
    private String empresaName;

    public Message() {}

    public Message(String text, String senderId, Timestamp createdAt, String buyerName, String empresaName) {
        this.text = text;
        this.senderId = senderId;
        this.createdAt = createdAt;
        this.buyerName = buyerName;
        this.empresaName = empresaName;
    }

    public String getText() { return text; }
    public String getSenderId() { return senderId; }
    public Timestamp getCreatedAt() { return createdAt; }
    public String getBuyerName() { return buyerName; }
    public String getEmpresaName() { return empresaName; }
}
