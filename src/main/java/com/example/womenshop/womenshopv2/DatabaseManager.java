package com.example.womenshop.womenshopv2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class DatabaseManager {

    private static final String URL = "jdbc:mysql://localhost:3306/WomenShopDB";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public ObservableList<Product> getProductsByCategory(int categoryId) {
        ObservableList<Product> products = FXCollections.observableArrayList();

        String query = "SELECT * FROM product WHERE category_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String name = rs.getString("name");
                int stock = rs.getInt("stock");
                double sellPrice = rs.getDouble("price");
                double purchasePrice = rs.getDouble("cost_price");
                Integer shoeSize = (rs.getObject("shoe_size") != null) ? rs.getInt("shoe_size") : null;
                Integer clothingSize = (rs.getObject("clothing_size") != null) ? rs.getInt("clothing_size") : null;
                double discountPrice = rs.getDouble("discount_price");
                int categoryIdFromDB = rs.getInt("category_id");

                Product product;
                if (shoeSize != null) {
                    product = new Shoes(name, purchasePrice, sellPrice, shoeSize);
                } else if (clothingSize != null) {
                    product = new Clothes(name, purchasePrice, sellPrice, clothingSize);
                } else {
                    product = new Accessories(name, purchasePrice, sellPrice);
                }

                product.setCategoryId(categoryIdFromDB);

                if (discountPrice > 0) {
                    product.setDiscountPrice(discountPrice);
                }

                product.setStock(stock);
                products.add(product);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public ObservableList<Product> getAllProducts() {
        ObservableList<Product> products = FXCollections.observableArrayList();

        String query = "SELECT * FROM product";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String name = rs.getString("name");
                int categoryId = rs.getInt("category_id");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");
                double cost_price = rs.getDouble("cost_price");
                Integer shoeSize = (Integer) rs.getObject("shoe_size");
                Integer clothingSize = (Integer) rs.getObject("clothing_size");
                double discountPrice = rs.getDouble("discount_price");

                Product product;

                if (categoryId == 1) {
                    product = new Shoes(name, cost_price, price, shoeSize);
                } else if (categoryId == 2) {
                    product = new Clothes(name,cost_price, price, clothingSize);
                } else if (categoryId == 3) {
                    product = new Accessories(name, cost_price, price);
                } else {
                    continue;
                }

                product.setNbItems(stock);
                product.setDiscountPrice(discountPrice);
                products.add(product);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }


    protected void deleteProductFromDatabase(Product product) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM product WHERE product_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, product.getProductId());
                pstmt.executeUpdate();
            }
        }
    }


    public static void updateProductStock(int productId, int newStock) {
        String query = "UPDATE product SET stock = ? WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, newStock);
            stmt.setInt(2, productId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
