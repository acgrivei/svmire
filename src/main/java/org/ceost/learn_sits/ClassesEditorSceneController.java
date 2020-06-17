/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ceost.learn_sits;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * @author Alex
 */
public class ClassesEditorSceneController {

    private Stage dialogStage;
    private boolean okClicked = false;
    ObservableList<String> clase;
    //Reference to the vertical list from the window
    @FXML
    ListView fxClasesList;
    //Reference to the text field at the botom of the window
    @FXML
    TextField fxTextField;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    }

    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Fils the vertical list with the classes contained in the combo box in the
     * main window
     *
     * @param clase
     */
    public void fillClasesList(ObservableList<String> clase) {
        this.clase = clase;
        fxClasesList.setItems(clase);

    }

    /**
     * Add a class to the list
     */
    @FXML
    private void addClass() {
        System.out.println("Class added!");
        if (!fxTextField.getText().equals("")) {
            clase.add(fxTextField.getText());
        }        
    }

    /**
     * Removes a class from the list
     */
    @FXML
    private void removeClass() {
        clase.removeAll(fxClasesList.getSelectionModel().getSelectedItems());
        System.out.println("Class removed!");
    }

}
