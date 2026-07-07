package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import com.example.pawpal.session.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class MyReviewsController {

    @FXML private TableView<ObservableList<String>> tableReviews;
    @FXML private TableColumn<ObservableList<String>, String> colPet, colStore, colRating, colReview, colDate;

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    @FXML
    public void initialize() {

        colPet.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
        colStore.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colRating.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        colReview.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));

        loadReviews(null);
    }

    @FXML
    public void loadReviews(ActionEvent e) {

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        String sql = "SELECT p.pet_name, s.store_name, r.rating, r.review, r.rated_at " +
                "FROM ratings r JOIN pets p ON r.pet_id = p.pet_id JOIN stores s ON r.store_id = s.store_id " +
                "WHERE r.buyer_id = ? ORDER BY r.rated_at DESC";

        try {
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, Session.getCurrentUser().getUserId());
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("pet_name"));
                row.add(rs.getString("store_name"));
                row.add("\u2605".repeat(rs.getInt("rating")));
                row.add(rs.getString("review"));
                Timestamp ts = rs.getTimestamp("rated_at");
                row.add(ts != null ? ts.toLocalDateTime().toLocalDate().toString() : "");
                data.add(row);
            }

            tableReviews.setItems(data);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void goBack(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/BuyerDashboard.fxml");
    }
}
