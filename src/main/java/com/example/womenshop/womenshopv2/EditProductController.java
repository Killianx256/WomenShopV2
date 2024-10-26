package com.example.womenshop.womenshopv2;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class EditProductController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField purchasePriceField;
    @FXML
    private TextField sellPriceField;
    @FXML
    private TextField stockField;
    @FXML
    private TextField sizeField;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    private Product product;
    private DatabaseManager dbManager;
    private Stage stage;
    private AdminController adminController;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setAdminController(AdminController adminController) {
        this.adminController = adminController;
    }


    @FXML
    private void initialize() {
        dbManager = new DatabaseManager();
        ObservableList<Product> products = dbManager.getAllProducts();
    }

    public void setProduct(Product product) {
        this.product = product;

        nameField.setText(product.getName());
        purchasePriceField.setText(String.valueOf(product.getPurchasePrice()));
        sellPriceField.setText(String.valueOf(product.getSellPrice()));
        stockField.setText(String.valueOf(product.getStock()));

        if (product.getProductId() == null) {
            System.out.println("Product ID is null. Ensure the product is loaded correctly.");
        }
    }

    @FXML
    private void onSaveChangesButtonClick() {
        if (product == null) {
            showAlert("Error", "No product selected for editing.");
            return;
        }
        String name = nameField.getText();
        double purchasePrice;
        double sellPrice;
        int stock;

        try {
            purchasePrice = Double.parseDouble(purchasePriceField.getText());
            sellPrice = Double.parseDouble(sellPriceField.getText());
            stock = Integer.parseInt(stockField.getText());
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for price and stock.");
            return;
        }

        product.setName(name);
        product.setPurchasePrice(purchasePrice);
        product.setSellPrice(sellPrice);
        product.setStock(stock);
        product.updateProductInDatabase();
        showAlert("Success", "Product updated successfully.");

        if (adminController != null) {
            adminController.refreshProducts();
        } else {
            System.err.println("AdminController is null. Cannot refresh products.");
        }

        onCancelButtonClick();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

    @FXML
    private void onCancelButtonClick() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

}
