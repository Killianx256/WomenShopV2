package com.example.womenshop.womenshopv2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Finance {
    private double capital;
    private double globalIncome;
    private double globalCost;

    public Finance() {
        loadFromDatabase();
    }

    public double getCapital() {
        return capital;
    }

    public double getGlobalIncome() {
        return globalIncome;
    }

    public double getGlobalCost() {
        return globalCost;
    }

    public void setCapital(double capital) {
        this.capital = capital;
    }

    public void setGlobalIncome(double globalIncome) {
        this.globalIncome = globalIncome;
    }

    public void setGlobalCost(double globalCost) {
        this.globalCost = globalCost;
    }

    public void addIncome(double income) {
        this.globalIncome += income;
        this.capital += income;
        updateDatabase();
    }

    public void addCost(double cost) {
        this.globalCost += cost;
        this.capital -= cost;
        updateDatabase();
    }

    public static int getFinancialId() {
        int financialId = -1;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT financial_id FROM Financial LIMIT 1");
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                financialId = rs.getInt("financial_id");
            }
        } catch (SQLException e) {
            System.err.println("Can't find financial_id : " + e.getMessage());
            e.printStackTrace();
        }
        return financialId;
    }

    public static void updateFinancialRecord(double income, double cost, int financialId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE Financial SET total_income = total_income + ?, total_cost = total_cost + ?, capital = capital + ? WHERE financial_id = ?")) {
            stmt.setDouble(1, income);
            stmt.setDouble(2, cost);
            stmt.setDouble(3, income - cost);
            stmt.setInt(4, financialId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error occurred while updating financial records : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateDatabase() {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE Financial SET total_income = ?, total_cost = ?, capital = ?")) {

            stmt.setDouble(1, this.globalIncome);
            stmt.setDouble(2, this.globalCost);
            stmt.setDouble(3, this.capital);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadFromDatabase() {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT total_income, total_cost, capital FROM Financial")) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.globalIncome = rs.getDouble("total_income");
                this.globalCost = rs.getDouble("total_cost");
                this.capital = rs.getDouble("capital");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void initializeInDatabase() {
        try (Connection conn = DatabaseManager.getConnection()) {

            String checkQuery = "SELECT COUNT(*) AS count FROM Financial";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt("count");

            if (count == 0) {
                String insertQuery = "INSERT INTO Financial (capital, total_income, total_cost) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setDouble(1, 10000.00);
                insertStmt.setDouble(2, 0.00);
                insertStmt.setDouble(3, 0.00);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void displayFinancials() {
        System.out.printf("Capital: %.2f, Global Income: %.2f, Global Cost: %.2f%n", capital, globalIncome, globalCost);
    }
}
