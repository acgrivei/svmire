/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ceost.learn_sits;

import java.io.IOException;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.ceost.learn_sits.model.Sits;
import org.ceost.learn_sits.model.Classifier;

/**
 *
 * @author Alex
 */
public class SVMSceneController {

    @FXML
    private AnchorPane fxPaneSVMSettings;
    @FXML
    private ComboBox fxComboBoxSVMType;
    @FXML
    private ComboBox fxComboBoxKernelType;
    @FXML
    private Spinner fxSpinnerDegree;
    @FXML
    private TextField fxTextFieldGamma;
    @FXML
    private TextField fxTextFieldCoef;
    @FXML
    private TextField fxTextFieldNu;
    @FXML
    private TextField fxTextFieldCache;
    @FXML
    private TextField fxTextFieldC;
    @FXML
    private TextField fxTextFieldEps;
    @FXML
    private TextField fxTextFieldP;
    @FXML
    private ComboBox fxComboBoxShrinking;
    @FXML
    private ComboBox fxComboBoxProbability;
    @FXML
    private Spinner fxSpinnerNoWeights;

    //one-class SVM selection
    @FXML
    private ComboBox fxComboBoxClassOfInterest;
    @FXML
    private Label fxLabelNofPositives;
    @FXML
    private Label fxLabelNofNegatives;
    @FXML
    private Label fxLabelError;

    //features
    @FXML
    private ComboBox fxComboBoxFeatures;
    //normalize checkbox
    @FXML
    private CheckBox fxCheckBoxNormalize;
    //apply PCA checkbox
    @FXML
    private CheckBox fxCheckBoxPCA;
    @FXML
    private TextField fxTextFieldPCAComponents;

    //filter results
    @FXML
    private CheckBox fxCheckBoxFilterResults;
    @FXML
    private TextField fxTextFieldThreshold;

    //observable lists
    private ObservableList<String> svmTypes = FXCollections.observableArrayList();
    private ObservableList<String> kernelTypes = FXCollections.observableArrayList();
    private ObservableList<String> yesNo = FXCollections.observableArrayList();

    private ObservableList<String> features = FXCollections.observableArrayList();

    //declared instance of Classifier
    private Classifier c;
    //patches used for training
    private ArrayList<Sits> trainingPatches = new ArrayList<>();

    private ObservableList<String> clases = FXCollections.observableArrayList();

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        c = MainSceneController.classifier;

        //fill svm type combo box
        svmTypes.add("C-SVC");
        svmTypes.add("nu-SVC");
        svmTypes.add("one-class SVM");
        svmTypes.add("epsilon-SVR");
        svmTypes.add("nu-SVR");
        fxComboBoxSVMType.setItems(svmTypes);
        fxComboBoxSVMType.getSelectionModel().selectFirst();

        //fill kernel type combo box
        kernelTypes.add("linear");
        kernelTypes.add("polynomial");
        kernelTypes.add("radial");
        kernelTypes.add("sigmoid");
        fxComboBoxKernelType.setItems(kernelTypes);
        fxComboBoxKernelType.getSelectionModel().select(3);

        //fill yes/no combo boxes
        yesNo.add("No");
        yesNo.add("Yes");

        fxComboBoxProbability.setItems(yesNo);
        fxComboBoxShrinking.setItems(yesNo);

        //fill feature comboBox
        features.add("Fuzzy");
        features.add("Gabor");
        features.add("Haralick");
        features.add("Histogram");
        features.add("MeDiB");
        features.add("NDVBI-HB");
        features.add("Pixels");
        features.add("Sift");
        features.add("Surf");
        fxComboBoxFeatures.setItems(features);
        fxComboBoxFeatures.getSelectionModel().select("Pixels");

        fxCheckBoxPCA.setSelected(c.isPCAused());
        fxTextFieldPCAComponents.setText("" + c.getNofPCAComponents());

