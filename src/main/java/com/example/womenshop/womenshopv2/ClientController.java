package com.example.womenshop.womenshopv2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.function.UnaryOperator;

public class ClientController {

    @FXML
    private Label financialInfoLabel;

    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private ComboBox<Product> productComboBox;
    @FXML
    private TextField quantityTextField;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private Button backToWelcomePageButton;

    private ObservableList<String> categories;
    private ObservableList<Product> productList;
    private Finance finance;
    private DatabaseManager dbManager;

    @FXML
    public void initialize() {
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("-?\\d*")) {
                return change;
            }
            return null;
        };
        quantityTextField.setTextFormatter(new TextFormatter<>(integerFilter));
        loadFinancialInfo();
        finance = new Finance();
        categories = FXCollections.observableArrayList("Shoes", "Clothes", "Accessories");
        categoryComboBox.setItems(categories);
        categoryComboBox.setOnAction(event -> loadProductsByCategory());
    }

    private void loadProductsByCategory() {
        String selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            int categoryId = mapCategoryToId(selectedCategory);
            DatabaseManager dbManager = new DatabaseManager();
            productList = dbManager.getProductsByCategory(categoryId);
            productComboBox.setItems(productList);
        }
    }

    private int mapCategoryToId(String category) {
        switch (category) {
            case "Shoes":
                return 1;
            case "Clothes":
                return 2;
            case "Accessories":
                return 3;
            default:
                return 0;
        }
    }

    @FXML
    private void updateTotalPrice() {
        Product selectedProduct = productComboBox.getSelectionModel().getSelectedItem();
        String quantityText = quantityTextField.getText();

        if (selectedProduct != null && !quantityText.isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityText);
                if (quantity > 0) {
                    double totalPrice = quantity * selectedProduct.getSellPrice();
                    totalPriceLabel.setText(String.format("Total Price : %.2f", totalPrice));
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Error", "Enter a valid quantity.");
            }
        }
    }

    @FXML
    private void onProductSelected() {
        Product selectedProduct = productComboBox.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            updateTotalPrice();
        }
    }

    @FXML
    private void onConfirmPurchase() {
        Product selectedProduct = productComboBox.getSelectionModel().getSelectedItem();
        String quantityText = quantityTextField.getText();

        if (selectedProduct != null && !quantityText.isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityText);

                if (quantity <= 0) {
                    showErrorAlert("Quantity Error", "Please enter a valid positive number.");
                    return;
                }

                if (quantity > selectedProduct.getStock()) {
                    showErrorAlert("Stock Error", "Insufficient stock for this product.");
                    return;
                }

                double totalPrice = quantity * selectedProduct.getSellPrice();

                Transaction transaction = new Transaction(
                        selectedProduct.getProductId(),
                        "sale",
                        quantity,
                        selectedProduct.getSellPrice()
                );

                transaction.saveToDatabase();

                updateStock(selectedProduct, quantity);

                updateFinancials(totalPrice);

                showConfirmationAlert("Success", "Purchase confirm !");
                updateTotalPrice();

            } catch (NumberFormatException e) {
                showErrorAlert("Format Error", "Enter a valid number.");
            }
        } else {
            showErrorAlert("Error", "Please select a product and enter a quantity.");
        }
        loadFinancialInfo();
        loadProductsByCategory();
        quantityTextField.clear();
    }


    private boolean checkStock(Product product, int quantity) {
        if (product.getStock() >= quantity) {
            return true;
        } else {
            showErrorAlert("Stock Error", "Insufficient stock for this product.");
            return false;
        }
    }

    private void updateStock(Product product, int quantity) {
        product.setStock(product.getStock() - quantity);
        DatabaseManager.updateProductStock(product.getProductId(), product.getStock());
    }

    private void updateFinancials(double income) {
        int financialId = Finance.getFinancialId();
        if (financialId != -1) {
            Finance.updateFinancialRecord(income, 0, financialId);
        } else {
            showErrorAlert("Error", "Can't retrieve financial record.");
        }
    }


    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showConfirmationAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onBackToWelcomePageButtonClick() {
        loadView("hello-view.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Stage stage = (Stage) backToWelcomePageButton.getScene().getWindow();
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFinancialInfo() {
        try (Connection connection = dbManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT capital, total_income, total_cost FROM financial")) {

            if (resultSet.next()) {
                double capital = resultSet.getDouble("capital");
                double totalIncome = resultSet.getDouble("total_income");
                double totalCost = resultSet.getDouble("total_cost");
                financialInfoLabel.setText(String.format("Capital: %.2f, Global Income: %.2f, Global Cost: %.2f", capital, totalIncome, totalCost));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Finance.initializeInDatabase();
    }
}
