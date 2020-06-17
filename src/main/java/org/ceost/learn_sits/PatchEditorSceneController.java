/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ceost.learn_sits;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.ceost.learn_sits.model.Sits;

/**
 * FXML Controller class
 *
 * @author Alex
 */
public class PatchEditorSceneController implements Initializable {

    private ObservableList<String> clase = FXCollections.observableArrayList();
    public Sits patch;

    //objects from the interface
    @FXML
    private ImageView fxImageView;
    @FXML
    private Label fxLabelCurrentClass;
    @FXML
    private ComboBox fxComboBoxClasses;
    @FXML
    private ComboBox fxComboBoxPosNeg;
    @FXML
    private AnchorPane fxPanePatchEditor;

    private ObservableList<String> posNeg = FXCollections.observableArrayList();
    private MainSceneController parentController;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        posNeg.add("Positive");
        posNeg.add("Negative");
        fxComboBoxPosNeg.setItems(posNeg);
    }

    public void setPatch(Sits p) {
        this.patch = p;
        fxLabelCurrentClass.setText(patch.getClasa());
        /*
        fxImageView.setImage(
                new WritableImage(
                        MainSceneController.images.get(p.getParentID()).getPixelReader(),
                        p.getxIndex(),
                        p.getyIndex(),
                        p.getSize(),
                        p.getSize())
        );
        */
        fxImageView.setFitHeight(250);
        fxImageView.setFitWidth(250);
        fxComboBoxPosNeg.getSelectionModel().select(0);
        //fxComboBoxPosNeg.getSelectionModel().select((patch.isPositive() ? (int) 1 : (int) 0));
    }

    public void setClasses(ObservableList<String> clase) {
        this.clase.addAll(clase);
        fxComboBoxClasses.setItems(clase);
        if (clase.size() >= 1) {
            fxComboBoxClasses.getSelectionModel().select(clase.get(0).equals(patch.getClasa()) ? clase.get(1) : clase.get(0));
        } else {
            fxComboBoxClasses.getSelectionModel().selectFirst();
        }
    }

    @FXML
    public void changePatch() {
        if (fxComboBoxClasses.getSelectionModel().getSelectedItem() != null) {
            patch.setClasa(fxComboBoxClasses.getSelectionModel().getSelectedItem().toString());
            if (fxComboBoxPosNeg.getSelectionModel().isSelected(0)) {
                patch.setPositive(true);
            } else {
                patch.setPositive(false);
            }
            parentController.addTrainingSample(patch);
            Stage stage = (Stage) fxPanePatchEditor.getScene().getWindow();
            stage.close();
        } else {

        }
    }

    public void setParent(MainSceneController controller) {
        this.parentController = controller;
    }
}