        //set default values
        fxComboBoxSVMType.getSelectionModel().select(c.getSVMtype());
        fxComboBoxKernelType.getSelectionModel().select(c.getKernelType());
        fxTextFieldGamma.setText("" + c.getGamma());
        fxTextFieldCoef.setText("" + c.getCoef());
        fxTextFieldNu.setText("" + c.getNu());
        fxTextFieldCache.setText("" + c.getCache());
        fxTextFieldC.setText("" + c.getC());
        fxTextFieldEps.setText("" + c.getEps());
        fxTextFieldP.setText("" + c.getP());
        fxComboBoxShrinking.getSelectionModel().select(c.getShrinking());
        fxComboBoxProbability.getSelectionModel().select(c.getProbability());
        fxSpinnerDegree.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, c.getDegree(), 1));
        fxSpinnerNoWeights.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, c.getNoWeights(), 1));
        fxComboBoxClassOfInterest.setItems(clases);

        fxCheckBoxFilterResults.setSelected(true);
        fxTextFieldThreshold.setText("" + MainSceneController.threshold);

    }

    @FXML
    private void train() throws IOException {
        System.out.println("Training started");
        //control variable
        boolean go = true;

        //instantiate variables
        int degree = c.getDegree();
        double gama = c.getGamma();
        double coef = c.getCoef();
        double nu = c.getNu();
        double cache = c.getCache();
        double C = c.getC();
        double eps = c.getEps();
        double P = c.getP();
        int nOfWeights = c.getNoWeights();
        int nOfPCAComponents = c.getNofPCAComponents();

        try {
            degree = Integer.parseInt(fxSpinnerDegree.getValue().toString());
            fxSpinnerDegree.getStyleClass().remove("errorTextField");
        } catch (NumberFormatException e) {
            go = false;
            fxSpinnerDegree.getStyleClass().add("errorTextField");
        }

        try {
            gama = Double.parseDouble(fxTextFieldGamma.getText());
            fxTextFieldGamma.getStyleClass().remove("errorTextField");
        } catch (NumberFormatException e) {
            go = false;
            fxTextFieldGamma.getStyleClass().add("errorTextField");
        }

        try {
            coef = Double.parseDouble(fxTextFieldCoef.getText());
            fxTextFieldCoef.getStyleClass().remove("errorTextField");
        } catch (NumberFormatException e) {
            go = false;
            fxTextFieldCoef.getStyleClass().add("errorTextField");
        }

        try {
            nu = Double.parseDouble(fxTextFieldNu.getText());
            fxTextFieldNu.getStyleClass().remove("errorTextField");
        } catch (NumberFormatException e) {
            go = false;
            fxTextFieldNu.getStyleClass().add("errorTextField");
        }

        try {
            cache = Double.parseDouble(fxTextFieldCache.getText());
            fxTextFieldCache.getStyleClass().remove("errorTextField");
        } catch (NumberFormatException e) {
            go = false;
            fxTextFieldCache.getStyleClass().add("errorTextField");
        }

        try {
            C = Double.parseDouble(fxTextFieldC.getText());
            fxTextFieldC.getStyleClass().remove("errorTextField");
        } catch (NumberFormatException e) {
            go = false;
            fxTextFieldC.getStyleClass().add("errorTextField");
        }

        try {
            eps = Double.parseDouble(fxTextFieldEps.getText());
            fxTextFieldEps.getStyleClass().remove("errorTextField");
        } catch (NumberFormatException e) {
            go = false;
            fxTextFieldEps.getStyleClass().add("errorTextField");
        }

        try {
            P = Double.parseDouble(fxTextFieldP.getText());
            fxTextFieldP.getStyleClass().remove("errorTextField");
        } catch (NumberFormatException e) {
            go = false;
            fxTextFieldP.getStyleClass().add("errorTextField");
        }

        try {
            nOfWeights = Integer.parseInt(fxSpinnerNoWeights.getValue().toString());
            fxSpinnerNoWeights.getStyleClass().remove("errorTextField");
        } catch (NumberFormatException e) {
            go = false;
            fxSpinnerNoWeights.getStyleClass().add("errorTextField");
        }

        try {
            nOfPCAComponents = Integer.parseInt(fxTextFieldPCAComponents.getText());
            fxTextFieldPCAComponents.getStyleClass().remove("errorTextField");
        } catch (NumberFormatException e) {
            go = false;
            fxTextFieldPCAComponents.getStyleClass().add("errorTextField");
        }

        if (fxCheckBoxFilterResults.isSelected()) {
            try {
                MainSceneController.threshold = Double.parseDouble(fxTextFieldThreshold.getText());
                fxTextFieldThreshold.getStyleClass().remove("errorTextField");
            } catch (NumberFormatException e) {
                go = false;
                fxTextFieldThreshold.getStyleClass().add("errorTextField");
            }
        } else {
            MainSceneController.threshold = 0;
        }
        //System.out.println(MainSceneController.threshold);

        if (go) {
            c.setSVMtype(fxComboBoxSVMType.getSelectionModel().getSelectedIndex());
            c.setKernelType(fxComboBoxKernelType.getSelectionModel().getSelectedIndex());
            c.setDegree(degree);
            c.setGamma(gama);
            c.setCoef(coef);
            c.setNu(nu);
            c.setCache(cache);
            c.setC(C);
            c.setEps(eps);
            c.setP(P);
            c.setC(C);
            c.setNoWeights(nOfWeights);
            c.setShrinking(fxComboBoxShrinking.getSelectionModel().getSelectedIndex());
            c.setProbability(fxComboBoxProbability.getSelectionModel().getSelectedIndex());
            c.setFeature(fxComboBoxFeatures.getSelectionModel().getSelectedItem().toString());
            c.setNormalize(fxCheckBoxNormalize.isSelected());
            c.setNofPCAComponents(nOfPCAComponents);
            c.setPCA(fxCheckBoxPCA.isSelected());

        }
        if (fxComboBoxSVMType.getSelectionModel().getSelectedIndex() == 2) {
            if (validateAndShowInfo() && go) {
                MainSceneController.startTrain = true;
                //build model for classification
                ArrayList<Sits> trainingSamples = new ArrayList<>();
                String clas = (String) fxComboBoxClassOfInterest.getSelectionModel().getSelectedItem();
                for (Sits patch : trainingPatches) {
                    if (patch.getClasa().equals(clas) & patch.isPositive()) {
                        trainingSamples.add(0, patch);
                    } else if (patch.getClasa().equals(clas) & !patch.isPositive()) {
                        trainingSamples.add(patch);
                    }
                }

                c.buildModel(trainingSamples);
                MainSceneController.classifier = c;
                exit();
            }
        } else if (go) {
            MainSceneController.startTrain = true;
            //build model for classification
            ArrayList<Sits> trainingSamples = new ArrayList<>();
            for (Sits patch : trainingPatches) {
                if (patch.isPositive()) {
                    trainingSamples.add(patch);
                }
            }

            c.buildModel(trainingSamples);
            MainSceneController.classifier = c;
            exit();
        }

    }

    private void exit() {
        Stage stage = (Stage) fxPaneSVMSettings.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void cancel() {
        MainSceneController.startTrain = false;
        exit();
    }

    public void setTrainingPatchesList(ArrayList<Sits> training) {
        clases.clear();
        for (Sits patch : training) {
            if (!clases.contains(patch.getClasa())) {
                clases.add(patch.getClasa());
            }
            trainingPatches.add(patch);
        }
        //validateAndShowInfo();
        System.out.println("Total number of training patches: " + trainingPatches.size());
        fxComboBoxClassOfInterest.getSelectionModel().selectFirst();

    }

    @FXML
    private boolean validateAndShowInfo() {
        String clas = (String) fxComboBoxClassOfInterest.getSelectionModel().getSelectedItem();
        int nOfPos = 0;
        int nOfNeg = 0;
        boolean ok = true;

        //count number of positive samples
        for (Sits patch : trainingPatches) {
            if (patch.getClasa().equals(clas) & patch.isPositive() == true) {
                nOfPos++;
            }
        }
        System.out.println("Number of positive patches for training: " + nOfPos);

        //count number of negative samples
        for (Sits patch : trainingPatches) {
            if (patch.getClasa().equals(clas) & patch.isPositive() == false) {
                nOfNeg++;
            }
        }
        System.out.println("Number of negative patches for training: " + nOfNeg);

        fxLabelNofPositives.setText("# of positives: " + nOfPos);
        fxLabelNofNegatives.setText("# of negatives: " + nOfNeg);
        if (nOfNeg == 0 || nOfPos == 0) {
            fxLabelError.setText("You need samples for both + and -!");
            fxLabelError.getStyleClass().add("errorTextField");
            ok = false;
        } else {
            fxLabelError.getStyleClass().remove("errorTextField");
            fxLabelError.setText("");

        }

        return ok;
    }

    void setClassifier(Classifier classifier) {

        c = classifier;

        fxComboBoxSVMType.getSelectionModel().select(c.getSVMtype());
        fxComboBoxKernelType.getSelectionModel().select(c.getKernelType());
        fxComboBoxProbability.getSelectionModel().select(c.getProbability());
        fxComboBoxShrinking.getSelectionModel().select(c.getShrinking());

        fxComboBoxSVMType.getSelectionModel().select(c.getSVMtype());
        fxComboBoxKernelType.getSelectionModel().select(c.getKernelType());
        fxTextFieldGamma.setText("" + c.getGamma());
        fxTextFieldCoef.setText("" + c.getCoef());
        fxTextFieldNu.setText("" + c.getNu());
        fxTextFieldCache.setText("" + c.getCache());
        fxTextFieldC.setText("" + c.getC());
        fxTextFieldEps.setText("" + c.getEps());
        fxTextFieldP.setText("" + c.getP());
        fxComboBoxShrinking.getSelectionModel().select(c.getShrinking());
        fxComboBoxProbability.getSelectionModel().select(c.getProbability());
        fxSpinnerDegree.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, c.getDegree(), c.getDegree()));
        fxSpinnerNoWeights.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, c.getNoWeights(), c.getNoWeights()));
        //if (!c.getLabels().isEmpty()) {
        //  fxComboBoxClassOfInterest.getSelectionModel().select(c.getLabels().get(0));
        //}
        fxComboBoxClassOfInterest.getSelectionModel().selectFirst();
        fxComboBoxFeatures.getSelectionModel().select(c.getFeature());
        fxCheckBoxNormalize.setSelected(c.isNormalize());
        fxCheckBoxPCA.setSelected(c.isPCAused());
        fxTextFieldPCAComponents.setText("" + c.getNofPCAComponents());

    }

}
