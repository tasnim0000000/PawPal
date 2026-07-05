
package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class SellerDashboardController {

    @FXML private Label lblSeller;
    @FXML private Label lblPets;
    @FXML private Label lblPendingOrders;
    @FXML private Label lblSales;
    @FXML private Label lblRating;
    @FXML private ListView<String> listRecentPets;

    @FXML
    public void initialize(){

        lblSeller.setText("Welcome, Store Owner");
        lblPets.setText("8");
        lblPendingOrders.setText("3");
        lblSales.setText("15");
        lblRating.setText("4.8 ★");

        listRecentPets.getItems().addAll(
                "Golden Retriever",
                "Persian Cat",
                "Macaw Parrot",
                "Dutch Rabbit"
        );
    }

    @FXML private void openDashboard(ActionEvent e){}
    @FXML private void openAddPet(ActionEvent e){}
    @FXML private void openMyPets(ActionEvent e){}
    @FXML private void openOrders(ActionEvent e){}
    @FXML private void openStoreProfile(ActionEvent e){}
    @FXML private void openSales(ActionEvent e){}

    @FXML
    private void logout(ActionEvent e) throws Exception{
        HelloApplication.changeScene("/fxml/Login.fxml");
    }
}
