package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import com.example.pawpal.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BrowsePetsController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbTypeFilter;
    @FXML private TableView<ObservableList<String>> tablePets;
    @FXML private TableColumn<ObservableList<String>, String> colId, colName, colType, colBreed,
            colAge, colGender, colPrice, colStore, colRating;

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    @FXML
    public void initialize() {

        cmbTypeFilter.getItems().addAll("All", "Dog", "Cat", "Bird", "Rabbit", "Fish", "Other");
        cmbTypeFilter.setValue("All");

        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        colBreed.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        colAge.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
        colGender.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(5)));
        colPrice.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(6)));
        colStore.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(7)));
        colRating.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(8)));

        loadPets(null);
    }

    @FXML
    public void loadPets(ActionEvent e) {

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        String search = txtSearch.getText() == null ? "" : txtSearch.getText().trim();
        String type = cmbTypeFilter.getValue();

        StringBuilder sql = new StringBuilder(
                "SELECT p.pet_id, p.pet_name, p.pet_type, p.breed, p.age, p.gender, p.price, " +
                        "s.store_name, s.average_rating " +
                        "FROM pets p JOIN stores s ON p.store_id = s.store_id " +
                        "WHERE s.approval_status = 'APPROVED' AND p.availability_status = 'AVAILABLE' ");

        if (!search.isEmpty()) {
            sql.append("AND p.pet_name LIKE ? ");
        }
        if (type != null && !type.equals("All")) {
            sql.append("AND p.pet_type = ? ");
        }
        sql.append("ORDER BY p.created_at DESC");

        try {
            PreparedStatement pst = connection.prepareStatement(sql.toString());
            int idx = 1;
            if (!search.isEmpty()) {
                pst.setString(idx++, "%" + search + "%");
            }
            if (type != null && !type.equals("All")) {
                pst.setString(idx, type.toUpperCase());
            }

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("pet_id")));
                row.add(rs.getString("pet_name"));
                row.add(rs.getString("pet_type"));
                row.add(rs.getString("breed"));
                row.add(String.valueOf(rs.getInt("age")));
                row.add(rs.getString("gender"));
                row.add(String.valueOf(rs.getDouble("price")));
                row.add(rs.getString("store_name"));
                row.add(String.format("%.1f \u2605", rs.getDouble("average_rating")));
                data.add(row);
            }

            tablePets.setItems(data);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void viewDetails(ActionEvent e) {

        ObservableList<String> selected = tablePets.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showError("No Selection", "Please select a pet first.");
            return;
        }

        int petId = Integer.parseInt(selected.get(0));

        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/fxml/PetDetails.fxml"));
            Parent root = loader.load();

            PetDetailsController controller = loader.getController();
            controller.loadPet(petId);

            Stage stage = (Stage) tablePets.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void goBack(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/BuyerDashboard.fxml");
    }
}
