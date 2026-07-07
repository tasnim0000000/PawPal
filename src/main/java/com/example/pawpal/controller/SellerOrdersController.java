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

public class SellerOrdersController {

    @FXML private TableView<ObservableList<String>> tableOrders;
    @FXML private TableColumn<ObservableList<String>, String> colOrderId, colPet, colBuyer, colAddress,
            colPhone, colPayment, colStatus, colDate;

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    @FXML
    public void initialize() {

        colOrderId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
        colPet.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colBuyer.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        colAddress.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
        colPayment.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(5)));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(6)));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(7)));

        loadOrders(null);
    }

    private int getStoreId() throws Exception {
        PreparedStatement pst = connection.prepareStatement("SELECT store_id FROM stores WHERE owner_id=?");
        pst.setInt(1, Session.getCurrentUser().getUserId());
        ResultSet rs = pst.executeQuery();
        return rs.next() ? rs.getInt("store_id") : -1;
    }

    @FXML
    public void loadOrders(ActionEvent e) {

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        String sql = "SELECT o.order_id, p.pet_name, u.full_name AS buyer_name, o.delivery_address, " +
                "o.contact_phone, o.payment_method, o.order_status, o.buyer_confirmed, o.seller_confirmed, o.order_date " +
                "FROM orders o JOIN pets p ON o.pet_id = p.pet_id JOIN users u ON o.buyer_id = u.user_id " +
                "WHERE p.store_id = ? ORDER BY o.order_date DESC";

        try {
            int storeId = getStoreId();

            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, storeId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("order_id")));
                row.add(rs.getString("pet_name"));
                row.add(rs.getString("buyer_name"));
                row.add(rs.getString("delivery_address"));
                row.add(rs.getString("contact_phone"));
                row.add(rs.getString("payment_method"));

                String status = rs.getString("order_status");
                if ("Approved".equalsIgnoreCase(status) && rs.getBoolean("seller_confirmed")
                        && !rs.getBoolean("buyer_confirmed")) {
                    status = "Approved (waiting for buyer)";
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
    private void approveOrder(ActionEvent e) {

        ObservableList<String> selected = tableOrders.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showError("No Selection", "Please select an order first.");
            return;
        }

        int orderId = Integer.parseInt(selected.get(0));

        try {
            PreparedStatement check = connection.prepareStatement(
                    "SELECT order_status, buyer_confirmed, pet_id FROM orders WHERE order_id=?");
            check.setInt(1, orderId);
            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                return;
            }

            if (!"Pending".equalsIgnoreCase(rs.getString("order_status"))) {
                AlertUtil.showError("Cannot Approve", "This order has already been processed.");
                return;
            }

            boolean buyerConfirmed = rs.getBoolean("buyer_confirmed");
            int petId = rs.getInt("pet_id");

            PreparedStatement update = connection.prepareStatement(
                    "UPDATE orders SET order_status='Approved', seller_confirmed=1 WHERE order_id=?");
            update.setInt(1, orderId);
            update.executeUpdate();

            if (buyerConfirmed) {
                PreparedStatement complete = connection.prepareStatement(
                        "UPDATE orders SET order_status='Completed', completed_at=NOW() WHERE order_id=?");
                complete.setInt(1, orderId);
                complete.executeUpdate();

                PreparedStatement sold = connection.prepareStatement(
                        "UPDATE pets SET availability_status='SOLD' WHERE pet_id=?");
                sold.setInt(1, petId);
                sold.executeUpdate();

                AlertUtil.showInformation("Order Completed", "Order approved and transaction completed.");
            } else {
                AlertUtil.showInformation("Order Approved", "Waiting for the buyer to confirm receipt.");
            }

            loadOrders(null);

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtil.showError("Database Error", "Could not approve the order.");
        }
    }

    @FXML
    private void rejectOrder(ActionEvent e) {

        ObservableList<String> selected = tableOrders.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showError("No Selection", "Please select an order first.");
            return;
        }

        int orderId = Integer.parseInt(selected.get(0));

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reject Order");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to reject/cancel this order?");

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
                AlertUtil.showError("Cannot Reject", "Only pending orders can be rejected.");
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

            AlertUtil.showInformation("Order Rejected", "The order has been rejected.");
            loadOrders(null);

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtil.showError("Database Error", "Could not reject the order.");
        }
    }

    @FXML
    private void goBack(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/SellerDashboard.fxml");
    }
}
