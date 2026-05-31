package com.example.ruralize;

import com.google.gson.annotations.SerializedName;

public class ResumoVendas {
    @SerializedName("total")
    private final double total;

    @SerializedName("totalOrders")
    private final int totalOrders;

    public ResumoVendas(double total, int totalOrders) {
        this.total = total;
        this.totalOrders = totalOrders;
    }

    public double getTotal() {
        return total;
    }

    public int getTotalOrders() {
        return totalOrders;
    }
}
