package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import com.example.pawpal.session.Session;
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

public class AdminUsersController {

    @FXML private ComboBox<String> cmbRoleFilter;
    @FXML private TableView<ObservableList<String>> tableUsers;
    @FXML private TableColumn<ObservableList<String>, String> colId, colName, colEmail, colPhone,
            colRole, colStatus;

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    @FXML
    public void initialize() {

        cmbRoleFilter.getItems().addAll("All", "BUYER", "STORE_OWNER", "ADMIN");
        cmbRoleFilter.setValue("All");

        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        colRole.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(5)));

        loadUsers(null);
    }

    @FXML
    public void loadUsers(ActionEvent e) {

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        String role = cmbRoleFilter.getValue();

        StringBuilder sql = new StringBuilder(
                "SELECT user_id, full_name, email, phone, role, account_status FROM users ");

        if (role != null && !role.equals("All")) {
            sql.append("WHERE role = ? ");
        }
        sql.append("ORDER BY created_at DESC");

        try {
            PreparedStatement pst = connection.prepareStatement(sql.toString());
            if (role != null && !role.equals("All")) {
                pst.setString(1, role);
            }

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("user_id")));
                row.add(rs.getString("full_name"));
                row.add(rs.getString("email"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("role"));
                row.add(rs.getString("account_status"));
                data.add(row);
            }

            tableUsers.setItems(data);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Integer getSelectedUserId() {

        ObservableList<String> selected = tableUsers.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showError("No Selection", "Please select a user first.");
            return null;
        }

        return Integer.parseInt(selected.get(0));
    }

    @FXML
    private void blockUser(ActionEvent e) {
        updateStatus("BLOCKED");
    }

    @FXML
    private void unblockUser(ActionEvent e) {
        updateStatus("ACTIVE");
    }

    private void updateStatus(String status) {

        Integer userId = getSelectedUserId();
        if (userId == null) return;

        if (userId == Session.getCurrentUser().getUserId()) {
            AlertUtil.showError("Not Allowed", "You cannot change the status of your own account.");
            return;
        }

        try {
            PreparedStatement pst = connection.prepareStatement(
                    "UPDATE users SET account_status=? WHERE user_id=?");
            pst.setString(1, status);
            pst.setInt(2, userId);
            pst.executeUpdate();

            AlertUtil.showInformation("User Updated", "Account status set to " + status + ".");
            loadUsers(null);

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtil.showError("Database Error", "Could not update the user.");
        }
    }

    @FXML
    private void deleteUser(ActionEvent e) {

        Integer userId = getSelectedUserId();
        if (userId == null) return;

        if (userId == Session.getCurrentUser().getUserId()) {
            AlertUtil.showError("Not Allowed", "You cannot delete your own account.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete User");
        confirm.setHeaderText(null);
        confirm.setContentText("This permanently deletes the user along with their store (if any), pets, " +
                "orders, comments, ratings, and reports. Continue?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            connection.setAutoCommit(false);

            // Comments and reports made BY this user
            exec("DELETE FROM comments WHERE user_id=?", userId);
            exec("DELETE FROM reports WHERE user_id=?", userId);

            // If this user owns a store, remove everything tied to that store's pets
            PreparedStatement findStore = connection.prepareStatement("SELECT store_id FROM stores WHERE owner_id=?");
            findStore.setInt(1, userId);
            ResultSet storeRs = findStore.executeQuery();

            if (storeRs.next()) {
                int storeId = storeRs.getInt("store_id");

                PreparedStatement findPets = connection.prepareStatement("SELECT pet_id FROM pets WHERE store_id=?");
                findPets.setInt(1, storeId);
                ResultSet petsRs = findPets.executeQuery();

                while (petsRs.next()) {
                    int petId = petsRs.getInt("pet_id");
                    exec("DELETE FROM comments WHERE pet_id=?", petId);
                    exec("DELETE FROM ratings WHERE pet_id=?", petId);
                    exec("DELETE FROM reports WHERE pet_id=?", petId);
                    exec("DELETE FROM orders WHERE pet_id=?", petId);
                }

                exec("DELETE FROM pets WHERE store_id=?", storeId);
                exec("DELETE FROM stores WHERE store_id=?", storeId);
            }

            // Orders and ratings made BY this user as a buyer
            exec("DELETE FROM ratings WHERE buyer_id=?", userId);
            exec("DELETE FROM orders WHERE buyer_id=?", userId);

            exec("DELETE FROM users WHERE user_id=?", userId);

            connection.commit();
            connection.setAutoCommit(true);

            AlertUtil.showInformation("User Deleted", "The user account has been removed.");
            loadUsers(null);

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (Exception ignored) {
            }
            AlertUtil.showError("Database Error", "Could not delete the user.");
        }
    }

    private void exec(String sql, int id) throws Exception {
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setInt(1, id);
        pst.executeUpdate();
    }

    @FXML
    private void goBack(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/AdminDashboard.fxml");
    }
}
