package com.example.pawpal.controller;

import com.example.pawpal.database.DatabaseConnection;
import com.example.pawpal.session.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AddPetController {

@FXML private TextField txtPetName;
@FXML private ComboBox<String> cmbSpecies;
@FXML private TextField txtBreed;
@FXML private TextField txtAge;
@FXML private ComboBox<String> cmbGender;
@FXML private TextField txtPrice;
@FXML private CheckBox chkVaccinated;
@FXML private TextArea txtDescription;
@FXML private TextField txtImagePath;

private final Connection connection=DatabaseConnection.getInstance().getConnection();

@FXML
public void initialize(){
cmbSpecies.getItems().addAll("Dog","Cat","Bird","Rabbit","Fish","Other");
cmbGender.getItems().addAll("Male","Female");
}

    private int getStoreId() {

        String sql = "SELECT store_id FROM stores WHERE owner_id = ? AND approval_status = 'APPROVED'";

        try {

            PreparedStatement pst = connection.prepareStatement(sql);

            pst.setInt(1, Session.getCurrentUser().getUserId());

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {

                return rs.getInt("store_id");

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return -1;

    }

@FXML
private void addPet(ActionEvent e){
String sql="INSERT INTO pets(pet_name, pet_type, breed, age, gender, price, description, vaccinated, image_path, store_id)\n" +
        "VALUES(?,?,?,?,?,?,?,?,?,?)";
try{
PreparedStatement pst=connection.prepareStatement(sql);
pst.setString(1,txtPetName.getText());
pst.setString(2,cmbSpecies.getValue());
pst.setString(3,txtBreed.getText());
pst.setInt(4,Integer.parseInt(txtAge.getText()));
pst.setString(5,cmbGender.getValue());
pst.setDouble(6,Double.parseDouble(txtPrice.getText()));
pst.setString(7,txtDescription.getText());
pst.setBoolean(8,chkVaccinated.isSelected());
pst.setString(9,txtImagePath.getText());
    int storeId = getStoreId();

    if (storeId == -1) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText("You need an admin-approved store before you can add pets. Please set up your store profile and wait for approval.");
        alert.showAndWait();
        return;

    }

    pst.setInt(10, storeId);
pst.executeUpdate();
new Alert(Alert.AlertType.INFORMATION,"Pet Added Successfully!").showAndWait();
clearForm(null);
}catch(Exception ex){ex.printStackTrace();}
}

@FXML
private void clearForm(ActionEvent e){
txtPetName.clear();
cmbSpecies.getSelectionModel().clearSelection();
txtBreed.clear();
txtAge.clear();
cmbGender.getSelectionModel().clearSelection();
txtPrice.clear();
chkVaccinated.setSelected(false);
txtDescription.clear();
txtImagePath.clear();
}
}
