package com.example.pawpal.controller;

import com.example.pawpal.HelloApplication;
import com.example.pawpal.database.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class EditPetController {

@FXML private Label lblPetId;
@FXML private TextField txtPetName;
@FXML private ComboBox<String> cmbSpecies;
@FXML private TextField txtBreed;
@FXML private TextField txtAge;
@FXML private ComboBox<String> cmbGender;
@FXML private TextField txtPrice;
@FXML private CheckBox chkVaccinated;
@FXML private TextArea txtDescription;
@FXML private TextField txtImagePath;

private int petId;
private final Connection connection=DatabaseConnection.getInstance().getConnection();

@FXML
public void initialize(){
cmbSpecies.getItems().addAll("Dog","Cat","Bird","Rabbit","Fish","Other");
cmbGender.getItems().addAll("Male","Female");
}

public void setPetData(int petId,String petName,String species,String breed,int age,String gender,double price,boolean vaccinated,String description,String imagePath){
this.petId=petId;
lblPetId.setText("Pet ID: "+petId);
txtPetName.setText(petName);
cmbSpecies.setValue(species);
txtBreed.setText(breed);
txtAge.setText(String.valueOf(age));
cmbGender.setValue(gender);
txtPrice.setText(String.valueOf(price));
chkVaccinated.setSelected(vaccinated);
txtDescription.setText(description);
txtImagePath.setText(imagePath);
}

@FXML
private void updatePet(ActionEvent e){
try{

PreparedStatement pst=connection.prepareStatement("UPDATE pets SET pet_name=?, pet_type=?, breed=?, age=?, gender=?, price=?, description=?, vaccinated=?, image_path=? WHERE pet_id=?");
pst.setString(1,txtPetName.getText());
pst.setString(2,cmbSpecies.getValue());
pst.setString(3,txtBreed.getText());
pst.setInt(4,Integer.parseInt(txtAge.getText()));
pst.setString(5,cmbGender.getValue());
pst.setDouble(6,Double.parseDouble(txtPrice.getText()));
pst.setString(7,txtDescription.getText());
pst.setBoolean(8,chkVaccinated.isSelected());
pst.setString(9,txtImagePath.getText());
pst.setInt(10,petId);
pst.executeUpdate();
new Alert(Alert.AlertType.INFORMATION,"Pet updated successfully!").showAndWait();
HelloApplication.changeScene("/fxml/MyPets.fxml");
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
