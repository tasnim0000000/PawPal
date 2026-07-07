package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import com.example.pawpal.session.Session;
import com.example.pawpal.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;

public class PetDetailsController {

    @FXML private Label lblPetName;
    @FXML private Label lblType;
    @FXML private Label lblBreed;
    @FXML private Label lblAge;
    @FXML private Label lblGender;
    @FXML private Label lblVaccinated;
    @FXML private Label lblPrice;
    @FXML private Label lblStatus;
    @FXML private Label lblStore;
    @FXML private Label lblStoreRating;
    @FXML private TextArea txtDescriptionView;

    @FXML private TextField txtDeliveryAddress;
    @FXML private TextField txtContactPhone;
    @FXML private ComboBox<String> cmbPaymentMethod;
    @FXML private Button btnPlaceOrder;

    @FXML private TextArea txtReportReason;
    @FXML private Button btnReportPet;

    @FXML private ListView<String> listComments;
    @FXML private TextArea txtNewComment;

    @FXML private VBox reviewBox;
    @FXML private ComboBox<Integer> cmbRatingValue;
    @FXML private TextArea txtReviewText;
    @FXML private Button btnSubmitReview;

    @FXML private ListView<String> listReviews;

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    private int petId;
    private int storeId;
    private String availabilityStatus;

    @FXML
    public void initialize() {
        cmbPaymentMethod.getItems().addAll("Cash On Delivery", "Bkash", "Nagad");
        cmbPaymentMethod.setValue("Cash On Delivery");
        cmbRatingValue.getItems().addAll(1, 2, 3, 4, 5);
        cmbRatingValue.setValue(5);
    }

