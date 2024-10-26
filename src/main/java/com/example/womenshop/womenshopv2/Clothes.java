package com.example.womenshop.womenshopv2;

public class Clothes extends Product {
    private int clothingSize;
    private final int categoryId = 2;

    public Clothes(String name, double purchasePrice, double sellPrice, int size) {
        super(name, purchasePrice, sellPrice);
        setClothingSize(size);
    }


    public int getClothingSize() {
        return clothingSize;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setClothingSize(int clothingSize) {
        if (clothingSize < 34 || clothingSize > 54) {
            throw new IllegalArgumentException("Wrong size! Size should be between 34 and 54 and even.");
        }
        this.clothingSize = clothingSize;
    }

    @Override
    public String toString() {
        return "👕" + name + " - (Size: " + clothingSize + ") - " + String.format("%.2f €", sellPrice) + " (Quantité: " + nbItems + ")";
    }
}