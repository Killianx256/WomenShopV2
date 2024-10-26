package com.example.womenshop.womenshopv2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Transaction {
    private int transactionId;
    private int productId;
    private String transactionType;
    private int quantity;
    private double priceAtTransaction;
    private String transactionDate;

    public Transaction(int productId, String transactionType, int quantity, double priceAtTransaction) {
        this.productId = productId;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.priceAtTransaction = priceAtTransaction;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getProductId() {
        return productId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPriceAtTransaction() {
        return priceAtTransaction;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void saveToDatabase() {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO Transaction (product_id, transaction_type, quantity, price_at_transaction) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, productId);
            stmt.setString(2, transactionType);
            stmt.setInt(3, quantity);
            stmt.setDouble(4, priceAtTransaction);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
