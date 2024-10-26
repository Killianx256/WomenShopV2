package com.example.womenshop.womenshopv2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class Product implements Discount, Comparable<Product> {

    private static int counter = 0;
    protected int number;
    protected String name;
    protected double purchasePrice;
    protected double sellPrice;
    protected double discountPrice;
    protected int nbItems;
    protected Integer productId;

    protected static double capital = 0;
    protected static double income = 0;
    protected static double cost = 0;

    public Product(String name, double purchasePrice, double sellPrice) {
        this.number = ++counter;
        this.name = name;

        if (purchasePrice < 0 || sellPrice < 0) {
            throw new IllegalArgumentException("Negative price!");
        }

        this.purchasePrice = purchasePrice;
        this.sellPrice = sellPrice;
        this.discountPrice = 0;
        this.nbItems = 0;
    }

    public void setCategoryId(int id) { this.number = id; }

    public String getName() {
        return name;
    }

    public int getStock() {
        return nbItems;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public double getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(double discountPrice) {
        this.discountPrice = discountPrice;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public double getCostPrice() {
        return purchasePrice;
    }

    public int getNbItems() {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT stock FROM Product WHERE name = ?")) {
            stmt.setString(1, this.name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.nbItems = rs.getInt("stock");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this.nbItems;
    }

    Map<String, Integer> productIdCache = new HashMap<>();
    public Integer getProductId() {
        String productName = getName().trim();
        if (productIdCache.containsKey(productName)) {
            return productIdCache.get(productName);
        }

        String sql = "SELECT product_id FROM Product WHERE name = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Integer productId = rs.getInt("product_id");
                productIdCache.put(productName, productId);
                return productId;
            } else {
                System.out.println("No product found with name: " + productName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    public void setPurchasePrice(double purchasePrice) {
        if (purchasePrice < 0) {
            throw new IllegalArgumentException("Purchase price cannot be negative.");
        }
        this.purchasePrice = purchasePrice;
        updateProductInDatabase();
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty.");
        }
        this.name = name;
        updateProductInDatabase();
    }

    public void setSellPrice(double sellPrice) {
        if (sellPrice < 0) {
            throw new IllegalArgumentException("Sell price cannot be negative.");
        }
        this.sellPrice = sellPrice;
        updateProductInDatabase();
    }

    public void setNbItems(int nbItems) {
        if (nbItems < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }
        this.nbItems = nbItems;
        updateProductInDatabase();
    }

    public void setStock(int nbItems) {
        if (nbItems < 0) {
            throw new IllegalArgumentException("Negative number of items!");
        }
        this.nbItems = nbItems;
        updateStockInDatabase();
    }

    public void sell(int nbItems) {
        if (this.nbItems < nbItems) {
            throw new IllegalArgumentException("Product unavailable");
        }
        this.nbItems -= nbItems;
        income += nbItems * this.sellPrice;
    }

    public void purchase(int nbItems) {
        if (nbItems < 0) {
            throw new IllegalArgumentException("Negative number of items!");
        }
        this.nbItems += nbItems;
        cost += nbItems * this.purchasePrice;
    }

    @Override
    public void applyDiscount(double discountPercentage) {
        if (discountPercentage > 0 && discountPercentage <= 100) {
            this.discountPrice = discountPercentage;
            this.sellPrice = this.sellPrice * (1 - discountPercentage / 100);
            updateSellPriceAndDiscountInDatabase();
        }
    }

    @Override
    public void removeDiscount() {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT discount_price FROM Product WHERE name = ?")) {
            stmt.setString(1, this.name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.discountPrice = rs.getDouble("discount_price");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (this.discountPrice > 0) {
            this.sellPrice = this.sellPrice / (1 - (this.discountPrice / 100));
            this.discountPrice = 0;
            updateSellPriceAndDiscountInDatabase();
            System.out.println("Discount removed. Restored selling price: " + this.sellPrice);
        } else {
            System.out.println("No discount to remove.");
        }
    }


    @Override
    public int compareTo(Product otherProduct) {
        return Double.compare(this.sellPrice, otherProduct.sellPrice);
    }

    @Override
    public String toString() {
        return "Product{" +
                "number=" + number +
                ", name='" + name + '\'' +
                ", sellPrice=" + sellPrice +
                ", discountPrice=" + discountPrice +
                ", nbItems=" + nbItems +
                '}';
    }

    protected void updateStockInDatabase() {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE Product SET stock = ? WHERE name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.nbItems);
            stmt.setString(2, this.name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateSellPriceAndDiscountInDatabase() {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE Product SET price = ?, discount_price = ? WHERE name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, this.sellPrice);
            stmt.setDouble(2, this.discountPrice);
            stmt.setString(3, this.name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void updateProductInDatabase() {
        Integer productId = getProductId();
        if (productId == null) {
            System.out.println("Cannot update: Product ID is null.");
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE product SET name = ?, cost_price = ?, price = ?, stock = ?, shoe_size = ?, clothing_size = ? WHERE product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, this.name);
                stmt.setDouble(2, this.purchasePrice);
                stmt.setDouble(3, this.sellPrice);
                stmt.setInt(4, this.nbItems);
                if (this instanceof Shoes) {
                    stmt.setInt(5, ((Shoes) this).getShoeSize());
                    stmt.setNull(6, java.sql.Types.INTEGER);
                } else if (this instanceof Clothes) {
                    stmt.setNull(5, java.sql.Types.INTEGER);
                    stmt.setInt(6, ((Clothes) this).getClothingSize());
                } else {
                    stmt.setNull(5, java.sql.Types.INTEGER);
                    stmt.setNull(6, java.sql.Types.INTEGER);
                }
                stmt.setInt(7, productId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}