package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import com.example.pawpal.session.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminDashboardController {

    @FXML private Label lblAdmin;
    @FXML private Label lblPendingStores;
    @FXML private Label lblReports;
    @FXML private Label lblUsers;
    @FXML private Label lblPets;

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    @FXML
    public void initialize() {

        lblAdmin.setText("Welcome, " + Session.getCurrentUser().getFullName());

        try (Statement st = connection.createStatement()) {

            ResultSet rs = st.executeQuery("SELECT COUNT(*) c FROM stores WHERE approval_status='PENDING'");
            if (rs.next()) lblPendingStores.setText(rs.getString("c"));

            rs = st.executeQuery("SELECT COUNT(*) c FROM reports WHERE report_status='Pending'");
            if (rs.next()) lblReports.setText(rs.getString("c"));

            rs = st.executeQuery("SELECT COUNT(*) c FROM users");
            if (rs.next()) lblUsers.setText(rs.getString("c"));

            rs = st.executeQuery("SELECT COUNT(*) c FROM pets");
            if (rs.next()) lblPets.setText(rs.getString("c"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void openDashboard(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/AdminDashboard.fxml");
    }

    @FXML
    private void openStores(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/AdminStores.fxml");
    }

    @FXML
    private void openPets(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/AdminPets.fxml");
    }

    @FXML
    private void openReports(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/AdminReports.fxml");
    }

    @FXML
    private void openUsers(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/AdminUsers.fxml");
    }

    @FXML
    private void openStatistics(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/AdminStatistics.fxml");
    }

    @FXML
    private void logout(ActionEvent e) throws Exception {
        Session.logout();
        HelloApplication.changeScene("/fxml/Login.fxml");
    }
}
