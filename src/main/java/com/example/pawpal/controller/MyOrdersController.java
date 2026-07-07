package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import com.example.pawpal.session.Session;
import com.example.pawpal.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class MyOrdersController {

    @FXML private TableView<ObservableList<String>> tableOrders;
    @FXML private TableColumn<ObservableList<String>, String> colOrderId, colPet, colStore, colPrice,
            colPayment, colStatus, colDate;

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    @FXML
    public void initialize() {

        colOrderId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
        colPet.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colStore.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        colPrice.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        colPayment.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(5)));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(6)));

        loadOrders(null);
    }

    @FXML
    public void loadOrders(ActionEvent e) {

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        String sql = "SELECT o.order_id, p.pet_name, s.store_name, p.price, o.payment_method, " +
                "o.order_status, o.order_date, o.buyer_confirmed " +
                "FROM orders o JOIN pets p ON o.pet_id = p.pet_id JOIN stores s ON p.store_id = s.store_id " +
                "WHERE o.buyer_id = ? ORDER BY o.order_date DESC";

        try {
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, Session.getCurrentUser().getUserId());
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("order_id")));
                row.add(rs.getString("pet_name"));
                row.add(rs.getString("store_name"));
                row.add(String.valueOf(rs.getDouble("price")));
                row.add(rs.getString("payment_method"));

                String status = rs.getString("order_status");
                if ("Approved".equalsIgnoreCase(status) && rs.getBoolean("buyer_confirmed")) {
                    status = "Approved (waiting for store)";
                }
                row.add(status);

                Timestamp ts = rs.getTimestamp("order_date");
                row.add(ts != null ? ts.toLocalDateTime().toLocalDate().toString() : "");

                data.add(row);
            }

            tableOrders.setItems(data);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void confirmReceipt(ActionEvent e) {

        ObservableList<String> selected = tableOrders.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showError("No Selection", "Please select an order first.");
            return;
        }

        int orderId = Integer.parseInt(selected.get(0));

        try {
            PreparedStatement check = connection.prepareStatement(
                    "SELECT order_status, seller_confirmed, pet_id FROM orders WHERE order_id=?");
            check.setInt(1, orderId);
            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                return;
            }

            String status = rs.getString("order_status");
            boolean sellerConfirmed = rs.getBoolean("seller_confirmed");
            int petId = rs.getInt("pet_id");

            if (!"Approved".equalsIgnoreCase(status)) {
                AlertUtil.showError("Not Approved Yet", "The store owner needs to approve this order first.");
                return;
            }

            PreparedStatement update = connection.prepareStatement(
                    "UPDATE orders SET buyer_confirmed=1 WHERE order_id=?");
            update.setInt(1, orderId);
            update.executeUpdate();

            if (sellerConfirmed) {
                PreparedStatement complete = connection.prepareStatement(
                        "UPDATE orders SET order_status='Completed', completed_at=NOW() WHERE order_id=?");
                complete.setInt(1, orderId);
                complete.executeUpdate();

                PreparedStatement sold = connection.prepareStatement(
                        "UPDATE pets SET availability_status='SOLD' WHERE pet_id=?");
                sold.setInt(1, petId);
                sold.executeUpdate();

                AlertUtil.showInformation("Order Completed", "Transaction completed! Enjoy your new pet.");
            } else {
                AlertUtil.showInformation("Receipt Confirmed", "Waiting for the store owner to finalize the order.");
            }

            loadOrders(null);

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtil.showError("Database Error", "Could not confirm the order.");
        }
    }

    @FXML
    private void cancelOrder(ActionEvent e) {

        ObservableList<String> selected = tableOrders.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showError("No Selection", "Please select an order first.");
            return;
        }

        int orderId = Integer.parseInt(selected.get(0));

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Order");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to cancel this order?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            PreparedStatement check = connection.prepareStatement(
                    "SELECT order_status, pet_id FROM orders WHERE order_id=?");
            check.setInt(1, orderId);
            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                return;
            }

            if (!"Pending".equalsIgnoreCase(rs.getString("order_status"))) {
                AlertUtil.showError("Cannot Cancel", "Only pending orders can be cancelled.");
                return;
            }

            int petId = rs.getInt("pet_id");

            PreparedStatement update = connection.prepareStatement(
                    "UPDATE orders SET order_status='Cancelled' WHERE order_id=?");
            update.setInt(1, orderId);
            update.executeUpdate();

            PreparedStatement restore = connection.prepareStatement(
                    "UPDATE pets SET availability_status='AVAILABLE' WHERE pet_id=?");
            restore.setInt(1, petId);
            restore.executeUpdate();

            AlertUtil.showInformation("Order Cancelled", "Your order has been cancelled.");
            loadOrders(null);

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtil.showError("Database Error", "Could not cancel the order.");
        }
    }

    @FXML
    private void goBack(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/BuyerDashboard.fxml");
    }
}
