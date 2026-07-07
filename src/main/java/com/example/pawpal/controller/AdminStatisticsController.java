package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminStatisticsController {

    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalBuyers;
    @FXML private Label lblTotalStoreOwners;
    @FXML private Label lblBlockedUsers;

    @FXML private Label lblTotalStores;
    @FXML private Label lblApprovedStores;
    @FXML private Label lblPendingStores;

    @FXML private Label lblTotalPets;
    @FXML private Label lblAvailablePets;
    @FXML private Label lblSoldPets;

    @FXML private Label lblTotalOrders;
    @FXML private Label lblCompletedOrders;
    @FXML private Label lblPendingOrders;
    @FXML private Label lblTotalRevenue;

    @FXML private Label lblPendingReports;

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    @FXML
    public void initialize() {
        loadStatistics();
    }

    private void loadStatistics() {

        try (Statement st = connection.createStatement()) {

            ResultSet rs;

            rs = st.executeQuery("SELECT COUNT(*) c FROM users");
            if (rs.next()) lblTotalUsers.setText(rs.getString("c"));

            rs = st.executeQuery("SELECT COUNT(*) c FROM users WHERE role='BUYER'");
            if (rs.next()) lblTotalBuyers.setText(rs.getString("c"));

            rs = st.executeQuery("SELECT COUNT(*) c FROM users WHERE role='STORE_OWNER'");
            if (rs.next()) lblTotalStoreOwners.setText(rs.getString("c"));

            rs = st.executeQuery("SELECT COUNT(*) c FROM users WHERE account_status='BLOCKED'");
            if (rs.next()) lblBlockedUsers.setText(rs.getString("c"));

            rs = st.executeQuery("SELECT COUNT(*) c FROM stores");
            if (rs.next()) lblTotalStores.setText(rs.getString("c"));

            rs = st.executeQuery("SELECT COUNT(*) c FROM stores WHERE approval_status='APPROVED'");
            if (rs.next()) lblApprovedStores.setText(rs.getString("c"));

            rs = st.executeQuery("SELECT COUNT(*) c FROM stores WHERE approval_status='PENDING'");
            if (rs.next()) lblPendingStores.setText(rs.getString("c"));

            rs = st.executeQuery("SELECT COUNT(*) c FROM pets");
            if (rs.next()) lblTotalPets.setText(rs.getString("c"));

            rs = st.executeQuery("SELECT COUNT(*) c FROM pets WHERE availability_status='AVAILABLE'");
            if (rs.next()) lblAvailablePets.setText(rs.getString("c"));

            rs = st.executeQuery("SELECT COUNT(*) c FROM pets WHERE availability_status='SOLD'");
            if (rs.next()) lblSoldPets.setText(rs.getString("c"));

            rs = st.executeQuery("SELECT COUNT(*) c FROM orders");
            if (rs.next()) lblTotalOrders.setText(rs.getString("c"));

            rs = st.executeQuery("SELECT COUNT(*) c FROM orders WHERE order_status='Completed'");
            if (rs.next()) lblCompletedOrders.setText(rs.getString("c"));

            rs = st.executeQuery("SELECT COUNT(*) c FROM orders WHERE order_status='Pending'");
            if (rs.next()) lblPendingOrders.setText(rs.getString("c"));

            rs = st.executeQuery(
                    "SELECT COALESCE(SUM(p.price),0) total FROM orders o JOIN pets p ON o.pet_id=p.pet_id WHERE o.order_status='Completed'");
            if (rs.next()) lblTotalRevenue.setText(String.format("%.2f BDT", rs.getDouble("total")));

            rs = st.executeQuery("SELECT COUNT(*) c FROM reports WHERE report_status='Pending'");
            if (rs.next()) lblPendingReports.setText(rs.getString("c"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void refresh(ActionEvent e) {
        loadStatistics();
    }

    @FXML
    private void goBack(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/AdminDashboard.fxml");
    }
}
