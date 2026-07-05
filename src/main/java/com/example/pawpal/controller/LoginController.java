package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import com.example.pawpal.model.User;
import com.example.pawpal.session.Session;
import com.example.pawpal.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnRegister;

    private final Connection connection =
            DatabaseConnection.getInstance().getConnection();

    @FXML
    private void login(ActionEvent event) {

        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {

            AlertUtil.showError(
                    "Missing Information",
                    "Please enter your email and password."
            );
            return;
        }

        String sql =
                "SELECT * FROM users WHERE email=? AND password=?";

        try {

            PreparedStatement pst = connection.prepareStatement(sql);

            pst.setString(1, email);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if (!rs.next()) {

                AlertUtil.showError(
                        "Login Failed",
                        "Invalid Email or Password."
                );
                return;
            }

            if (rs.getString("account_status").equalsIgnoreCase("BLOCKED")) {

                AlertUtil.showError(
                        "Account Blocked",
                        "Your account has been blocked by the administrator."
                );
                return;
            }

            User user = new User();

            user.setUserId(rs.getInt("user_id"));
            user.setFullName(rs.getString("full_name"));
            user.setEmail(rs.getString("email"));
            user.setPhone(rs.getString("phone"));
            user.setRole(rs.getString("role"));
            user.setAccountStatus(rs.getString("account_status"));

            Session.setCurrentUser(user);

            clearFields();

            switch (user.getRole()) {

                case "BUYER":

                    HelloApplication.changeScene("/fxml/BuyerDashboard.fxml");
                    break;

                case "STORE_OWNER":

                    HelloApplication.changeScene("/fxml/SellerDashboard.fxml");
                    break;

                case "ADMIN":

                    HelloApplication.changeScene("/fxml/AdminDashboard.fxml");
                    break;

                default:

                    AlertUtil.showError(
                            "Role Error",
                            "Unknown user role."
                    );

            }

        }

        catch (Exception e) {

            e.printStackTrace();

            AlertUtil.showError(
                    "Database Error",
                    "Something went wrong."
            );

        }

    }

    @FXML
    private void openRegister(ActionEvent event) throws Exception {

        HelloApplication.changeScene("/fxml/Register.fxml");

    }

    @FXML
    private void forgotPassword(ActionEvent event) throws Exception {

        HelloApplication.changeScene("/fxml/ForgotPassword.fxml");

    }

    private void clearFields() {

        txtEmail.clear();
        txtPassword.clear();

    }

}