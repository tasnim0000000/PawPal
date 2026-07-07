package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import com.example.pawpal.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminStoresController {

    @FXML private TableView<ObservableList<String>> tableStores;
    @FXML private TableColumn<ObservableList<String>, String> colId, colName, colOwner, colEmail,
            colAddress, colPhone, colRating, colStatus;

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colOwner.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        colAddress.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(5)));
        colRating.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(6)));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(7)));

        loadStores(null);
    }

    @FXML
    public void loadStores(ActionEvent e) {

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        String sql = "SELECT s.store_id, s.store_name, u.full_name AS owner_name, u.email, s.address, " +
                "s.phone, s.average_rating, s.approval_status " +
                "FROM stores s JOIN users u ON s.owner_id = u.user_id " +
                "ORDER BY (s.approval_status = 'PENDING') DESC, s.created_at DESC";

        try {
            PreparedStatement pst = connection.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("store_id")));
                row.add(rs.getString("store_name"));
                row.add(rs.getString("owner_name"));
                row.add(rs.getString("email"));
                row.add(rs.getString("address"));
                row.add(rs.getString("phone"));
                row.add(String.format("%.1f", rs.getDouble("average_rating")));
                row.add(rs.getString("approval_status"));
                data.add(row);
            }

            tableStores.setItems(data);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void approveStore(ActionEvent e) {
        updateStatus("APPROVED");
    }

    @FXML
    private void rejectStore(ActionEvent e) {
        updateStatus("REJECTED");
    }

    private void updateStatus(String status) {

        ObservableList<String> selected = tableStores.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showError("No Selection", "Please select a store first.");
            return;
        }

        int storeId = Integer.parseInt(selected.get(0));

        try {
            PreparedStatement pst = connection.prepareStatement(
                    "UPDATE stores SET approval_status=? WHERE store_id=?");
            pst.setString(1, status);
            pst.setInt(2, storeId);
            pst.executeUpdate();

            AlertUtil.showInformation("Store Updated", "Store status set to " + status + ".");
            loadStores(null);

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtil.showError("Database Error", "Could not update the store.");
        }
    }

    @FXML
    private void goBack(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/AdminDashboard.fxml");
    }
}
