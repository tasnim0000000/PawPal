package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import com.example.pawpal.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ForgotPasswordController {

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtNewPassword;

    @FXML
    private PasswordField txtConfirmPassword;

    private final Connection connection =
            DatabaseConnection.getInstance().getConnection();

    @FXML
    private void resetPassword(ActionEvent event) {

        String email = txtEmail.getText().trim();
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if(email.isEmpty() ||
                newPassword.isEmpty() ||
                confirmPassword.isEmpty()){

            AlertUtil.showError(
                    "Missing Information",
                    "Please fill in all the fields."
            );

            return;

        }

        if(!isValidEmail(email)){

            AlertUtil.showError(
                    "Invalid Email",
                    "Please enter a valid email address."
            );

            return;

        }

        if(!isValidPassword(newPassword)){

            AlertUtil.showError(
                    "Weak Password",
                    "Password must be at least 8 characters."
            );

            return;

        }

        if(!newPassword.equals(confirmPassword)){

            AlertUtil.showError(
                    "Password Mismatch",
                    "Passwords do not match."
            );

            return;

        }

        try {

            PreparedStatement check = connection.prepareStatement(
                    "SELECT user_id FROM users WHERE email=?");
            check.setString(1, email);
            ResultSet rs = check.executeQuery();

            if (!rs.next()) {

                AlertUtil.showError(
                        "Account Not Found",
                        "No account is registered with this email address."
                );
                return;
            }

            PreparedStatement update = connection.prepareStatement(
                    "UPDATE users SET password=? WHERE email=?");
            update.setString(1, newPassword);
            update.setString(2, email);
            update.executeUpdate();

            AlertUtil.showInformation(
                    "Password Reset",
                    "Your password has been reset successfully. You can now log in."
            );

            txtEmail.clear();
            txtNewPassword.clear();
            txtConfirmPassword.clear();

            HelloApplication.changeScene("/fxml/Login.fxml");

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.showError(
                    "Database Error",
                    "Something went wrong while resetting your password."
            );

        }

    }

    @FXML
    private void backToLogin(ActionEvent event) throws Exception{

        HelloApplication.changeScene("/fxml/Login.fxml");

    }

    private boolean isValidEmail(String email){

        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    }

    private boolean isValidPassword(String password){

        return password.length() >= 8;

    }

}
