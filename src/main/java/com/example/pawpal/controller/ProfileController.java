package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import com.example.pawpal.session.Session;
import com.example.pawpal.util.AlertUtil;
import com.example.pawpal.util.ValidationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ProfileController {

    @FXML private Label lblEmail;
    @FXML private Label lblRole;
    @FXML private TextField txtFullName;
    @FXML private TextField txtPhone;
    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    @FXML
    public void initialize() {
        lblEmail.setText(Session.getCurrentUser().getEmail());
        lblRole.setText(Session.getCurrentUser().getRole());
        txtFullName.setText(Session.getCurrentUser().getFullName());
        txtPhone.setText(Session.getCurrentUser().getPhone());
    }

    @FXML
    private void saveProfile(ActionEvent e) {

        String name = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();

        if (!ValidationUtil.isValidName(name)) {
            AlertUtil.showError("Invalid Name", "Name must start with a capital letter.");
            return;
        }

        if (!ValidationUtil.isValidPhone(phone)) {
            AlertUtil.showError("Invalid Phone", "Phone number must be in +8801XXXXXXXXX format.");
            return;
        }

        try {
            PreparedStatement pst = connection.prepareStatement(
                    "UPDATE users SET full_name=?, phone=? WHERE user_id=?");
            pst.setString(1, name);
            pst.setString(2, phone);
            pst.setInt(3, Session.getCurrentUser().getUserId());
            pst.executeUpdate();

            Session.getCurrentUser().setFullName(name);
            Session.getCurrentUser().setPhone(phone);

            AlertUtil.showInformation("Profile Updated", "Your profile has been updated.");

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtil.showError("Database Error", "Could not update your profile. The phone number may already be in use.");
        }
    }

    @FXML
    private void changePassword(ActionEvent e) {

        String current = txtCurrentPassword.getText();
        String newPass = txtNewPassword.getText();
        String confirm = txtConfirmPassword.getText();

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            AlertUtil.showError("Missing Information", "Please fill in all password fields.");
            return;
        }

        if (!ValidationUtil.isValidPassword(newPass)) {
            AlertUtil.showError("Weak Password", "New password must be at least 8 characters.");
            return;
        }

        if (!newPass.equals(confirm)) {
            AlertUtil.showError("Password Mismatch", "Passwords do not match.");
            return;
        }

        try {
            PreparedStatement check = connection.prepareStatement(
                    "SELECT user_id FROM users WHERE user_id=? AND password=?");
            check.setInt(1, Session.getCurrentUser().getUserId());
            check.setString(2, current);

            if (!check.executeQuery().next()) {
                AlertUtil.showError("Incorrect Password", "Your current password is incorrect.");
                return;
            }

            PreparedStatement update = connection.prepareStatement(
                    "UPDATE users SET password=? WHERE user_id=?");
            update.setString(1, newPass);
            update.setInt(2, Session.getCurrentUser().getUserId());
            update.executeUpdate();

            txtCurrentPassword.clear();
            txtNewPassword.clear();
            txtConfirmPassword.clear();

            AlertUtil.showInformation("Password Changed", "Your password has been updated.");

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtil.showError("Database Error", "Could not change your password.");
        }
    }

    @FXML
    private void goBack(ActionEvent e) throws Exception {

        switch (Session.getCurrentUser().getRole()) {
            case "STORE_OWNER":
                HelloApplication.changeScene("/fxml/SellerDashboard.fxml");
                break;
            case "ADMIN":
                HelloApplication.changeScene("/fxml/AdminDashboard.fxml");
                break;
            default:
                HelloApplication.changeScene("/fxml/BuyerDashboard.fxml");
        }
    }
}
