
package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AdminDashboardController {

    @FXML private Label lblAdmin;
    @FXML private Label lblPendingStores;
    @FXML private Label lblReports;
    @FXML private Label lblUsers;
    @FXML private Label lblPets;

    @FXML
    public void initialize(){
        lblAdmin.setText("Welcome, Admin");
        lblPendingStores.setText("3");
        lblReports.setText("5");
        lblUsers.setText("26");
        lblPets.setText("41");
    }

    @FXML private void openDashboard(ActionEvent e){}
    @FXML private void openStores(ActionEvent e){}
    @FXML private void openPets(ActionEvent e){}
    @FXML private void openReports(ActionEvent e){}
    @FXML private void openUsers(ActionEvent e){}
    @FXML private void openStatistics(ActionEvent e){}

    @FXML
    private void logout(ActionEvent e) throws Exception{
        HelloApplication.changeScene("/fxml/Login.fxml");
    }
}
