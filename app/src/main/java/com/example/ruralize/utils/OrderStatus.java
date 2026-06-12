package com.example.ruralize.utils;

import com.example.ruralize.R;
import java.util.Locale;

public class OrderStatus {

    public static final String PENDING = "pending";
    public static final String PREPARING = "preparing";
    public static final String SHIPPED = "shipped";
    public static final String DELIVERED = "delivered";
    public static final String CANCELLED = "cancelled";

    public static String getLabel(String status) {
        if (status == null) return "Pendente";
        switch (status.toLowerCase(Locale.ROOT)) {
            case PENDING: return "Pendente";
            case PREPARING: return "Em Preparação";
            case SHIPPED: return "Enviado";
            case DELIVERED: return "Entregue";
            case CANCELLED: return "Cancelado";
            default: return status;
        }
    }

    public static int getColorResource(String status) {
        if (status == null) return R.color.text_secondary;
        switch (status.toLowerCase(Locale.ROOT)) {
            case PENDING: return R.color.text_secondary;
            case PREPARING: return R.color.green_light;
            case SHIPPED: return R.color.green_mid;
            case DELIVERED: return R.color.green_dark;
            case CANCELLED: return R.color.error_red;
            default: return R.color.green_light;
        }
    }
}
