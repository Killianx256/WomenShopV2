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

public class AddProductController {
    private ObservableList<String> categories;
    @FXML
    private ComboBox<String> productChoiceCBox;
    @FXML
    private TextField nameField;
    @FXML
    private TextField purchasePriceField;
    @FXML
    private TextField sellPriceField;
    @FXML
    private TextField sizeField;
    @FXML
    private TextField stockField;
    @FXML
    private Button validationButton;
    @FXML
    private Button cancelButton;
    private DatabaseManager dbManager;
    private Finance finance;

    @FXML
    public void initialize() {
        categories = FXCollections.observableArrayList("Shoes", "Clothes", "Accessories");
        productChoiceCBox.setItems(categories);
        productChoiceCBox.setOnAction(event -> changeFieldFormatIfProduct());

        disableAllFields();

        loadFinancialInfo();
    }

    private void disableAllFields() {
        nameField.setDisable(true);
        purchasePriceField.setDisable(true);
        sellPriceField.setDisable(true);
        sizeField.setDisable(true);
        stockField.setDisable(true);
        validationButton.setDisable(true);
    }

    private void changeFieldFormatIfProduct() {
        String selectedCategory = productChoiceCBox.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            int categoryId = mapCategoryToId(selectedCategory);
            UnaryOperator<TextFormatter.Change> integerFilter = change -> {
                String newText = change.getControlNewText();
                return newText.matches("\\d*") ? change : null;
            };

            nameField.setDisable(false);
            purchasePriceField.setDisable(false);
            sellPriceField.setDisable(false);
            stockField.setDisable(false);
            validationButton.setDisable(false);

            if (categoryId == 1) {
                sizeField.setDisable(false);
                sizeField.setPromptText("Enter shoe size (36-50)");
                sizeField.setTextFormatter(new TextFormatter<>(integerFilter));
            } else if (categoryId == 2) {
                sizeField.setDisable(false);
                sizeField.setPromptText("Enter clothing size (34-54)");
                sizeField.setTextFormatter(new TextFormatter<>(integerFilter));
            } else {
                sizeField.setDisable(true);
                sizeField.clear();
            }
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
    private void onValidationButtonClick() {
        try {
            String selectedCategory = productChoiceCBox.getSelectionModel().getSelectedItem();
            String name = nameField.getText();
            double purchasePrice = Double.parseDouble(purchasePriceField.getText());
            double sellPrice = Double.parseDouble(sellPriceField.getText());
            int stock = Integer.parseInt(stockField.getText());
            if (purchasePrice < 0 || sellPrice < 0 || stock < 0) {
                showAlert("Error", "Some values are negative.");
                return;
            }
            int size = 0;

            if (selectedCategory.equals("Shoes") || selectedCategory.equals("Clothes")) {
                size = Integer.parseInt(sizeField.getText());
                if ((selectedCategory.equals("Shoes") && (size < 36 || size > 50)) ||
                        (selectedCategory.equals("Clothes") && (size < 34 || size > 54))) {
                    showAlert("Error", "Size must be within the valid range.");
                    return;
                }
            }

            int categoryId = mapCategoryToId(selectedCategory);
            Product product;
            switch (categoryId) {
                case 1: product = new Shoes(name, purchasePrice, sellPrice, size); break;
                case 2: product = new Clothes(name, purchasePrice, sellPrice, size); break;
                case 3: default: product = new Accessories(name, purchasePrice, sellPrice); break;
            }
            product.setNbItems(stock);
            insertProductIntoDatabase(product, categoryId, size, stock, purchasePrice, sellPrice);

            double totalCost = purchasePrice * stock;
            Transaction transaction = new Transaction(product.getProductId(), "purchase", stock, purchasePrice);
            transaction.saveToDatabase();

            finance.addCost(totalCost);

            showAlert("Success", "Product added successfully.");
            onCancelButtonClick();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers.");
        } catch (SQLException e) {
            showAlert("Error", "Failed to add product to the database.");
            e.printStackTrace();
        }
    }

    private void insertProductIntoDatabase(Product product, int categoryId, int size, int stock, double purchasePrice, double sellPrice) throws SQLException {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "INSERT INTO product (name, category_id, price, stock, cost_price, shoe_size, clothing_size, discount_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, product.getName());
                pstmt.setInt(2, categoryId);
                pstmt.setDouble(3, sellPrice);
                pstmt.setInt(4, stock);
                pstmt.setDouble(5, purchasePrice);

                if (categoryId == 1) {
                    pstmt.setInt(6, size);
                    pstmt.setNull(7, java.sql.Types.INTEGER);
                } else if (categoryId == 2) {
                    pstmt.setNull(6, java.sql.Types.INTEGER);
                    pstmt.setInt(7, size);
                } else {
                    pstmt.setNull(6, java.sql.Types.INTEGER);
                    pstmt.setNull(7, java.sql.Types.INTEGER);
                }

                pstmt.setDouble(8, product.getDiscountPrice());
                pstmt.executeUpdate();
            }
        }
    }

    private void loadFinancialInfo() {
        try (Connection connection = dbManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT capital, total_income, total_cost FROM Financial")) {

            if (resultSet.next()) {
                double capital = resultSet.getDouble("capital");
                double totalIncome = resultSet.getDouble("total_income");
                double totalCost = resultSet.getDouble("total_cost");

                finance = new Finance();
                finance.setCapital(capital);
                finance.setGlobalIncome(totalIncome);
                finance.setGlobalCost(totalCost);

            } else {
                showAlert("Error", "Failed to retrieve financial data from the database.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateFinancials(double cost) {
        int financialId = Finance.getFinancialId();
        if (financialId != -1) {
            Finance.updateFinancialRecord(0, cost, financialId);
        } else {
            showAlert("Error", "Unable to retrieve financial information.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
}
