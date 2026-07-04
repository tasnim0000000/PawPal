package com.example.pawpal.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    public void login(ActionEvent event){

        System.out.println("Login Button Clicked");

        System.out.println("Email : " + txtEmail.getText());

        System.out.println("Password : " + txtPassword.getText());

    }

    @FXML
    public void register(ActionEvent event){

        System.out.println("Register Button Clicked");

    }

}