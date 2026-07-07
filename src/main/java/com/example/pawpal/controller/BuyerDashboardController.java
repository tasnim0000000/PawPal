package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import com.example.pawpal.session.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class BuyerDashboardController {

    @FXML private Label lblWelcome;
    @FXML private Label lblAvailablePets;
    @FXML private Label lblOrders;
    @FXML private Label lblReviews;
    @FXML private FlowPane flowFeaturedPets;

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    @FXML
    public void initialize() {

        lblWelcome.setText("Welcome, " + Session.getCurrentUser().getFullName() + "!");

        try (Statement st = connection.createStatement()) {

            ResultSet rs = st.executeQuery(
                    "SELECT COUNT(*) c FROM pets p JOIN stores s ON p.store_id=s.store_id " +
                            "WHERE s.approval_status='APPROVED' AND p.availability_status='AVAILABLE'");
            if (rs.next()) lblAvailablePets.setText(rs.getString("c"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            PreparedStatement pst = connection.prepareStatement(
                    "SELECT COUNT(*) c FROM orders WHERE buyer_id=?");
            pst.setInt(1, Session.getCurrentUser().getUserId());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) lblOrders.setText(rs.getString("c"));

            PreparedStatement pst2 = connection.prepareStatement(
                    "SELECT COUNT(*) c FROM ratings WHERE buyer_id=?");
            pst2.setInt(1, Session.getCurrentUser().getUserId());
            ResultSet rs2 = pst2.executeQuery();
            if (rs2.next()) lblReviews.setText(rs2.getString("c"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        loadFeaturedPets();
    }

    private void loadFeaturedPets() {

        flowFeaturedPets.getChildren().clear();

        String sql = "SELECT p.pet_id, p.pet_name, p.pet_type, p.price FROM pets p JOIN stores s ON p.store_id=s.store_id " +
                "WHERE s.approval_status='APPROVED' AND p.availability_status='AVAILABLE' " +
                "ORDER BY p.is_featured DESC, p.created_at DESC LIMIT 4";

        try (Statement st = connection.createStatement()) {

            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                int petId = rs.getInt("pet_id");

                String icon = switch (rs.getString("pet_type") == null ? "" : rs.getString("pet_type").toUpperCase()) {
                    case "DOG" -> "\uD83D\uDC36";
                    case "CAT" -> "\uD83D\uDC31";
                    case "BIRD" -> "\uD83D\uDC26";
                    case "RABBIT" -> "\uD83D\uDC30";
                    case "FISH" -> "\uD83D\uDC20";
                    default -> "\uD83D\uDC3E";
                };

                VBox card = new VBox(8);
                card.setPrefWidth(180);
                card.setStyle("-fx-background-color:white;-fx-padding:15;-fx-alignment:center;");

                Label lblIcon = new Label(icon);
                lblIcon.setStyle("-fx-font-size:32;");

                Label lblName = new Label(rs.getString("pet_name"));
                lblName.setStyle("-fx-font-weight:bold;");

                Label lblPrice = new Label(String.format("%.0f BDT", rs.getDouble("price")));

                Button btnView = new Button("View Details");
                btnView.setOnAction(e -> openPetDetails(petId));

                card.getChildren().addAll(lblIcon, lblName, lblPrice, btnView);
                flowFeaturedPets.getChildren().add(card);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openPetDetails(int petId) {

        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/fxml/PetDetails.fxml"));
            Parent root = loader.load();

            PetDetailsController controller = loader.getController();
            controller.loadPet(petId);

            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void openDashboard(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/BuyerDashboard.fxml");
    }

    @FXML
    private void openBrowsePets(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/BrowsePets.fxml");
    }

    @FXML
    private void openOrders(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/MyOrders.fxml");
    }

    @FXML
    private void openReviews(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/MyReviews.fxml");
    }

    @FXML
    private void openProfile(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/Profile.fxml");
    }

    @FXML
    private void logout(ActionEvent e) throws Exception {
        Session.logout();
        HelloApplication.changeScene("/fxml/Login.fxml");
    }
}
