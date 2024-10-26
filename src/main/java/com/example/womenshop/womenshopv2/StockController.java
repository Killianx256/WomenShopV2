package com.example.womenshop.womenshopv2;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StockController {

    @FXML
    private Label productNameLabel;

    @FXML
    private TextField quantityTextField;

    private Product selectedProduct;

    public void initialize() {
        productNameLabel.setText("Product: " + selectedProduct.getName());
    }

    public void setProduct(Product product) {
        this.selectedProduct = product;
    }

    @FXML
    private void onAddStock() {
        try {
            int quantity = Integer.parseInt(quantityTextField.getText());
            selectedProduct.purchase(quantity);
            System.out.println("Added " + quantity + " items to stock of product: " + selectedProduct.getName());
            updateStockInDatabase(selectedProduct, quantity, true);
            closeWindow();
        } catch (NumberFormatException e) {
            System.err.println("Invalid quantity entered.");
        }
    }

    @FXML
    private void onRemoveStock() {
        try {
            int quantity = Integer.parseInt(quantityTextField.getText());
            if (selectedProduct.getStock() >= quantity) {
                selectedProduct.sell(quantity);
                System.out.println("Removed " + quantity + " items from stock of product: " + selectedProduct.getName());
                updateStockInDatabase(selectedProduct, quantity, false);
                closeWindow();
            } else {
                System.err.println("Not enough stock available.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid quantity entered.");
        }
    }

    private void updateStockInDatabase(Product product, int quantity, boolean addStock) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String sql;
            if (addStock) {
                sql = "UPDATE product SET stock = stock + ? WHERE product_id = ?";
            } else {
                sql = "UPDATE product SET stock = stock - ? WHERE product_id = ?";
            }

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, quantity);
            stmt.setInt(2, product.getProductId());

            stmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) quantityTextField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }
}
