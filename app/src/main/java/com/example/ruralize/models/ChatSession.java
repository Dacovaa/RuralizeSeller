package com.example.ruralize.models;
public class ChatSession {
    private String id;
    private String buyerName;

    public ChatSession(String id, String buyerName) {
        this.id = id;
        this.buyerName = buyerName;
    }

    public String getId() { return id; }
    public String getBuyerName() { return buyerName; }
}
