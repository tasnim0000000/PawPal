package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import com.example.pawpal.session.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SellerDashboardController {

    @FXML private Label lblSeller;
    @FXML private Label lblPets;
    @FXML private Label lblPendingOrders;
    @FXML private Label lblSales;
    @FXML private Label lblRating;
    @FXML private ListView<String> listRecentPets;

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    private int storeId = -1;

    @FXML
    public void initialize() {

        lblSeller.setText("Welcome, " + Session.getCurrentUser().getFullName());

        try {
            PreparedStatement storePst = connection.prepareStatement(
                    "SELECT store_id, average_rating, approval_status FROM stores WHERE owner_id=?");
            storePst.setInt(1, Session.getCurrentUser().getUserId());
            ResultSet storeRs = storePst.executeQuery();

            if (storeRs.next()) {
                storeId = storeRs.getInt("store_id");
                String status = storeRs.getString("approval_status");
                lblRating.setText("APPROVED".equalsIgnoreCase(status)
                        ? String.format("%.1f \u2605", storeRs.getDouble("average_rating"))
                        : status);
            } else {
                lblRating.setText("No Store");
            }

            if (storeId != -1) {

                PreparedStatement pets = connection.prepareStatement("SELECT COUNT(*) c FROM pets WHERE store_id=?");
                pets.setInt(1, storeId);
                ResultSet petsRs = pets.executeQuery();
                if (petsRs.next()) lblPets.setText(petsRs.getString("c"));

                PreparedStatement pending = connection.prepareStatement(
                        "SELECT COUNT(*) c FROM orders o JOIN pets p ON o.pet_id=p.pet_id " +
                                "WHERE p.store_id=? AND o.order_status='Pending'");
                pending.setInt(1, storeId);
                ResultSet pendingRs = pending.executeQuery();
                if (pendingRs.next()) lblPendingOrders.setText(pendingRs.getString("c"));

                PreparedStatement sales = connection.prepareStatement(
                        "SELECT COUNT(*) c FROM orders o JOIN pets p ON o.pet_id=p.pet_id " +
                                "WHERE p.store_id=? AND o.order_status='Completed'");
                sales.setInt(1, storeId);
                ResultSet salesRs = sales.executeQuery();
                if (salesRs.next()) lblSales.setText(salesRs.getString("c"));

                PreparedStatement recent = connection.prepareStatement(
                        "SELECT pet_name, pet_type, availability_status FROM pets WHERE store_id=? ORDER BY created_at DESC LIMIT 5");
                recent.setInt(1, storeId);
                ResultSet recentRs = recent.executeQuery();
                while (recentRs.next()) {
                    listRecentPets.getItems().add(
                            recentRs.getString("pet_name") + " (" + recentRs.getString("pet_type") + ") - " +
                                    recentRs.getString("availability_status"));
                }

            } else {
                lblPets.setText("0");
                lblPendingOrders.setText("0");
                lblSales.setText("0");
                listRecentPets.getItems().add("You don't have a store yet. Go to Store Profile to apply.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void openDashboard(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/SellerDashboard.fxml");
    }

    @FXML
    private void openAddPet(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/AddPet.fxml");
    }

    @FXML
    private void openMyPets(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/MyPets.fxml");
    }

    @FXML
    private void openOrders(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/SellerOrders.fxml");
    }

    @FXML
    private void openStoreProfile(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/StoreProfile.fxml");
    }

    @FXML
    private void openSales(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/Sales.fxml");
    }

    @FXML
    private void logout(ActionEvent e) throws Exception {
        Session.logout();
        HelloApplication.changeScene("/fxml/Login.fxml");
    }
}
