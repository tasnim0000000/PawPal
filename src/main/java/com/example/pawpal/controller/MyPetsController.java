package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import com.example.pawpal.session.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;

public class MyPetsController {

@FXML private TableView<ObservableList<String>> tablePets;
@FXML private TableColumn<ObservableList<String>,String> colId,colName,colSpecies,colBreed,colPrice,colStatus;

private final Connection connection=DatabaseConnection.getInstance().getConnection();

@FXML
public void initialize(){
colId.setCellValueFactory(d->new SimpleStringProperty(d.getValue().get(0)));
colName.setCellValueFactory(d->new SimpleStringProperty(d.getValue().get(1)));
colSpecies.setCellValueFactory(d->new SimpleStringProperty(d.getValue().get(2)));
colBreed.setCellValueFactory(d->new SimpleStringProperty(d.getValue().get(3)));
colPrice.setCellValueFactory(d->new SimpleStringProperty(d.getValue().get(4)));
colStatus.setCellValueFactory(d->new SimpleStringProperty(d.getValue().get(5)));
loadPets(null);
}

private int getStoreId() throws SQLException{
PreparedStatement pst=connection.prepareStatement("SELECT store_id FROM stores WHERE owner_id=?");
pst.setInt(1,Session.getCurrentUser().getUserId());
ResultSet rs=pst.executeQuery();
return rs.next()?rs.getInt("store_id"):-1;
}

@FXML
public void loadPets(ActionEvent e){
ObservableList<ObservableList<String>> data=FXCollections.observableArrayList();
try{
PreparedStatement pst=connection.prepareStatement("SELECT pet_id,pet_name,species,breed,price,adoption_status FROM pets WHERE store_id=?");
pst.setInt(1,getStoreId());
ResultSet rs=pst.executeQuery();
while(rs.next()){
ObservableList<String> r=FXCollections.observableArrayList();
r.add(rs.getString(1));
r.add(rs.getString(2));
r.add(rs.getString(3));
r.add(rs.getString(4));
r.add(rs.getString(5));
r.add(rs.getString(6));
data.add(r);
}
tablePets.setItems(data);
}catch(Exception ex){ex.printStackTrace();}
}

    @FXML
    private void editPet(ActionEvent event) {

        ObservableList<String> selectedPet =
                tablePets.getSelectionModel().getSelectedItem();

        if (selectedPet == null) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select a pet first.");
            alert.showAndWait();
            return;
        }

        int petId = Integer.parseInt(selectedPet.get(0));

        try {

            String sql = "SELECT * FROM pets WHERE pet_id=?";

            PreparedStatement pst = connection.prepareStatement(sql);

            pst.setInt(1, petId);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {

                FXMLLoader loader = new FXMLLoader(
                        HelloApplication.class.getResource("/fxml/EditPet.fxml"));

                Parent root = loader.load();

                EditPetController controller = loader.getController();

                controller.setPetData(

                        rs.getInt("pet_id"),

                        rs.getString("pet_name"),

                        rs.getString("pet_type"),

                        rs.getString("breed"),

                        rs.getInt("age"),

                        rs.getString("gender"),

                        rs.getDouble("price"),

                        rs.getBoolean("vaccinated"),

                        rs.getString("description"),

                        rs.getString("image_path")

                );

                Stage stage =
                        (Stage) tablePets.getScene().getWindow();

                stage.setScene(new Scene(root));

                stage.show();

            }

        }

        catch (Exception e) {

            e.printStackTrace();

        }

    }
    @FXML
    private void deletePet(ActionEvent event) {

        ObservableList<String> selectedPet =
                tablePets.getSelectionModel().getSelectedItem();

        if (selectedPet == null) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select a pet first.");
            alert.showAndWait();

            return;
        }

        int petId = Integer.parseInt(selectedPet.get(0));

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Delete Pet");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this pet?");

        if (confirm.showAndWait().get() == ButtonType.OK) {

            try {

                String sql = "DELETE FROM pets WHERE pet_id=?";

                PreparedStatement pst =
                        connection.prepareStatement(sql);

                pst.setInt(1, petId);

                int row = pst.executeUpdate();

                if (row > 0) {

                    Alert success =
                            new Alert(Alert.AlertType.INFORMATION);

                    success.setHeaderText(null);

                    success.setContentText("Pet deleted successfully.");

                    success.showAndWait();

                    loadPets(null);

                }

            }

            catch (Exception e) {

                e.printStackTrace();

            }

        }

    }
@FXML private void goBack(ActionEvent e) throws Exception{HelloApplication.changeScene("/fxml/SellerDashboard.fxml");}
}
