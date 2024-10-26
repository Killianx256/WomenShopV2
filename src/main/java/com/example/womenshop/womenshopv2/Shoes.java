package com.example.womenshop.womenshopv2;

public class Shoes extends Product {
    private int shoeSize;
    private final int categoryId = 1;

    public Shoes(String name, double purchasePrice, double sellPrice, int shoeSize) {
        super(name, purchasePrice, sellPrice);
        setShoeSize(shoeSize);
    }

    public int getShoeSize() {
        return shoeSize;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setShoeSize(int shoeSize) {
        if (shoeSize < 36 || shoeSize > 50) {
            throw new IllegalArgumentException("Wrong shoe size! Shoe size should be between 36 and 50.");
        }
        this.shoeSize = shoeSize;
    }

    @Override
    public String toString() {
        return "👟" + name + " - " +  "(Size : " + shoeSize + ") - " +  String.format("%.2f €", sellPrice) + " (Quantité: " + nbItems + ")";
    }

}