package com.example.ruralize;

public class Notificacao {
    private final String id;
    private final String type;
    private final String title;
    private final String message;
    private final boolean read;
    private final String createdAt;

    public Notificacao(String id, String type, String title, String message, boolean read, String createdAt) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public String getCreatedAt() { return createdAt; }
}
