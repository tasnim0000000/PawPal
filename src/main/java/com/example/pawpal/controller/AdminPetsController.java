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

public class AdminPetsController {

    @FXML private TableView<ObservableList<String>> tablePets;
    @FXML private TableColumn<ObservableList<String>, String> colId, colName, colType, colStore,
            colPrice, colStatus;

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        colStore.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        colPrice.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(5)));

        loadPets(null);
    }

    @FXML
    public void loadPets(ActionEvent e) {

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        String sql = "SELECT p.pet_id, p.pet_name, p.pet_type, s.store_name, p.price, p.availability_status " +
                "FROM pets p JOIN stores s ON p.store_id = s.store_id ORDER BY p.created_at DESC";

        try {
            PreparedStatement pst = connection.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("pet_id")));
                row.add(rs.getString("pet_name"));
                row.add(rs.getString("pet_type"));
                row.add(rs.getString("store_name"));
                row.add(String.valueOf(rs.getDouble("price")));
                row.add(rs.getString("availability_status"));
                data.add(row);
            }

            tablePets.setItems(data);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void deletePet(ActionEvent e) {

        ObservableList<String> selected = tablePets.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showError("No Selection", "Please select a pet first.");
            return;
        }

        int petId = Integer.parseInt(selected.get(0));

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Pet");
        confirm.setHeaderText(null);
        confirm.setContentText("This will permanently remove the pet listing along with its comments, " +
                "reviews, reports, and order history. Continue?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            connection.setAutoCommit(false);

            exec("DELETE FROM comments WHERE pet_id=?", petId);
            exec("DELETE FROM ratings WHERE pet_id=?", petId);
            exec("DELETE FROM reports WHERE pet_id=?", petId);
            exec("DELETE FROM orders WHERE pet_id=?", petId);
            exec("DELETE FROM pets WHERE pet_id=?", petId);

            connection.commit();
            connection.setAutoCommit(true);

            AlertUtil.showInformation("Pet Deleted", "The pet listing has been removed.");
            loadPets(null);

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (Exception ignored) {
            }
            AlertUtil.showError("Database Error", "Could not delete the pet.");
        }
    }

    private void exec(String sql, int petId) throws Exception {
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setInt(1, petId);
        pst.executeUpdate();
    }

    @FXML
    private void goBack(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/AdminDashboard.fxml");
    }
}
