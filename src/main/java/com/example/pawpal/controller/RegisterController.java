package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import com.example.pawpal.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterController {

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtPhone;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtConfirmPassword;

    @FXML
    private ComboBox<String> cmbRole;

    @FXML
    private Button btnRegister;

    private final Connection connection =
            DatabaseConnection
                    .getInstance()
                    .getConnection();

    @FXML
    public void initialize() {

        cmbRole.getItems().addAll(
                "Buyer",
                "Store Owner"
        );

    }

    @FXML
    private void register(ActionEvent event) {

        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();
        String role = cmbRole.getValue();

        if (name.isEmpty() ||
                phone.isEmpty() ||
                email.isEmpty() ||
                password.isEmpty() ||
                confirmPassword.isEmpty() ||
                role == null) {

            AlertUtil.showError(
                    "Missing Information",
                    "Please fill in all the fields."
            );
            return;
        }


        if (!isValidName(name)) {

            AlertUtil.showError(
                    "Invalid Name",
                    "Name must start with a capital letter."
            );
            return;
        }
        if (!isValidPhone(phone)) {

            AlertUtil.showError(
                    "Invalid Phone Number",
                    "Phone number must be in +8801XXXXXXXXX format."
            );
            return;
        }
        if (!isValidEmail(email)) {

            AlertUtil.showError(
                    "Invalid Email",
                    "Please enter a valid email address."
            );
            return;
        }

        if (!isValidPassword(password)) {

            AlertUtil.showError(
                    "Weak Password",
                    "Password must be at least 8 characters long."
            );
            return;
        }

        // Confirm Password

        if (!password.equals(confirmPassword)) {

            AlertUtil.showError(
                    "Password Mismatch",
                    "Passwords do not match."
            );
            return;
        }
        if (emailExists(email)) {

            AlertUtil.showError(
                    "Duplicate Email",
                    "This email is already registered."
            );
            return;
        }

        // Insert User

        if (insertUser(name, email, password, phone, role)) {

            AlertUtil.showInformation(
                    "Registration Successful",
                    "Account Created Successfully!"
            );

            clearFields();

            try {

                HelloApplication.changeScene("/fxml/Login.fxml");

            }

            catch (Exception e) {

                e.printStackTrace();

            }

        }

        else {

            AlertUtil.showError(
                    "Database Error",
                    "Unable to create account."
            );

        }

    }

    @FXML
    private void backToLogin(ActionEvent event) throws Exception {

        HelloApplication.changeScene("/fxml/Login.fxml");

    }

    private boolean emailExists(String email) {

        String sql =
                "SELECT email FROM users WHERE email=?";

        try {

            PreparedStatement pst =
                    connection.prepareStatement(sql);

            pst.setString(1, email);

            ResultSet rs = pst.executeQuery();

            return rs.next();

        }

        catch (SQLException e) {

            e.printStackTrace();

        }

        return false;

    }

    private boolean insertUser(String name,
                               String email,
                               String password,
                               String phone,
                               String role) {

        String sql =
                "INSERT INTO users(full_name,email,password,phone,role) VALUES(?,?,?,?,?)";

        try {

            PreparedStatement pst =
                    connection.prepareStatement(sql);

            pst.setString(1, name);

            pst.setString(2, email);

            pst.setString(3, password);

            pst.setString(4, phone);

            pst.setString(
                    5,
                    role.toUpperCase().replace(" ", "_")
            );

            int row = pst.executeUpdate();

            return row > 0;

        }

        catch (SQLException e) {

            e.printStackTrace();

        }

        return false;

    }

    private boolean isValidName(String name) {

        return name.matches("^[A-Z][a-zA-Z ]*$");

    }

    private boolean isValidPhone(String phone) {

        return phone.matches("^\\+8801[3-9]\\d{8}$");

    }

    private boolean isValidEmail(String email) {

        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    }

    private boolean isValidPassword(String password) {

        return password.length() >= 8;

    }


    private void clearFields() {

        txtName.clear();
        txtPhone.clear();
        txtEmail.clear();
        txtPassword.clear();
        txtConfirmPassword.clear();

        cmbRole.getSelectionModel().clearSelection();

    }

}