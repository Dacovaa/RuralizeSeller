package com.example.ruralize.network;

/**
 * Centraliza a configuração dos endpoints usados pelo app.
 * Todas as rotas agora são gerenciadas pelas interfaces do Retrofit.
 */
public final class ApiConfig {

    private ApiConfig() {
    }

    /**
     * Host base da API. Ao mover para a API própria, altere esta constante.
     */
    public static final String BASE_URL = "https://ruralize-api.vercel.app";

    /**
     * Caminhos principais utilizados atualmente.
     */
    public static final String AUTH_PATH = "/auth";
    public static final String PRODUCTS_PATH = "/products";
    public static final String SALES_PATH = "/orders";
    public static final String DELIVERIES_PATH = "/deliveries";
    public static final String NOTIFICATIONS_PATH = "/notifications";
}

