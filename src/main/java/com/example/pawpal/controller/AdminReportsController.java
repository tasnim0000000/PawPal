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

public class AdminReportsController {

    @FXML private TableView<ObservableList<String>> tableReports;
    @FXML private TableColumn<ObservableList<String>, String> colId, colPet, colReporter, colReason,
            colStatus, colDate;
    @FXML private TextArea txtAdminNote;

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
        colPet.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colReporter.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        colReason.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(5)));

        loadReports(null);
    }

    @FXML
    public void loadReports(ActionEvent e) {

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        String sql = "SELECT r.report_id, p.pet_name, u.full_name AS reporter, r.reason, r.report_status, r.reported_at " +
                "FROM reports r JOIN pets p ON r.pet_id = p.pet_id JOIN users u ON r.user_id = u.user_id " +
                "ORDER BY (r.report_status = 'Pending') DESC, r.reported_at DESC";

        try {
            PreparedStatement pst = connection.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("report_id")));
                row.add(rs.getString("pet_name"));
                row.add(rs.getString("reporter"));
                row.add(rs.getString("reason"));
                row.add(rs.getString("report_status"));
                row.add(rs.getTimestamp("reported_at") != null
                        ? rs.getTimestamp("reported_at").toLocalDateTime().toLocalDate().toString() : "");
                data.add(row);
            }

            tableReports.setItems(data);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Integer getSelectedReportId() {

        ObservableList<String> selected = tableReports.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showError("No Selection", "Please select a report first.");
            return null;
        }

        return Integer.parseInt(selected.get(0));
    }

    @FXML
    private void markReviewed(ActionEvent e) {
        updateReportStatus("Reviewed");
    }

    @FXML
    private void resolveReport(ActionEvent e) {
        updateReportStatus("Resolved");
    }

    private void updateReportStatus(String status) {

        Integer reportId = getSelectedReportId();
        if (reportId == null) return;

        String note = txtAdminNote.getText() == null ? "" : txtAdminNote.getText().trim();

        try {
            PreparedStatement pst = connection.prepareStatement(
                    "UPDATE reports SET report_status=?, admin_note=? WHERE report_id=?");
            pst.setString(1, status);
            pst.setString(2, note);
            pst.setInt(3, reportId);
            pst.executeUpdate();

            AlertUtil.showInformation("Report Updated", "Report marked as " + status + ".");
            txtAdminNote.clear();
            loadReports(null);

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtil.showError("Database Error", "Could not update the report.");
        }
    }

    @FXML
    private void deleteReportedPet(ActionEvent e) {

        Integer reportId = getSelectedReportId();
        if (reportId == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Pet");
        confirm.setHeaderText(null);
        confirm.setContentText("This removes the reported pet listing entirely (comments, reviews, reports, orders included). Continue?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            PreparedStatement findPet = connection.prepareStatement(
                    "SELECT pet_id FROM reports WHERE report_id=?");
            findPet.setInt(1, reportId);
            ResultSet rs = findPet.executeQuery();

            if (!rs.next()) return;

            int petId = rs.getInt("pet_id");

            connection.setAutoCommit(false);

            exec("DELETE FROM comments WHERE pet_id=?", petId);
            exec("DELETE FROM ratings WHERE pet_id=?", petId);
            exec("DELETE FROM orders WHERE pet_id=?", petId);
            exec("DELETE FROM reports WHERE pet_id=?", petId);
            exec("DELETE FROM pets WHERE pet_id=?", petId);

            connection.commit();
            connection.setAutoCommit(true);

            AlertUtil.showInformation("Pet Removed", "The reported pet listing has been removed.");
            loadReports(null);

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (Exception ignored) {
            }
            AlertUtil.showError("Database Error", "Could not remove the pet.");
        }
    }

    @FXML
    private void blockReportedStoreOwner(ActionEvent e) {

        Integer reportId = getSelectedReportId();
        if (reportId == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Block Store Owner");
        confirm.setHeaderText(null);
        confirm.setContentText("This will block the account of the store owner who listed the reported pet. Continue?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        String sql = "SELECT s.owner_id FROM reports r " +
                "JOIN pets p ON r.pet_id = p.pet_id " +
                "JOIN stores s ON p.store_id = s.store_id " +
                "WHERE r.report_id = ?";

        try {
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, reportId);
            ResultSet rs = pst.executeQuery();

            if (!rs.next()) return;

            int ownerId = rs.getInt("owner_id");

            PreparedStatement block = connection.prepareStatement(
                    "UPDATE users SET account_status='BLOCKED' WHERE user_id=?");
            block.setInt(1, ownerId);
            block.executeUpdate();

            PreparedStatement resolve = connection.prepareStatement(
                    "UPDATE reports SET report_status='Resolved', admin_note='Store owner blocked.' WHERE report_id=?");
            resolve.setInt(1, reportId);
            resolve.executeUpdate();

            AlertUtil.showInformation("Owner Blocked", "The store owner's account has been blocked.");
            loadReports(null);

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtil.showError("Database Error", "Could not block the store owner.");
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
