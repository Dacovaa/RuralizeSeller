package com.example.ruralize.network;

/**
 * Centraliza a configuração dos endpoints usados pelo app.
 *
 * TODO: Substitua os valores abaixo pelos dados da sua nova API assim que estiverem disponíveis.
 *       Você pode manter a estrutura dos métodos de atalho e apenas ajustar os caminhos/domínios.
 */
public final class ApiConfig {

    private ApiConfig() {
        // Classe utilitária; não deve ser instanciada.
    }

    /**
     * Host base da API. Ao mover para a API própria, altere esta constante.
     */
    public static final String BASE_URL = "https://ruralize-api.vercel.app"; // TODO: atualizar para o domínio da nova API

    /**
     * Caminhos principais utilizados atualmente. Ajuste conforme a estrutura de rotas do novo backend.
     */
    public static final String AUTH_PATH = "/auth";           // TODO: alterar se o novo backend usar outro prefixo
    public static final String PRODUCTS_PATH = "/products";   // TODO: ajustar se necessário
    public static final String SALES_PATH = "/sales";         // TODO: ajustar se necessário
    public static final String DELIVERIES_PATH = "/deliveries"; // TODO: ajustar se necessário

    // ---------- AUTH ----------

    public static String signUp() {
        return BASE_URL + AUTH_PATH + "/signup";
    }

    public static String profile(String uid) {
        return BASE_URL + AUTH_PATH + "/" + uid;
    }

    public static String updateProfile() {
        return BASE_URL + AUTH_PATH + "/update";
    }

    public static String updatePassword() {
        return BASE_URL + AUTH_PATH + "/updatePassword";
    }

    // ---------- PRODUCTS ----------

    public static String productsByUser(String uid) {
        return BASE_URL + PRODUCTS_PATH + "/" + uid;
    }

    public static String productsCollection() {
        return BASE_URL + PRODUCTS_PATH;
    }

    public static String productUpdate(String productId) {
        return BASE_URL + PRODUCTS_PATH + "/" + productId;
    }

    public static String productDelete(String uid, String productId) {
        return BASE_URL + PRODUCTS_PATH + "/" + uid + "/" + productId;
    }

    // ---------- SALES ----------

    public static String salesByUser(String uid) {
        return BASE_URL + SALES_PATH + "/" + uid;
    }

    // ---------- DELIVERIES ----------

    public static String deliveriesByUser(String uid) {
        return BASE_URL + DELIVERIES_PATH + "/" + uid;
    }
}

