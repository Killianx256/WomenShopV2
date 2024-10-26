package com.example.womenshop.womenshopv2;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class AdminController {

    @FXML
    private Label financialInfoLabel;
    @FXML
    private Label stockInfoLabel;
    @FXML
    private TableView<Product> stockTableView;
    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private Button addProductButton;
    @FXML
    private Button addStockButton;
    @FXML
    private Button removeStockButton;
    @FXML
    private Button sellProductButton;
    @FXML
    private Button applyDiscountButton;
    @FXML
    private Button removeDiscountButton;
    @FXML
    private Button backToWelcomePageButton;

    @FXML
    private TableColumn<Product, Integer> productIdColumn;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, Integer> categoryIdColumn;
    @FXML
    private TableColumn<Product, Integer> stockColumn;
    @FXML
    private TableColumn<Product, Double> purchasePriceColumn;
    @FXML
    private TableColumn<Product, Double> sellPriceColumn;
    @FXML
    private TableColumn<Shoes, Integer> shoeSizeColumn;
    @FXML
    private TableColumn<Clothes, Integer> clothingSizeColumn;
    @FXML
    private TableColumn<Product, Double> discountPriceColumn;


    private DatabaseManager dbManager;
    private Finance finance;

    @FXML
    private void initialize() {
        dbManager = new DatabaseManager();
        finance = new Finance();
        loadFinancialInfo();
        setupTableColumns();
        loadCategories();
        categoryComboBox.setOnAction(event -> loadProductsByCategory());
    }

    private void setupTableColumns() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryIdColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Shoes) {
                return new SimpleIntegerProperty(((Shoes) cellData.getValue()).getCategoryId()).asObject();
            } else if (cellData.getValue() instanceof Clothes) {
                return new SimpleIntegerProperty(((Clothes) cellData.getValue()).getCategoryId()).asObject();
            } else if (cellData.getValue() instanceof Accessories) {
                return new SimpleIntegerProperty(((Accessories) cellData.getValue()).getCategoryId()).asObject();
            } else {
                return null;
            }
        });
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("nbItems"));
        purchasePriceColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        sellPriceColumn.setCellValueFactory(new PropertyValueFactory<>("sellPrice"));
        discountPriceColumn.setCellValueFactory(new PropertyValueFactory<>("discountPrice"));
        shoeSizeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Shoes) {
                return new SimpleObjectProperty<>(((Shoes) cellData.getValue()).getShoeSize());
            } else {
                return new SimpleObjectProperty<>(null);
            }
        });

        clothingSizeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Clothes) {
                return new SimpleObjectProperty<>(((Clothes) cellData.getValue()).getClothingSize());
            } else {
                return new SimpleObjectProperty<>(null);
            }
        });

    }

    private void loadFinancialInfo() {
        Finance.initializeInDatabase();
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
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadCategories() {
        ObservableList<String> categories = FXCollections.observableArrayList("All", "Shoes", "Clothes", "Accessories");
        categoryComboBox.setItems(categories);
    }

    private void loadProductsByCategory() {
        String selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            ObservableList<Product> productList;

            if (selectedCategory.equals("All")) {
                productList = dbManager.getAllProducts();
            } else {
                int categoryId = mapCategoryToId(selectedCategory);
                productList = dbManager.getProductsByCategory(categoryId);
            }

            stockTableView.setItems(FXCollections.observableArrayList(productList));
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
    private void onAddProductButtonClick() {
        loadView("add-product-view.fxml");
    }

    @FXML
    private void onAddStock() {
        Product selectedProduct = stockTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Stock");
            dialog.setHeaderText("Add stock to product: " + selectedProduct.getName());
            dialog.setContentText("Enter quantity to add:");

            TextField inputField = dialog.getEditor();
            inputField.setTextFormatter(new TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("[0-9]*")) {
                    return change;
                } else {
                    return null;
                }
            }));

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(quantityStr -> {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    if (quantity > 0) {
                        double purchaseCost = selectedProduct.getPurchasePrice() * quantity;

                        selectedProduct.purchase(quantity);
                        updateFinancials(purchaseCost, true);
                        updateStockInDatabase(selectedProduct, quantity, true);

                        Transaction transaction = new Transaction(
                                selectedProduct.getProductId(),
                                "purchase",
                                quantity,
                                selectedProduct.getPurchasePrice()
                        );
                        transaction.saveToDatabase();
                        refreshProducts();
                    } else {
                        showErrorDialog("Please enter a positive number.");
                    }
                } catch (NumberFormatException e) {
                    showErrorDialog("Invalid input. Please enter a valid positive number.");
                }
            });
        } else {
            showAlert("Error", "No product selected.");
        }
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateFinancials(double amount, boolean isCost) {
        try {
            if (isCost) {
                finance.addCost(amount);
                System.out.println("Added cost: " + amount);
            } else {
                finance.addIncome(amount);
                System.out.println("Added income: " + amount);
            }
            loadFinancialInfo();
        } catch (Exception e) {
            System.err.println("Error updating financials: " + e.getMessage());
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

    @FXML
    private void onSellProduct() {
        Product selectedProduct = stockTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Sell Product");
            dialog.setHeaderText("Sell stock of product: " + selectedProduct.getName());
            dialog.setContentText("Enter quantity to sell:");
            TextField inputField = dialog.getEditor();
            inputField.setTextFormatter(new TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("[0-9]*")) {
                    return change;
                } else {
                    return null;
                }
            }));

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(quantityStr -> {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    if (quantity > 0 && selectedProduct.getStock() >= quantity) {
                        double revenue = selectedProduct.getSellPrice() * quantity;
                        selectedProduct.sell(quantity);
                        updateStockInDatabase(selectedProduct, quantity, false);
                        Transaction transaction = new Transaction(
                                selectedProduct.getProductId(),
                                "sale",
                                quantity,
                                selectedProduct.getSellPrice()
                        );
                        transaction.saveToDatabase();
                        updateFinancials(revenue, false);

                        refreshProducts();
                    } else if (quantity > selectedProduct.getStock()) {
                        showErrorDialog("Not enough stock available.");
                    } else {
                        showErrorDialog("Please enter a positive number.");
                    }
                } catch (NumberFormatException e) {
                    showErrorDialog("Invalid input. Please enter a valid positive number.");
                }
            });
        } else {
            showAlert("Error", "No product selected.");
        }
    }

    @FXML
    private void onDeleteProduct() {
        Product selectedProduct = stockTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirmation");
            confirmationAlert.setHeaderText("Are you sure you want to delete this product?");
            confirmationAlert.setContentText("This action cannot be undone.");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {

                    dbManager.deleteProductFromDatabase(selectedProduct);


                    refreshProducts();

                } catch (SQLException e) {
                    showErrorDialog("Failed to delete the product from the database.");
                    e.printStackTrace();
                }
            }
        } else {
            showErrorDialog("No product selected.");
        }
    }

    @FXML
    private void onEditProductButtonClick() {
        Product selectedProduct = stockTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("edit-product-view.fxml"));
                Parent root = loader.load();

                EditProductController controller = loader.getController();
                controller.setProduct(selectedProduct);
                controller.setAdminController(this);

                Stage stage = new Stage();
                stage.setTitle("Edit Product");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showErrorDialog("Please select a product to edit.");
        }
    }


    @FXML
    private void onApplyDiscountButtonClick() {
        loadView("ApplyDiscountView.fxml");
    }

    @FXML
    private void onRemoveDiscountButtonClick() {
        Product selectedProduct = stockTableView.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            if (selectedProduct.getDiscountPrice() > 0) {
                double oldPrice = selectedProduct.getSellPrice();

                selectedProduct.removeDiscount();
                double newPrice = selectedProduct.getSellPrice();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Discount Removed");
                alert.setHeaderText("Discount removed from product: " + selectedProduct.getName());
                alert.setContentText("Old Price: " + oldPrice + " €\nNew Price: " + newPrice + " €");
                alert.showAndWait();

                refreshProducts();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("No Discount");
                alert.setHeaderText("No discount available");
                alert.setContentText("The selected product does not have a discount to remove.");
                alert.showAndWait();
            }
        } else {
            showAlert("Error", "No product selected.");
        }
    }

    protected void refreshProducts() {
        loadProductsByCategory();
    }

    @FXML
    private void onBackToWelcomePageButtonClick() {
        loadView("hello-view.fxml");
    }

    @FXML
    private void onViewTransaction() {
        loadView("transaction.fxml");
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
}