    public void loadPet(int petId) {
        this.petId = petId;

        String sql = "SELECT p.*, s.store_name, s.average_rating, s.store_id AS s_store_id " +
                "FROM pets p JOIN stores s ON p.store_id = s.store_id WHERE p.pet_id = ?";

        try {
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, petId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                storeId = rs.getInt("s_store_id");
                availabilityStatus = rs.getString("availability_status");

                lblPetName.setText(rs.getString("pet_name"));
                lblType.setText(rs.getString("pet_type"));
                lblBreed.setText(rs.getString("breed"));
                lblAge.setText(rs.getInt("age") + " month(s)");
                lblGender.setText(rs.getString("gender"));
                lblVaccinated.setText(rs.getBoolean("vaccinated") ? "Yes" : "No");
                lblPrice.setText(String.format("%.2f BDT", rs.getDouble("price")));
                lblStatus.setText(availabilityStatus);
                lblStore.setText(rs.getString("store_name"));
                lblStoreRating.setText(String.format("%.1f \u2605", rs.getDouble("average_rating")));
                txtDescriptionView.setText(rs.getString("description"));

                btnPlaceOrder.setDisable(!"AVAILABLE".equalsIgnoreCase(availabilityStatus));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        loadComments();
        loadReviews();
        checkReviewEligibility();
    }

    private void loadComments() {

        listComments.getItems().clear();

        String sql = "SELECT c.comment, c.commented_at, u.full_name, u.role " +
                "FROM comments c JOIN users u ON c.user_id = u.user_id " +
                "WHERE c.pet_id = ? ORDER BY c.commented_at DESC";

        try {
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, petId);
            ResultSet rs = pst.executeQuery();

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

            while (rs.next()) {
                String when = rs.getTimestamp("commented_at") != null
                        ? rs.getTimestamp("commented_at").toLocalDateTime().format(fmt)
                        : "";
                listComments.getItems().add(
                        rs.getString("full_name") + " (" + rs.getString("role") + ") - " + when +
                                "\n" + rs.getString("comment"));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void postComment(ActionEvent e) {

        String comment = txtNewComment.getText() == null ? "" : txtNewComment.getText().trim();

        if (comment.isEmpty()) {
            AlertUtil.showError("Empty Comment", "Please write something before posting.");
            return;
        }

        String sql = "INSERT INTO comments(pet_id, user_id, comment) VALUES(?,?,?)";

        try {
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, petId);
            pst.setInt(2, Session.getCurrentUser().getUserId());
            pst.setString(3, comment);
            pst.executeUpdate();

            txtNewComment.clear();
            loadComments();

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtil.showError("Database Error", "Could not post your comment.");
        }
    }

    @FXML
    private void placeOrder(ActionEvent e) {

        String address = txtDeliveryAddress.getText() == null ? "" : txtDeliveryAddress.getText().trim();
        String phone = txtContactPhone.getText() == null ? "" : txtContactPhone.getText().trim();
        String payment = cmbPaymentMethod.getValue();

        if (address.isEmpty() || phone.isEmpty()) {
            AlertUtil.showError("Missing Information", "Please enter a delivery address and contact phone.");
            return;
        }

        try {
            PreparedStatement check = connection.prepareStatement(
                    "SELECT order_id FROM orders WHERE buyer_id=? AND pet_id=? AND order_status IN ('Pending','Approved')");
            check.setInt(1, Session.getCurrentUser().getUserId());
            check.setInt(2, petId);

            if (check.executeQuery().next()) {
                AlertUtil.showError("Order Exists", "You already have an active order for this pet.");
                return;
            }

            PreparedStatement pst = connection.prepareStatement(
                    "INSERT INTO orders(buyer_id, pet_id, delivery_address, contact_phone, payment_method) VALUES(?,?,?,?,?)");
            pst.setInt(1, Session.getCurrentUser().getUserId());
            pst.setInt(2, petId);
            pst.setString(3, address);
            pst.setString(4, phone);
            pst.setString(5, payment);
            pst.executeUpdate();

            PreparedStatement update = connection.prepareStatement(
                    "UPDATE pets SET availability_status='RESERVED' WHERE pet_id=?");
            update.setInt(1, petId);
            update.executeUpdate();

            AlertUtil.showInformation("Order Placed", "Your order has been placed! The store owner will review and approve it soon.");

            loadPet(petId);

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtil.showError("Database Error", "Could not place the order.");
        }
    }

    @FXML
    private void reportPet(ActionEvent e) {

        String reason = txtReportReason.getText() == null ? "" : txtReportReason.getText().trim();

        if (reason.isEmpty()) {
            AlertUtil.showError("Missing Reason", "Please describe why you are reporting this pet listing.");
            return;
        }

        String sql = "INSERT INTO reports(pet_id, user_id, reason) VALUES(?,?,?)";

        try {
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, petId);
            pst.setInt(2, Session.getCurrentUser().getUserId());
            pst.setString(3, reason);
            pst.executeUpdate();

            txtReportReason.clear();
            AlertUtil.showInformation("Report Submitted", "Thanks for letting us know. Our admin team will review it.");

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtil.showError("Database Error", "Could not submit the report.");
        }
    }

    private void loadReviews() {

        listReviews.getItems().clear();

        String sql = "SELECT r.rating, r.review, r.rated_at, u.full_name " +
                "FROM ratings r JOIN users u ON r.buyer_id = u.user_id " +
                "WHERE r.pet_id = ? ORDER BY r.rated_at DESC";

        try {
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, petId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String stars = "\u2605".repeat(rs.getInt("rating"));
                String review = rs.getString("review");
                listReviews.getItems().add(
                        rs.getString("full_name") + " - " + stars +
                                (review != null && !review.isEmpty() ? "\n" + review : ""));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkReviewEligibility() {

        reviewBox.setVisible(false);
        reviewBox.setManaged(false);

        if (Session.getCurrentUser() == null || !"BUYER".equalsIgnoreCase(Session.getCurrentUser().getRole())) {
            return;
        }

        try {
            PreparedStatement completed = connection.prepareStatement(
                    "SELECT order_id FROM orders WHERE buyer_id=? AND pet_id=? AND order_status='Completed'");
            completed.setInt(1, Session.getCurrentUser().getUserId());
            completed.setInt(2, petId);

            if (!completed.executeQuery().next()) {
                return;
            }

            PreparedStatement alreadyRated = connection.prepareStatement(
                    "SELECT rating_id FROM ratings WHERE buyer_id=? AND pet_id=?");
            alreadyRated.setInt(1, Session.getCurrentUser().getUserId());
            alreadyRated.setInt(2, petId);

            if (alreadyRated.executeQuery().next()) {
                return;
            }

            reviewBox.setVisible(true);
            reviewBox.setManaged(true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void submitReview(ActionEvent e) {

        Integer rating = cmbRatingValue.getValue();
        String review = txtReviewText.getText() == null ? "" : txtReviewText.getText().trim();

        if (rating == null) {
            AlertUtil.showError("Missing Rating", "Please select a rating from 1 to 5.");
            return;
        }

        try {
            PreparedStatement pst = connection.prepareStatement(
                    "INSERT INTO ratings(buyer_id, store_id, pet_id, rating, review) VALUES(?,?,?,?,?)");
            pst.setInt(1, Session.getCurrentUser().getUserId());
            pst.setInt(2, storeId);
            pst.setInt(3, petId);
            pst.setInt(4, rating);
            pst.setString(5, review);
            pst.executeUpdate();

            PreparedStatement recalc = connection.prepareStatement(
                    "UPDATE stores SET average_rating = (SELECT ROUND(AVG(rating),1) FROM ratings WHERE store_id=?) WHERE store_id=?");
            recalc.setInt(1, storeId);
            recalc.setInt(2, storeId);
            recalc.executeUpdate();

            AlertUtil.showInformation("Review Submitted", "Thank you for your feedback!");

            txtReviewText.clear();
            loadPet(petId);

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtil.showError("Database Error", "Could not submit your review.");
        }
    }

    @FXML
    private void goBack(ActionEvent e) throws Exception {
        HelloApplication.changeScene("/fxml/BrowsePets.fxml");
    }
}
