package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import com.example.pawpal.session.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class SalesController {

    @FXML private Label lblTotalSales;
    @FXML private Label lblTotalRevenue;
    @FXML private TableView<ObservableList<String>> tableSales;
    @FXML private TableColumn<ObservableList<String>, String> colOrderId, colPet, colBuyer, colPrice, colCompletedAt;

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    @FXML
    public void initialize() {

        colOrderId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
        colPet.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colBuyer.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        colPrice.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        colCompletedAt.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));

        loadSales();
    }

    private void loadSales() {

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        String sql = "SELECT o.order_id, p.pet_name, u.full_name AS buyer_name, p.price, o.completed_at " +
                "FROM orders o JOIN pets p ON o.pet_id = p.pet_id JOIN users u ON o.buyer_id = u.user_id " +
                "WHERE p.store_id = (SELECT store_id FROM stores WHERE owner_id = ?) AND o.order_status = 'Completed' " +
                "ORDER BY o.completed_at DESC";

        double total = 0;
        int count = 0;

        try {
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, Session.getCurrentUser().getUserId());
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("order_id")));
                row.add(rs.getString("pet_name"));
                row.add(rs.getString("buyer_name"));

                double price = rs.getDouble("price");
                row.add(String.valueOf(price));
                total += price;
                count++;

                Timestamp ts = rs.getTimestamp("completed_at");
                row.add(ts != null ? ts.toLocalDateTime().toLocalDate().toString() : "");

                data.add(row);
            }

            tableSales.setItems(data);
            lblTotalSales.setText(String.valueOf(count));
            lblTotalRevenue.setText(String.format("%.2f BDT", total));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void goBack(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/SellerDashboard.fxml");
    }
}
