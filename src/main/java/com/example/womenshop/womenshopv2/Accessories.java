package com.example.womenshop.womenshopv2;

public class Accessories extends Product {

    private final int categoryId = 3;

    public Accessories(String name, double purchasePrice, double sellPrice) {
        super(name, purchasePrice, sellPrice);
    }

    public int getCategoryId() {
        return categoryId;
    }

    @Override
    public String toString() {
        return "💍" + name + " - " + String.format("%.2f €", sellPrice) + " (Quantité: " + nbItems + ")";
    }
}
