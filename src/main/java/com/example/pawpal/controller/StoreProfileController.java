package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import com.example.pawpal.session.Session;
import com.example.pawpal.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StoreProfileController {

    @FXML private Label lblStatusBanner;
    @FXML private Label lblApprovalStatus;
    @FXML private Label lblAverageRating;
    @FXML private TextField txtStoreName;
    @FXML private TextField txtAddress;
    @FXML private TextField txtPhone;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtLogoPath;
    @FXML private Button btnSave;

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    private boolean storeExists = false;

    @FXML
    public void initialize() {
        loadStore();
    }

    private void loadStore() {

        String sql = "SELECT * FROM stores WHERE owner_id = ?";

        try {
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, Session.getCurrentUser().getUserId());
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                storeExists = true;

                txtStoreName.setText(rs.getString("store_name"));
                txtAddress.setText(rs.getString("address"));
                txtPhone.setText(rs.getString("phone"));
                txtDescription.setText(rs.getString("store_description"));
                txtLogoPath.setText(rs.getString("logo_path"));

                String status = rs.getString("approval_status");
                lblApprovalStatus.setText(status);
                lblAverageRating.setText(String.format("%.1f \u2605", rs.getDouble("average_rating")));

                if ("APPROVED".equalsIgnoreCase(status)) {
                    lblStatusBanner.setText("Your store is approved and visible to buyers.");
                } else if ("PENDING".equalsIgnoreCase(status)) {
                    lblStatusBanner.setText("Your store is awaiting admin approval. You can't add pets until it's approved.");
                } else {
                    lblStatusBanner.setText("Your store application was rejected. Update your details and re-apply.");
                }

                btnSave.setText("Update Store");

            } else {
                storeExists = false;
                lblStatusBanner.setText("You don't have a store yet. Fill in the form below to apply.");
                lblApprovalStatus.setText("NOT CREATED");
                lblAverageRating.setText("N/A");
                btnSave.setText("Create Store");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void saveStore(ActionEvent e) {

        String name = txtStoreName.getText() == null ? "" : txtStoreName.getText().trim();
        String address = txtAddress.getText() == null ? "" : txtAddress.getText().trim();
        String phone = txtPhone.getText() == null ? "" : txtPhone.getText().trim();
        String description = txtDescription.getText() == null ? "" : txtDescription.getText().trim();
        String logoPath = txtLogoPath.getText() == null ? "" : txtLogoPath.getText().trim();

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            AlertUtil.showError("Missing Information", "Please fill in store name, address, and phone.");
            return;
        }

        try {
            if (storeExists) {

                PreparedStatement pst = connection.prepareStatement(
                        "UPDATE stores SET store_name=?, address=?, phone=?, store_description=?, logo_path=? WHERE owner_id=?");
                pst.setString(1, name);
                pst.setString(2, address);
                pst.setString(3, phone);
                pst.setString(4, description);
                pst.setString(5, logoPath);
                pst.setInt(6, Session.getCurrentUser().getUserId());
                pst.executeUpdate();

                AlertUtil.showInformation("Store Updated", "Your store information has been updated.");

            } else {

                PreparedStatement pst = connection.prepareStatement(
                        "INSERT INTO stores(owner_id, store_name, store_description, address, phone, logo_path) VALUES(?,?,?,?,?,?)");
                pst.setInt(1, Session.getCurrentUser().getUserId());
                pst.setString(2, name);
                pst.setString(3, description);
                pst.setString(4, address);
                pst.setString(5, phone);
                pst.setString(6, logoPath);
                pst.executeUpdate();

                AlertUtil.showInformation("Application Submitted", "Your store has been submitted for admin approval.");
            }

            loadStore();

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtil.showError("Database Error", "Could not save your store information.");
        }
    }

    @FXML
    private void goBack(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/SellerDashboard.fxml");
    }
}
