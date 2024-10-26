package com.example.womenshop.womenshopv2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TransactionHistoryController {

    @FXML
    private TableView<Transaction> transactionTableView;
    @FXML
    private TableColumn<Transaction, Integer> transactionIdColumn;
    @FXML
    private TableColumn<Transaction, Integer> productIdColumn;
    @FXML
    private TableColumn<Transaction, String> dateColumn;
    @FXML
    private TableColumn<Transaction, Integer> quantityColumn;
    @FXML
    private TableColumn<Transaction, Double> totalPriceColumn;

    @FXML
    private TableColumn<Transaction, Double> transactionTypeColumn;

    private DatabaseManager dbManager;

    @FXML
    private void initialize() {
        dbManager = new DatabaseManager();
        setupTableColumns();
        loadTransactionData();
    }

    private void setupTableColumns() {
        transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        transactionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("priceAtTransaction"));
    }
    private void loadTransactionData() {
        ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
        try (Connection connection = dbManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT transaction_id, product_id, transaction_type, quantity, price_at_transaction, transaction_date FROM transaction")) {

            while (resultSet.next()) {
                int transactionId = resultSet.getInt("transaction_id");
                int productId = resultSet.getInt("product_id");
                String transactionType = resultSet.getString("transaction_type");
                int quantity = resultSet.getInt("quantity");
                double priceAtTransaction = resultSet.getDouble("price_at_transaction");
                String transactionDate = resultSet.getString("transaction_date");

                Transaction transaction = new Transaction(productId, transactionType, quantity, priceAtTransaction);
                transaction.setTransactionId(transactionId);
                transaction.setTransactionDate(transactionDate);

                transactionList.add(transaction);
            }
            transactionTableView.setItems(transactionList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackToAdminViewClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("admin-view.fxml"));
            Stage stage = (Stage) transactionTableView.getScene().getWindow();
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
