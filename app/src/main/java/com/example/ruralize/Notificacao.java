package com.example.ruralize;

import com.google.gson.annotations.SerializedName;

public class Notificacao {
    @SerializedName("id")
    private final String id;

    @SerializedName("type")
    private final String type;

    @SerializedName("title")
    private final String title;

    @SerializedName("message")
    private final String message;

    @SerializedName("read")
    private final boolean read;

    @SerializedName("createdAt")
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
