package com.example.womenshop.womenshopv2;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ApplyDiscountController {

    @FXML
    private ComboBox<Product> productComboBox;

    @FXML
    private TextField discountPercentageField;
    @FXML
    private Button cancelButton;
    @FXML
    private VBox productInfoBox;
    @FXML
    private Label productNameLabel;
    @FXML
    private Label productPriceLabel;
    @FXML
    private Label productStockLabel;

    @FXML
    private void onApplyDiscountButtonClick() {
        Product selectedProduct = productComboBox.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            try {
                double discountPercentage = Double.parseDouble(discountPercentageField.getText());
                selectedProduct.applyDiscount(discountPercentage);
                System.out.println("Discount applied to product: " + selectedProduct.getName() + " - " + discountPercentage + "%");
                onCancelButtonClick();
            } catch (NumberFormatException e) {
                System.err.println("Invalid discount percentage entered.");
            }
        } else {
            System.err.println("No product selected.");
        }
    }

    @FXML
    private void onCancelButtonClick() {
        loadView("admin-view.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize() {
        loadProductsFromDatabase();
    }

    private void loadProductsFromDatabase() {
        String query = "SELECT product_id, name, price, stock, cost_price, clothing_size, shoe_size, category_id FROM product";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("name");
                double productPrice = rs.getDouble("price");
                int stock = rs.getInt("stock");
                double cost_price = rs.getDouble("cost_price");
                Integer clothingSize = (Integer) rs.getObject("clothing_size");
                Integer shoeSize = (Integer) rs.getObject("shoe_size");
                int categoryId = rs.getInt("category_id");

                Product product;

                if (categoryId == 1) {
                    product = new Shoes(productName, cost_price, productPrice, shoeSize);
                } else if (categoryId == 2) {
                    product = new Clothes(productName, cost_price, productPrice, clothingSize);
                } else if (categoryId == 3) {
                    product = new Accessories(productName, cost_price, productPrice);
                } else {
                    continue;
                }

                product.setNbItems(stock);

                productComboBox.getItems().add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onProductSelected() {
        Product selectedProduct = productComboBox.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            productNameLabel.setText("Product Name: " + selectedProduct.getName());
            productPriceLabel.setText("Price: " + selectedProduct.getSellPrice() + " €");
            productStockLabel.setText("Stock: " + selectedProduct.getStock());

            productInfoBox.setVisible(true);
        } else {
            productInfoBox.setVisible(false);
        }
    }

}
