package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ForgotPasswordController {

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtNewPassword;

    @FXML
    private PasswordField txtConfirmPassword;

    @FXML
    private void resetPassword(ActionEvent event){

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

        System.out.println("===== RESET PASSWORD =====");
        System.out.println(email);
        System.out.println(newPassword);

        AlertUtil.showInformation(
                "Reset Password",
                "Database logic will be added next."
        );

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