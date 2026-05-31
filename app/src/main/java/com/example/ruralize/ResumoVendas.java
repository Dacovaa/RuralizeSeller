package com.example.ruralize;

import com.google.gson.annotations.SerializedName;

public class ResumoVendas {
    @SerializedName("total")
    private final double total;

    @SerializedName("totalOrders")
    private final int totalOrders;

    @SerializedName("orderProductQuantity")
    private final int orderProductQuantity;

    public ResumoVendas(double total, int totalOrders, int orderProductQuantity) {
        this.total = total;
        this.totalOrders = totalOrders;
        this.orderProductQuantity = orderProductQuantity;
    }

    public double getTotal() {
        return total;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public int getOrderProductQuantity() {
        return orderProductQuantity;
    }
}
