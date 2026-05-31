package com.example.ruralize;

public class ResumoVendas {
    private final double total;
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
