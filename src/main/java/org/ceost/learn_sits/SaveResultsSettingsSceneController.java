/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ceost.learn_sits;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Alex
 */
public class SaveResultsSettingsSceneController implements Initializable {

    //fxml components
    @FXML
    private AnchorPane fxAnchorPaneMain;
    @FXML
    private CheckBox fxCheckBoxSaveClassifiedPatches;
    @FXML
    private CheckBox fxCheckBoxSaveTrainingPatches;
    @FXML
    private CheckBox fxCheckBoxSaveFeatures;
    @FXML
    private CheckBox fxCheckBoxSaveClassifier;
    @FXML
    private CheckBox fxCheckBoxSaveSettingsFile;
    @FXML
    private CheckBox fxCheckBoxSaveSaveMask;
    @FXML
    private CheckBox fxCheckBoxAutoSave;

    MainSceneController parent;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void setParent(MainSceneController aThis) {
        parent = aThis;
        fxCheckBoxSaveClassifiedPatches.setSelected(parent.saveClassificationResults);
        fxCheckBoxSaveTrainingPatches.setSelected(parent.saveTrainingSamples);
        fxCheckBoxSaveFeatures.setSelected(parent.saveFeatures);
        fxCheckBoxSaveClassifier.setSelected(parent.saveClassifier);
        fxCheckBoxSaveSettingsFile.setSelected(parent.saveSettingsFile);
        fxCheckBoxSaveSaveMask.setSelected(parent.saveMask);
        fxCheckBoxAutoSave.setSelected(parent.autoSave);
    }

    @FXML
    private void close() {
        Stage stage = (Stage) fxAnchorPaneMain.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void saveSettings() {
        //classified patches
        if (fxCheckBoxSaveClassifiedPatches.isSelected()) {
            parent.saveClassificationResults = true;
        } else {
            parent.saveClassificationResults = false;
        }
        //training samples
        if (fxCheckBoxSaveTrainingPatches.isSelected()) {
            parent.saveTrainingSamples = true;
        } else {
            parent.saveTrainingSamples = false;
        }
        
        //features as txt
        if (fxCheckBoxSaveFeatures.isSelected()) {
            parent.saveFeatures = true;
        } else {
            parent.saveFeatures = false;
        }

        //classifier
        if (fxCheckBoxSaveClassifier.isSelected()) {
            parent.saveClassifier = true;
        } else {
            parent.saveClassifier = false;
        }

        //settings file
        if (fxCheckBoxSaveSettingsFile.isSelected()) {
            parent.saveSettingsFile = true;
        } else {
            parent.saveSettingsFile = false;
        }

        //mask
        if (fxCheckBoxSaveSaveMask.isSelected()) {
            parent.saveMask = true;
        } else {
            parent.saveMask = false;
        }

        //auto-save
        if (fxCheckBoxAutoSave.isSelected()) {
            parent.autoSave = true;
        } else {
            parent.autoSave = false;
        }

        close();
    }

}
