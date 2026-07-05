
package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class BuyerDashboardController {

    @FXML private Label lblWelcome;
    @FXML private Label lblAvailablePets;
    @FXML private Label lblOrders;
    @FXML private Label lblReviews;

    @FXML
    public void initialize(){
        lblWelcome.setText("Welcome to PawPal!");
        lblAvailablePets.setText("12");
        lblOrders.setText("2");
        lblReviews.setText("5");
    }

    @FXML private void openDashboard(ActionEvent e){}
    @FXML private void openBrowsePets(ActionEvent e){}
    @FXML private void openOrders(ActionEvent e){}
    @FXML private void openReviews(ActionEvent e){}
    @FXML private void openProfile(ActionEvent e){}

    @FXML
    private void logout(ActionEvent e) throws Exception{
        HelloApplication.changeScene("/fxml/Login.fxml");
    }
}
