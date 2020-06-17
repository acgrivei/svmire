package org.ceost.learn_sits;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.ceost.learn_sits.model.Sits;
import org.ceost.learn_sits.model.Classifier;
import org.ceost.learn_sits.model.Patch;
import org.ceost.learn_sits.model.SVMSettings;
import org.ceost.learn_sits.model.Settings;
import org.ceost.learn_sits.util.ImageProcessor;
import org.ceost.learn_sits.util.XMLProcessor;

/**
 * Controller for main window
 *
 * @author Alex
 */
public class MainSceneController implements Initializable {

    //marked area color
    public static final Color DEFAULT = Color.WHITESMOKE.deriveColor(1, 1, 1, 0.05);
    public static final Color POSITIVE = Color.GREEN.deriveColor(1, 1, 1, 0.75);
    public static final Color NEGATIVE = Color.RED.deriveColor(1, 1, 1, 0.75);
    //deactivated patches
    public static final Color DEACT_POSITIVE = Color.GREEN.deriveColor(1, 1, 1, 0.3);
    public static final Color DEACT_NEGATIVE = Color.RED.deriveColor(1, 1, 1, 0.3);
    public static final Color DEACT_DEFAULT = Color.WHITESMOKE.deriveColor(1, 1, 1, 0.3);

    //used color
    public static Color usedColor = DEFAULT;

    //interface elements
    @FXML
    private BorderPane fxMainBorderPane;
    @FXML
    private ComboBox<String> fxComboBox;
    @FXML
    private ComboBox<String> fxComboBoxClassified;
    @FXML
    private CheckBox negativesCheckBoxShow;

    @FXML
    private ComboBox<String> fxComboBoxMostRelevantTop;
    @FXML
    private CheckBox negativesCheckBoxTop;
    @FXML
    private ComboBox<String> fxComboBoxMostRelevantBottom;
    @FXML
    private CheckBox negativesCheckBoxBottom;

    @FXML
    private TextField fxTextFieldGridSize;
    @FXML
    private ToggleButton fxButtonPositives;
    @FXML
    private ToggleButton fxButtonNegatives;
    @FXML
    private ScrollBar fxScrollBar;
    @FXML
    private Label fxLabelErrorGridSize;
    @FXML
    private Label fxPositiveSamplesLabel;
    @FXML
    private Pane fxImagePane;
    @FXML
    private ListView fxListofPositives;
    @FXML
    private ListView fxListofNegatives;

    //table for top relevant view 
    @FXML
    private TableView<Sits> fxTableClassificationResultsTop;
    @FXML
    private TableColumn<Sits, ArrayList<Patch>> fxTableColumnPatchTop;
    @FXML
    private TableColumn<Sits, String> fxTableColumnClassTop;
    @FXML
    private TableColumn<Sits, Double> fxTableColumnRelevanceTop;

    //table for bottom relevant view
    @FXML
    private TableView<Sits> fxTableClassificationResultsBottom;
    @FXML
    private TableColumn<Sits, ArrayList<Patch>> fxTableColumnPatchBottom;
    @FXML
    private TableColumn<Sits, String> fxTableColumnClassBottom;
    @FXML
    private TableColumn<Sits, Double> fxTableColumnRelevanceBottom;
    @FXML
    private Label fxLabelIterations;

    //clases to populate combobox
    private ObservableList<String> clase = FXCollections.observableArrayList();
    //classified patches classes
    private ObservableList<String> classifiedClasses = FXCollections.observableArrayList();

    //the list shown in top list
    private static ObservableList<Sits> topList = FXCollections.observableArrayList();
    //
    private static ObservableList<Sits> bottomList = FXCollections.observableArrayList();

    //labels which show the accuracy and precision values
    @FXML
    private Label fxLabelPrecision;
    @FXML
    private Label fxLabelRecall;
    @FXML
    private Label fxLabelAccuracy;
    @FXML
    private Label fxLabelFmeasure;
    @FXML
    private ComboBox<String> fxComboBoxReference;
    private File groundTruth;

    //list of training patches 
    private static ArrayList<Sits> trainingSamples = new ArrayList<>();
    //list of all selected positive patches
    private static ObservableList<Sits> selectedPositives = FXCollections.observableArrayList();
    //list of all selected negative patches
    private static ObservableList<Sits> selectedNegatives = FXCollections.observableArrayList();

    //list of all patches from all images 
    private static ArrayList<Sits> classifiedPatches = new ArrayList<>();
    //list of fixed classiffied patches
    private static ArrayList<Sits> fixedClassifiedPatches = new ArrayList<>();
    //threshold value for filtering
    public static double threshold = 0;

    //returned classifier
    public static Classifier classifier = new Classifier();

    //displayed RGB image
    //public static Image displayImage;
    //list of loaded images
    public static ArrayList<String> files = new ArrayList<>();
    public static ArrayList<Image> images = new ArrayList<>();
    public static ArrayList<BufferedImage> buffImages = new ArrayList<>();

    //preset grid size
    private static int gridSize = 100;
    private static double xRatio = 0.0;
    private static double yRatio = 0.0;

    //the scale of the image pane
    public static DoubleProperty myScale = new SimpleDoubleProperty(0.7);
    //public static boolean isModifiedPos = true;
    public static boolean startTrain = true;

    public static int reiteration = -1;

    //last opened directory
    private File lastLoad;
    //last save results
    private File lastSave;

    //save settings
    public boolean saveTrainingSamples = false;
    public boolean saveClassificationResults = false;
    public boolean saveFeatures = false;
    public boolean saveClassifier = false;
    public boolean saveMask = false;
    public boolean saveSettingsFile = false;
    public boolean autoSave = false;

    public static int redBand = 0;
    public static int greenBand = 0;
    public static int blueBand = 0;
    public static int nirBand = 0;
    public static int mirBand = 0;

    /**
     * Creates default settings for main window
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //TODO
        //fill comboBox
        fillComboBoxs();

        fxPositiveSamplesLabel.setText("Positive samples for "
                + fxComboBox.getSelectionModel().getSelectedItem());
        //clear the lists for pozitive and negative samples 
        initializeAdnotations();

        //format positives list and map positive samples
        fxListofPositives.setCellFactory(l -> new ImageCell());
        fxListofPositives.setItems(selectedPositives);
        //format negatives list and map negatives samples 
        //fxListofNegatives.setCellFactory(l -> new ImageCell());
        //fxListofNegatives.setItems(selectedNegatives);

        fxButtonPositives.setText(0 + " Positives");
        //fxButtonNegatives.setText(0 + " Negatives");

        //format the imagePane
        fxImagePane.prefHeightProperty().bind(fxMainBorderPane.heightProperty().multiply(0.5));
        fxImagePane.prefWidthProperty().bind(fxMainBorderPane.widthProperty().multiply(0.5));

        //configure top table
        fxTableColumnPatchTop.setCellValueFactory(new PropertyValueFactory<>("sits"));
        fxTableColumnPatchTop.setPrefWidth(200);
        fxTableColumnClassTop.setCellValueFactory(new PropertyValueFactory<Sits, String>("clasa"));
        fxTableColumnRelevanceTop.setCellValueFactory(new PropertyValueFactory<Sits, Double>("relevance"));
        fxTableClassificationResultsTop.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        fxTableColumnPatchTop.setCellFactory(new Callback<TableColumn<Sits, ArrayList<Patch>>, TableCell<Sits, ArrayList<Patch>>>() {

            @Override
            public TableCell<Sits, ArrayList<Patch>> call(TableColumn<Sits, ArrayList<Patch>> param) {
                return new TableCell<Sits, ArrayList<Patch>>() {

                    @Override
                    public void updateItem(ArrayList<Patch> s, boolean bln) {
                        if (s != null) {

                            HBox imgList = new HBox(10);

                            for (Patch p : s) {
                                ImageView intermImg = new ImageView(
                                        new WritableImage(
                                                MainSceneController.images.get(p.getParentID()).getPixelReader(),
                                                p.getxIndex(),
                                                p.getyIndex(),
                                                p.getSize(),
                                                p.getSize())
                                );
                                intermImg.setFitWidth(45);
                                intermImg.setPreserveRatio(true);
                                imgList.getChildren().add(intermImg);

                            }
                            imgList.setPadding(new Insets(5));

                            setGraphic(imgList);
                            setText(null);

                        } else {
                            setGraphic(null);
                            setText(null);
                        }

                    }

                };

            }
        }
        );

        negativesCheckBoxBottom.setSelected(false);
        //
        fxTableColumnPatchBottom.setCellValueFactory(new PropertyValueFactory<>("sits"));
        fxTableColumnPatchBottom.setPrefWidth(200);
        fxTableColumnClassBottom.setCellValueFactory(new PropertyValueFactory<>("clasa"));
        fxTableColumnRelevanceBottom.setCellValueFactory(new PropertyValueFactory<Sits, Double>("relevance"));
        //fxTableClassificationResultsBottom.setItems(classifiedPatches);
        fxTableClassificationResultsBottom.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        fxTableColumnPatchBottom.setCellFactory(new Callback<TableColumn<Sits, ArrayList<Patch>>, TableCell<Sits, ArrayList<Patch>>>() {

            @Override
            public TableCell<Sits, ArrayList<Patch>> call(TableColumn<Sits, ArrayList<Patch>> param) {
                return new TableCell<Sits, ArrayList<Patch>>() {

                    @Override
                    public void updateItem(ArrayList<Patch> s, boolean bln) {
                        if (s != null) {

                            HBox imgList = new HBox(10);

                            for (Patch p : s) {
                                ImageView intermImg = new ImageView(
                                        new WritableImage(
                                                MainSceneController.images.get(p.getParentID()).getPixelReader(),
                                                p.getxIndex(),
                                                p.getyIndex(),
                                                p.getSize(),
                                                p.getSize())
                                );
                                intermImg.setFitWidth(45);
                                intermImg.setPreserveRatio(true);
                                imgList.getChildren().add(intermImg);

                            }
                            imgList.setPadding(new Insets(5));

                            setGraphic(imgList);
                            setText(null);

                        } else {
                            setGraphic(null);
                            setText(null);
                        }

                    }

                };

            }

        }
        );

        fxTableClassificationResultsTop.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    try {
                        editPatch();
                    } catch (IOException ex) {
                        Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if (event.isPrimaryButtonDown() && event.getClickCount() == 1) {
                    //showTableSelection();
                }
            }
        });
        fxTableClassificationResultsBottom.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    try {
                        editPatch();
                    } catch (IOException ex) {
                        Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if (event.isPrimaryButtonDown() && event.getClickCount() == 1) {
                    //showTableSelection();
                }
            }
        });

    }

    @FXML
    private void changeBackgroundTop() {
        if (negativesCheckBoxTop.isSelected()) {
            fxTableClassificationResultsTop.setStyle("-fx-background-color:red");
        } else {
            fxTableClassificationResultsTop.setStyle("-fx-background-color:green");
        }
    }

    @FXML
    private void changeBackgroundBottom() {
        if (negativesCheckBoxBottom.isSelected()) {
            fxTableClassificationResultsBottom.setStyle("-fx-background-color:red");
        } else {
            fxTableClassificationResultsBottom.setStyle("-fx-background-color:green");
        }
    }

    @FXML
    private void fillTopTable() {
        topList.clear();

        String topClass = fxComboBoxMostRelevantTop.getSelectionModel().getSelectedItem();
        boolean notPositive = (negativesCheckBoxTop.isSelected() ? false : true);
        for (Sits p : classifiedPatches) {
            if (p.getClasa().equals(topClass) && p.isPositive() == notPositive) {
                topList.add(p);
            }
        }
        fxTableClassificationResultsTop.setItems(topList);
        fxTableColumnRelevanceTop.setSortType(TableColumn.SortType.DESCENDING);
        //fxTableClassificationResultsTop.setItems(classifiedPatches);        
        fxTableClassificationResultsTop.getSortOrder().add(fxTableColumnRelevanceTop);
        fxTableClassificationResultsTop.sort();
        changeBackgroundTop();
        //updateSelectedNegatives();
        updateSelectedPositives();
    }

    @FXML
    private void fillBottomTable() {
        bottomList.clear();

        String bottomClass = fxComboBoxMostRelevantBottom.getSelectionModel().getSelectedItem();
        boolean notPositive = (negativesCheckBoxBottom.isSelected() ? false : true);
        for (Sits p : classifiedPatches) {
            if (p.getClasa().equals(bottomClass) && p.isPositive() == notPositive) {
                bottomList.add(p);
            }
        }
        fxTableClassificationResultsBottom.setItems(bottomList);
        fxTableColumnRelevanceBottom.setSortType(TableColumn.SortType.DESCENDING);
        //fxTableClassificationResultsTop.setItems(classifiedPatches);        
        fxTableClassificationResultsBottom.getSortOrder().add(fxTableColumnRelevanceBottom);
        fxTableClassificationResultsBottom.sort();
        changeBackgroundBottom();
        //updateSelectedNegatives();
        updateSelectedPositives();
    }

    @FXML
    private void putClassifiedInToBeClassified() {
        classifiedPatches.addAll(fixedClassifiedPatches);
        fixedClassifiedPatches.clear();
    }

    /*
    @FXML
    private void calculateMetrics() throws IOException {
        if (groundTruth == null) {
            setReference();
        }

        //calculate statistics(truePos, trueNeg, falsePos, falseNeg)
        int[] stat = ImageProcessor.getStatistics(
                groundTruth,
                fxComboBoxReference.getSelectionModel().getSelectedItem(),
                classifiedPatches,
                fixedClassifiedPatches
        );

        //calculate and show accuracy
        fxLabelAccuracy.setText(""
                + ImageProcessor.getAccuracy(stat)
        );

        //calculate and show recall
        fxLabelRecall.setText(""
                + ImageProcessor.getRecall(stat)
        );

        //calculate and show precision
        fxLabelPrecision.setText(""
                + ImageProcessor.getPrecision(stat)
        );

        //calculate and show F-measure
        fxLabelFmeasure.setText(""
                + ImageProcessor.getFmeasure(stat)
        );

    }
     */
    @FXML
    private void setReference() {
        FileChooser fileChooser = new FileChooser();

        if (lastLoad != null) {
            File existDirectory = lastLoad.getParentFile();
            fileChooser.setInitialDirectory(existDirectory);
        }
        fileChooser.setTitle("Select ground truth");
        groundTruth = fileChooser.showOpenDialog(null);
    }

    private void updateSelectedPositives() {
        selectedPositives.clear();
        for (Sits s : trainingSamples) {
            if (s.getClasa().equals(fxComboBox.getSelectionModel().getSelectedItem()) && s.isPositive()) {
                selectedPositives.add(s);
            }
        }
        fxButtonPositives.setText(selectedPositives.size() + " Positives");
    }

    /*
    private void updateSelectedNegatives() {
        selectedNegatives.clear();
        for (Sits p : trainingSamples) {
            if (p.getClasa().equals(fxComboBox.getSelectionModel().getSelectedItem()) && !p.isPositive()) {
                selectedNegatives.add(p);
            }
        }
        fxButtonNegatives.setText(selectedNegatives.size() + " Negatives");

    }
     */
    /**
     * Formats the cell for the horizontal list containing the positive samples
     */
    static class ImageCell extends ListCell<Sits> {

        @Override
        public void updateItem(Sits s, boolean bln) {
            super.updateItem(s, bln);
            if (s != null) {
                ArrayList<Patch> patchList = s.getSits();
                HBox imgList = new HBox(10);

                for (Patch p : patchList) {
                    ImageView intermImg = new ImageView(
                            new WritableImage(
                                    MainSceneController.images.get(p.getParentID()).getPixelReader(),
                                    p.getxIndex(),
                                    p.getyIndex(),
                                    p.getSize(),
                                    p.getSize())
                    );
                    intermImg.setFitWidth(45);
                    intermImg.setPreserveRatio(true);
                    imgList.getChildren().add(intermImg);

                }
                imgList.setPadding(new Insets(5));

                setGraphic(imgList);
                setText(null);

            } else {
                setGraphic(null);
                setText(null);
            }
        }
    }

    /**
     * Shows on the current displayed image the patches classified to the class
     * specified by the combobox
     */
    @FXML
    private void showClassified() {
        ArrayList<Sits> positives = new ArrayList<>();
        ArrayList<Sits> negatives = new ArrayList<>();
        //get the class from the combobox
        String cls = fxComboBoxClassified.getSelectionModel().getSelectedItem();
        boolean positive = (!negativesCheckBoxShow.isSelected());

        int index = (int) fxScrollBar.getValue(); //image index from scroll bar
        //go trough the list of classified patches

        //go trough the classified patches and group into classes
        for (Sits p : classifiedPatches) {
            //if the class of the patch is the same as the one selected in the combobox
            //add it to the positive samples to be displayed
            /*if (positive) {
                if (p.getClasa().equals(cls)
                        && p.getParentID() == index
                        && p.isPositive() == true) {
                    positives.add(p);
                }
            } else if (p.getClasa().equals(cls)
                    && p.getParentID() == index
                    && p.isPositive() == false) {
                negatives.add(p);
            }
             */
            if (p.getClasa().equals(cls)
                    && p.isPositive() == true) {
                positives.add(p);
            }
            if (!p.getClasa().equals(cls)
                    && p.isPositive() == positive) {
                negatives.add(p);

            }
        }
        //System.out.println("Number of negative samples: " + negatives.size());
        /*
         //go trough the training samples and group into classes
         for (Patch p : trainingSamples) {
         //if the class of the patch is the same as the one selected in the combobox
         //add it to the positive samples to be displayed
         if (positive) {
         if (p.getClasa().equals(cls)
         && p.getParentID().equals(files.get(index))
         && p.isPositive() == true) {
         positives.add(p);
         }
         } else if (p.getClasa().equals(cls)
         && p.getParentID().equals(files.get(index))
         && p.isPositive() == false) {
         negatives.add(p);
         }

         }
         */

        //draw scene with the specified parameters
        //drawScene(images.get(index), gridSize, positives, negatives);
        updateSceneToBeClassified(images.get(index), gridSize, positives, negatives);
        showFixedClassified();
    }

    /**
     * Shows on the current displayed image the patches classified to the class
     * specified by the combobox
     */
    @FXML
    private void showFixedClassified() {
        ArrayList<Sits> positives = new ArrayList<>();
        ArrayList<Sits> negatives = new ArrayList<>();
        //get the class from the combobox
        String cls = fxComboBoxClassified.getSelectionModel().getSelectedItem();
        boolean positive = (!negativesCheckBoxShow.isSelected());

        int index = (int) fxScrollBar.getValue(); //image index from scroll bar
        //go trough the list of classified patches

        //go trough the classified patches and group into classes
        for (Sits p : fixedClassifiedPatches) {

            if (p.getClasa().equals(cls)
                    && p.isPositive() == true) {
                positives.add(p);
            }
            if (!p.getClasa().equals(cls)
                    && p.isPositive() == positive) {
                negatives.add(p);

            }
        }
        updateSceneFixedClassified(images.get(index), gridSize, positives, negatives);
    }

    /**
     * Sets the tagging color for positive patches
     */
    @FXML
    private void selectPositives() {
        if (fxButtonPositives.isSelected()) {
            usedColor = POSITIVE;
        } else {
            usedColor = DEFAULT;
        }
        //System.out.println("Positives");
    }

    /**
     * Sets the tagging color for negative patches
     */
    @FXML
    private void selectNegatives() {
        if (fxButtonNegatives.isSelected()) {
            usedColor = NEGATIVE;
        } else {
            usedColor = DEFAULT;
        }
        //System.out.println("Negatives");
    }

    /**
     * Sets the grid size which is displayed on each image
     *
     * @throws IOException
     */
    @FXML
    private void setGridSize() throws IOException {
        try {

            int newSize = Integer.parseInt(fxTextFieldGridSize.getText());
            if (newSize != gridSize) {
                gridSize = newSize;
                fxLabelErrorGridSize.setText("");
                fxTextFieldGridSize.getStyleClass().remove("errorTextField");

                int index = (int) fxScrollBar.getValue(); // get the image index                
                //reset all adnotations to null
                initializeAdnotations();
                //classifier = new Classifier();
                //draw the scene with the set parameters
                updateScene(images.get(index), gridSize, null, null);
                yRatio = (double) gridSize / buffImages.get(index).getHeight();
                xRatio = (double) gridSize / buffImages.get(index).getWidth();
            }
        } catch (NumberFormatException e) {
            //show error message
            fxTextFieldGridSize.getStyleClass().add("errorTextField");
            fxLabelErrorGridSize.setText("Insert valid number");
        }
    }

    /**
     *
     * @param collection
     * @return all the patches contained in a certain collection
     */
    private ArrayList<Sits> getTrainingPatches(
            ObservableMap<Integer, ObservableMap<String, ObservableList<Sits>>> collection) {
        ArrayList<Sits> allPatches = new ArrayList<>();
        for (Integer i : collection.keySet()) {
            for (String st : collection.get(i).keySet()) {
                allPatches.addAll(collection.get(i).get(st));
                //System.out.println("Added patches from class: " + st + " #" + collection.get(i).get(st).size());
            }
        }
        return allPatches;
    }

    /**
     * Training function Runs whent the train button is pressed
     */
    @FXML
    private void train() throws IOException {

        System.out.println("Training started!");
        //clears the classifiedPatches list
        startTrain = false;
        System.out.println("Classified list size: " + classifiedPatches.size());

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/SVMScene.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        // Create the dialog Stage.
        Stage stage = new Stage();
        stage.setTitle("SVM settings");
        stage.initModality(Modality.WINDOW_MODAL);

        Scene scene = new Scene(page);
        stage.setScene(scene);
        scene.getStylesheets().add("/styles/Styles.css");

        // Set patches into the controller.
        SVMSceneController controller = loader.getController();
        controller.setTrainingPatchesList(trainingSamples);
        controller.setClassifier(classifier);

        // Show the dialog and wait until the user closes it
        stage.showAndWait();

        if (!startTrain) {
        } else {
            reiteration++;
            fxLabelIterations.setText("Reiteration " + reiteration);
            if (classifiedPatches.size() == 0) {
                getAllPatches();
            }

            classifiedClasses.clear();

            Random rand = new Random();
            /*
             for (Patch patch : trainingSamples) {
             classifiedPatches.remove(patch);
             }
             */
            boolean oneTimeValue = true;
            long fullStart = System.nanoTime();
            //goes trough all the images and gets all gridSize patches from them
            for (Sits p : classifiedPatches) {
                if (p.getRelevance() > threshold && threshold != 0) {
                    fixedClassifiedPatches.add(p);
                } else if (!trainingSamples.contains(p)) {
                    long start = System.nanoTime();

                    String label = classifier.predictLabel(p);
                    if (label.startsWith("false")) {
                        p.setClasa(label.substring(5));
                        p.setPositive(false);
                    } else {
                        p.setClasa(label);
                        p.setPositive(true);
                    }
                    //sets relevance of a given patch
                    p.setRelevance(classifier.getDistance2(p));
                    //p.setRelevance(rand.nextDouble());
                    if (oneTimeValue) {
                        System.out.println("Classification time for one patch in ns: "
                                + (System.nanoTime() - start)
                        );
                        oneTimeValue = false;
                    }
                    if (!classifiedClasses.contains(p.getClasa())) {
                        classifiedClasses.add(p.getClasa());
                    }
                } else {
                    Sits patch = trainingSamples.get(trainingSamples.indexOf(p));
                    p.setClasa(patch.getClasa());
                    p.setPositive(patch.isPositive());
                    p.setRelevance(0);
                }

            }

            System.out.println("Total classification time in ns: "
                    + (System.nanoTime() - fullStart)
            );
            //remove patches from classifiedPatches wich are found in fixedClassifiedPatches
            for (Sits p : fixedClassifiedPatches) {
                classifiedPatches.remove(p);
            }

            //set first element of classifiedClass as the selected element in the comboBox
            fxComboBoxMostRelevantTop.getSelectionModel().selectFirst();
            fillTopTable();//populate the table

            //set the second element of classifiedClass as the selected element in the comboBox
            fxComboBoxMostRelevantBottom.getSelectionModel().select(1);
            fillBottomTable();

            if (autoSave) {
                saveResults();
            }

            System.out.println("Above treshold: " + fixedClassifiedPatches.size());
            System.out.println("Under treshold: " + classifiedPatches.size());
            //print in command line
            //printAllClases();

        }

    }

    @FXML
    private void classifyUsingLoadedModel() throws IOException {
        if (classifiedPatches.size() == 0) {
            getAllPatches();
        }

        classifiedClasses.clear();
        //goes trough all the images and gets all gridSize patches from them
        for (Sits p : classifiedPatches) {
            String label = classifier.predictLabel(p);
            if (label.startsWith("false")) {
                p.setClasa(label.substring(5));
                p.setPositive(false);
            } else {
                p.setClasa(label);
                p.setPositive(true);
            }
            if (!classifiedClasses.contains(p.getClasa())) {
                classifiedClasses.add(p.getClasa());
            }

            p.setRelevance(classifier.getDistance(p));

        }

        //set first element of classifiedClass as the selected element in the comboBox
        fxComboBoxMostRelevantTop.getSelectionModel().selectFirst();
        fillTopTable();//populate the table

        //set the second element of classifiedClass as the selected element in the comboBox
        fxComboBoxMostRelevantBottom.getSelectionModel().selectFirst();
        fillBottomTable();
    }

    @FXML
    private void showTableSelection() {
        int index = (int) fxScrollBar.getValue();
        ArrayList<Sits> positiveSamples = new ArrayList<>();
        ArrayList<Sits> negativeSamples = new ArrayList<>();
        if (fxTableClassificationResultsTop.isFocused()) {
            fxTableClassificationResultsBottom.getSelectionModel().clearSelection();
            ObservableList<Sits> toShowTop = fxTableClassificationResultsTop.getSelectionModel().getSelectedItems();
            if (!toShowTop.isEmpty()) {
                if (negativesCheckBoxTop.isSelected()) {
                    for (Sits p : toShowTop) {
                        negativeSamples.add(p);
                    }

                    drawScene(images.get((int) fxScrollBar.getValue()), gridSize, positiveSamples, negativeSamples);
                } else {
                    for (Sits p : toShowTop) {
                        positiveSamples.add(p);
                    }

                    drawScene(images.get((int) fxScrollBar.getValue()), gridSize, positiveSamples, negativeSamples);
                }
            }
        }

        if (fxTableClassificationResultsBottom.isFocused()) {
            fxTableClassificationResultsTop.getSelectionModel().clearSelection();
            ObservableList<Sits> toShowBottom = fxTableClassificationResultsBottom.getSelectionModel().getSelectedItems();
            if (!toShowBottom.isEmpty()) {
                if (negativesCheckBoxBottom.isSelected()) {
                    for (Sits p : toShowBottom) {
                        negativeSamples.add(p);
                    }

                    drawScene(images.get((int) fxScrollBar.getValue()), gridSize, positiveSamples, negativeSamples);
                } else {
                    for (Sits p : toShowBottom) {
                        positiveSamples.add(p);
                    }

                    drawScene(images.get((int) fxScrollBar.getValue()), gridSize, positiveSamples, negativeSamples);
                }

            }
        }

    }

    private void getAllPatches() {
        long t0 = System.currentTimeMillis();
        classifiedPatches.clear();

        int width = buffImages.get(0).getWidth();
        int height = buffImages.get(0).getHeight();
        //System.out.println("First image of series (width/height): " + width + "/" + height);

        for (int i = 0; i <= width - Math.round(width * xRatio); i += Math.round(width * xRatio)) {
            for (int j = 0; j <= height - Math.round(height * yRatio); j += Math.round(height * yRatio)) {
                Sits s = new Sits();
                ArrayList<Patch> patchList = new ArrayList<>();
                for (BufferedImage buff : buffImages) {
                    patchList.add(new Patch(i * (int) (buff.getWidth() / width), j * (int) (buff.getHeight() / height), (int) Math.round(buff.getWidth() * xRatio), buffImages.indexOf(buff)));
                    //System.out.println("Series image of series (width/height): " + (buff.getWidth() / width) + "/" + (buff.getHeight() / height));
                }
                s.setSits(patchList);
                classifiedPatches.add(s);
            }
        }
        System.out.println("Time needed to create all sits: " + (System.currentTimeMillis() - t0));
    }

    /**
     * Displays all marked patches
     */
    private void printAllClases() {
        System.out.println("######All positives######");
        int total = 0;
        for (Sits s : trainingSamples) {
            if (s.isPositive()) {
                //System.out.println(s);
                total++;
            }
        }
        System.out.println("Total positives : " + total);

        System.out.println("######All negatives######");
        total = 0;
        for (Sits s : trainingSamples) {
            if (!s.isPositive()) {
                //System.out.println(s);
                total++;
            }

        }
        System.out.println("Total negatives : " + total);
        System.out.println("Total number of patches: " + classifiedPatches.size());

    }

    /**
     * Opens a class editor dialogue
     *
     * @throws IOException
     */
    @FXML
    private void showClassesEditor() throws IOException {
        // Load the fxml file and create a new stage for the popup dialog.
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/ClassesEditorScene.fxml"));
        GridPane page = (GridPane) loader.load();

        // Create the dialog Stage.
        Stage stage = new Stage();
        stage.setTitle("Edit clases");
        stage.initModality(Modality.WINDOW_MODAL);

        Scene scene = new Scene(page);
        stage.setScene(scene);

        // Set the class into the controller.
        ClassesEditorSceneController controller = loader.getController();
        controller.fillClasesList(clase);

        // Show the dialog and wait until the user closes it
        stage.showAndWait();
    }

    @FXML
    public void editPatch() throws IOException {

        if (fxTableClassificationResultsBottom.getSelectionModel().getSelectedItems() != null) {

            for (Sits patch : fxTableClassificationResultsBottom.getSelectionModel().getSelectedItems()) {

                // Load the fxml file and create a new stage for the popup dialog.
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/fxml/PatchEditorScene.fxml"));
                AnchorPane page = (AnchorPane) loader.load();

                // Create the dialog Stage.
                Stage stage = new Stage();
                stage.setTitle("Edit patch");
                stage.initModality(Modality.WINDOW_MODAL);

                Scene scene = new Scene(page);
                stage.setScene(scene);

                // Set the class into the controller.
                PatchEditorSceneController controller = loader.getController();
                controller.setPatch(patch);
                controller.setClasses(classifiedClasses);
                controller.setParent(this);

                // Show the dialog and wait until the user closes it
                stage.showAndWait();

                /*
                 if (isModifiedPos) {
                 if (trainingSamples.contains(patch)) {
                 trainingSamples.remove(patch);//removes old patch which has different class and positive fields, the patch objects are not differentiated by these fields
                 trainingSamples.add(patch);//adds new patch
                 }
                 }
                 */
            }
        }
        if (fxTableClassificationResultsTop.getSelectionModel().getSelectedItems() != null) {
            for (Sits patch : fxTableClassificationResultsTop.getSelectionModel().getSelectedItems()) {

                // Load the fxml file and create a new stage for the popup dialog.
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/fxml/PatchEditorScene.fxml"));
                AnchorPane page = (AnchorPane) loader.load();

                // Set the class into the controller.
                PatchEditorSceneController controller = loader.getController();
                controller.setPatch(patch);
                controller.setClasses(classifiedClasses);
                controller.setParent(this);

                // Create the dialog Stage.
                Stage stage = new Stage();
                stage.setTitle("Edit patch");
                stage.initModality(Modality.WINDOW_MODAL);

                Scene scene = new Scene(page);
                stage.setScene(scene);

                // Show the dialog and wait until the user closes it
                stage.showAndWait();
                //update tables
                /*
                 if (isModifiedPos) {
                 if (trainingSamples.contains(patch)) {
                 trainingSamples.remove(patch);//removes old patch which has different class and positive fields, the patch objects are not differentiated by these fields
                 trainingSamples.add(patch);//adds new patch
                 }
                 }
                 */
            }
        }
        //update tables
        fillTopTable();
        fillBottomTable();
    }

    /**
     * Clears all lists with marked patches
     */
    private void initializeAdnotations() {
        trainingSamples.clear();
        updateSelectedPositives();
        //updateSelectedNegatives();
        classifiedPatches.clear();
        topList.clear();
        bottomList.clear();
        classifiedClasses.clear();
        classifier = new Classifier();
        reiteration = -1;
        fxLabelIterations.setText("");

    }

    @FXML
    private void clearAddnotations() {
        //initializeAdnotations();
        trainingSamples.clear();
        updateSelectedPositives();
        //updateSelectedNegatives();
        classifiedPatches.addAll(fixedClassifiedPatches);
        fixedClassifiedPatches.clear();
        topList.clear();
        bottomList.clear();
        classifiedClasses.clear();
        //classifier = new Classifier();
        classifier.setLabels(new ArrayList<>());
        reiteration = -1;
        fxLabelIterations.setText("");
        drawScene(images.get((int) fxScrollBar.getValue()), gridSize, null, null);
    }

    /**
     * Sets the parameters for the visible patches on the current image
     *
     * @param clasa
     * @param imageIndex
     */
    private ArrayList<Sits> getSelectiveData(String clasa, int imageIndex, boolean positive, ArrayList<Sits> data) {
        ArrayList<Sits> returnData = new ArrayList<>();

        for (Sits p : data) {
            if (p.getClasa().equals(clasa) && p.isPositive() == positive) {
                returnData.add(p);
            }
        }
        return returnData;

    }

    /**
     * Add default values to the combobox
     */
    private void fillComboBoxs() {
        clase.clear();//clear the list of classes
        //add some clases to the list
        clase.add("Urban");
        clase.add("Buildings");
        clase.add("Vegetation");
        clase.add("Roads");
        clase.add("Rural");
        clase.add("Industry");
        clase.add("Agriculture");
        clase.add("Forest");
        clase.add("Water");
        clase.add("Other");
        FXCollections.sort(clase);
        //add the list to the adnotation classes combobox
        fxComboBox.setItems(clase);
        fxComboBox.getSelectionModel().selectFirst();
        //add the classified classes list
        fxComboBoxClassified.setItems(classifiedClasses);//comboBox from left which selects the shown markings
        fxComboBoxMostRelevantTop.setItems(classifiedClasses);//comboBox from abpve first table
        fxComboBoxMostRelevantBottom.setItems(classifiedClasses);//comboBox from above bottom table
        fxComboBoxReference.setItems(classifiedClasses);

    }

    /**
     * Opens a dialogue window from which images can be loaded
     *
     * @throws MalformedURLException
     * @throws IOException
     */
    @FXML
    private void loadImages() throws MalformedURLException, IOException {

        List<File> fls = new ArrayList<>();
        //System.out.println(files.size());
        FileChooser fileChooser = new FileChooser();

        if (lastLoad != null) {
            File existDirectory = lastLoad.getParentFile();
            fileChooser.setInitialDirectory(existDirectory);
        }
        fileChooser.setTitle("Load images");
        fls = fileChooser.showOpenMultipleDialog(null);

        if (fls != null) {
            lastLoad = fls.get(0);
            images.clear();
            buffImages.clear();
            files.clear();
            for (File f : fls) {
                files.add(f.getAbsolutePath());
            }

            ///////////////////////////////////////
            //Open RGB image creator 
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/RGBSelector.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage stage = new Stage();
            stage.setTitle("RGB Creator");
            stage.initModality(Modality.WINDOW_MODAL);
            //stage.setMinHeight(263);
            //stage.setMaxHeight(263);
            //stage.setMinWidth(221);

            // Set patches into the controller.
            RGBSelectorController controller = loader.getController();
            controller.setFiles(fls);

            Scene scene = new Scene(page);
            stage.setScene(scene);

            // Show the dialog and wait until the user closes it
            stage.showAndWait();
            /////////////////////////////////////////

            /*
             //System.out.println(files.size());
             if (files != null) {

             fxScrollBar.setMax(files.size() - 1);
             fxScrollBar.setMin(0);
             fxScrollBar.setUnitIncrement(1);

             images.clear();
             initializeAdnotations();
             for (File f : fls) {
             buffImages.add(ImageIO.read(f));
             images.add(displayImage);
             //images.add(SwingFXUtils.toFXImage(buffImages.get(buffImages.size() - 1), null));
             }
             drawScene(images.get(0), gridSize, null, null);
             }
             */
            fxScrollBar.setMax(files.size() - 1);
            fxScrollBar.setMin(0);
            fxScrollBar.setUnitIncrement(1);
            initializeAdnotations();
            drawScene(images.get(0), gridSize, null, null);
        }

    }

    /**
     * Loads training set and predefined svm classifier
     */
    @FXML
    public void loadResults() {
        //Dialogue window 
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Choose load path");
        if (lastSave != null) {
            File existDirectory = lastSave.getParentFile();
            dirChooser.setInitialDirectory(existDirectory);
        }
        File loadPath = dirChooser.showDialog(null);

        if (loadPath != null) {
            lastSave = loadPath;
            //erase all data(training patches), training results
            initializeAdnotations();
            //load the classifier
            StringBuilder sb = new StringBuilder(loadPath.toString());
            sb.append(File.separator);
            sb.append("classifier.ser");
            try {
                //classifer = null;
                FileInputStream fin = new FileInputStream(sb.toString());
                ObjectInputStream ois = new ObjectInputStream(fin);
                this.classifier = (Classifier) ois.readObject();
                ois.close();
                System.out.println("Classifier loaded!");

            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainSceneController.class
                        .getName()).log(Level.SEVERE, null, ex);

            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(MainSceneController.class
                        .getName()).log(Level.SEVERE, null, ex);

            }

            //go into Training folder
            StringBuilder trSb = new StringBuilder(loadPath.toString());
            trSb.append(File.separator);
            trSb.append("Training");
            File trainDir = new File(trSb.toString());
            String[] directories = trainDir.list();

            /*
            //load training patches
            //System.out.println("Directories found in Training");
            if (directories != null) {
                for (String s : directories) {
                    //System.out.println(s);

                    //if the folder holds negative samples
                    if (s.startsWith("false")) {
                        StringBuilder strB = new StringBuilder();
                        strB.append(trSb);
                        strB.append(File.separator).append(s);
                        File imgList = new File(strB.toString());
                        String[] images = imgList.list();

                        for (String img : images) {
                            try {
                                Sits patch = new Sits();
                                patch.setClasa(s.substring(5));
                                patch.setPositive(false);
                                StringBuilder imageFile = new StringBuilder();
                                imageFile.append(strB);
                                imageFile.append(File.separator).append(img);
                                //System.out.println("Loaded file" + imageFile.toString());
                                patch.setParentID(-1);
                                //get the indexes from the image file name
                                patch.setxIndex(Integer.parseInt(img.substring(0, img.indexOf("_"))));
                                String yIndex = img.substring(img.indexOf("_") + 1);
                                yIndex = yIndex.substring(0, yIndex.indexOf("_"));
                                patch.setyIndex(Integer.parseInt(yIndex));
                                BufferedImage buff = ImageIO.read(new File(imageFile.toString()));
                                patch.setSize(buff.getHeight());
                                trainingSamples.add(patch);

                            } catch (IOException ex) {
                                Logger.getLogger(MainSceneController.class
                                        .getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                    } else {
                        StringBuilder strB = new StringBuilder();
                        strB.append(trSb);
                        strB.append(File.separator).append(s);
                        File imgList = new File(strB.toString());
                        String[] images = imgList.list();

                        for (String img : images) {
                            try {
                                Sits patch = new Sits();
                                patch.setClasa(s);
                                patch.setPositive(true);
                                StringBuilder imageFile = new StringBuilder();
                                imageFile.append(strB);
                                imageFile.append(File.separator).append(img);

                                patch.setParentID(-1);
                                //get the indexes from the image file name
                                patch.setxIndex(Integer.parseInt(img.substring(0, img.indexOf("_"))));
                                String yIndex = img.substring(img.indexOf("_") + 1);
                                yIndex = yIndex.substring(0, yIndex.indexOf("_"));
                                patch.setyIndex(Integer.parseInt(yIndex));
                                BufferedImage buff = ImageIO.read(new File(imageFile.toString()));
                                patch.setSize(buff.getHeight());
                                trainingSamples.add(patch);

                            } catch (IOException ex) {
                                Logger.getLogger(MainSceneController.class
                                        .getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    }

                }
            }
            updateSelectedPositives();
            updateSelectedNegatives();\
             */
            System.out.println("###Loaded data###");
            System.out.println("Total number of samples: " + trainingSamples.size());
        }
    }

    /**
     * Saves results of active learning Saves the training patches, the model
     * used to make the classification Saves the classified patches into folders
     *
     * @throws java.io.NotSerializableException
     */
    @FXML
    public void saveResults() throws NotSerializableException {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Choose save path");
        File savePath;
        if (!autoSave) {
            if (lastSave != null) {
                File existDirectory = lastSave.getAbsoluteFile();
                dirChooser.setInitialDirectory(existDirectory);
            }
            savePath = dirChooser.showDialog(null);
        } else if (lastSave != null) {
            savePath = lastSave;
        } else {
            savePath = dirChooser.showDialog(null);
        }

        if (savePath != null) {
            lastSave = savePath;
            //custom name for session
            StringBuilder session = new StringBuilder();
            session.append(classifier.getFeature()).append("_");
            if (classifier.isNormalize()) {
                session.append("Norm").append("_");
            } else {
                session.append("notNorm").append("_");
            }
            session.append(classifier.getKernelTypes().get(classifier.getKernelType())).append("_");
            session.append(classifier.getLabels().get(0)).append("_");
            session.append("it" + reiteration);
            //root directory
            StringBuilder customPath = new StringBuilder(savePath.toString());
            customPath.append(File.separator).append(session);

            /*
            if (saveClassificationResults) {
                //save classified patches        
                for (Sits p : classifiedPatches) {
                    StringBuilder sb = new StringBuilder(customPath);
                    sb.append(File.separator).append("Results").append(File.separator);
                    if (p.isPositive()) {
                        sb.append(p.getClasa());
                    } else {
                        sb.append(p.isPositive()).append(p.getClasa());
                    }
                    File saveDir = new File(sb.toString());
                    if (!saveDir.exists()) {
                        saveDir.mkdirs();
                    }
                    File inF = new File(files.get(p.getParentID()) + "");
                    sb.append(File.separator).append((int) p.getxIndex()).append("_").append((int) p.getyIndex()).append("_").append(inF.getName());
                    try {
                        ImageIO.write(
                                buffImages.get(p.getParentID()).getSubimage(
                                p.getxIndex(),
                                p.getyIndex(),
                                p.getSize(),
                                p.getSize()),
                                sb.substring(sb.lastIndexOf(".") + 1), new File(sb.toString())
                        );
                    } catch (IOException ex) {
                        System.out.println("Error saving image!");
                    }
                }
                //save classified patches        
                for (Sits p : fixedClassifiedPatches) {
                    StringBuilder sb = new StringBuilder(customPath);
                    sb.append(File.separator).append("Results").append(File.separator);
                    if (p.isPositive()) {
                        sb.append(p.getClasa());
                    } else {
                        sb.append(p.isPositive()).append(p.getClasa());
                    }
                    File saveDir = new File(sb.toString());
                    if (!saveDir.exists()) {
                        saveDir.mkdirs();
                    }
                    File inF = new File(files.get(p.getParentID()) + "");
                    sb.append(File.separator).append((int) p.getxIndex()).append("_").append((int) p.getyIndex()).append("_").append(inF.getName());
                    try {
                        ImageIO.write(
                                buffImages.get(p.getParentID()).getSubimage(
                                p.getxIndex(),
                                p.getyIndex(),
                                p.getSize(),
                                p.getSize()),
                                sb.substring(sb.lastIndexOf(".") + 1), new File(sb.toString())
                        );
                    } catch (IOException ex) {
                        System.out.println("Error saving image!");
                    }
                }
            }
            

            if (saveTrainingSamples) {
                //save training samples
                for (Sits p : trainingSamples) {
                    StringBuilder sb = new StringBuilder(customPath);
                    sb.append(File.separator).append("Training").append(File.separator);
                    if (p.isPositive()) {
                        sb.append(p.getClasa());
                    } else {
                        sb.append(p.isPositive()).append(p.getClasa());
                    }
                    File saveDir = new File(sb.toString());
                    if (!saveDir.exists()) {
                        saveDir.mkdirs();
                    }
                    File inF = new File(files.get(p.getParentID()) + "");
                    sb.append(File.separator).append((int) p.getxIndex()).append("_").append((int) p.getyIndex()).append("_").append(inF.getName());
                    try {
                        ImageIO.write(
                                buffImages.get(p.getParentID()).getSubimage(
                                p.getxIndex(),
                                p.getyIndex(),
                                p.getSize(),
                                p.getSize()),
                                sb.substring(sb.lastIndexOf(".") + 1), new File(sb.toString())
                        );
                    } catch (IOException ex) {
                        System.out.println("Error saving image!");
                    }
                }
            }
            
            if (saveFeatures) {
                //save features as txt file
                try {
                    classifier.printFeatures(new File(customPath.toString()));
                } catch (IOException ex) {
                    Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
             */
            if (saveClassifier) {
                //save the classifier
                StringBuilder classifierPath = new StringBuilder(customPath);
                classifierPath.append(File.separator);
                classifierPath.append("classifier.ser");
                File saveDir = new File(customPath.toString());
                if (!saveDir.exists()) {
                    saveDir.mkdirs();
                }
                try {
                    FileOutputStream fout = new FileOutputStream(classifierPath.toString());
                    ObjectOutputStream oos = new ObjectOutputStream(fout);
                    oos.writeObject(classifier);
                    oos.close();

                    System.out.println("Done saving classifier");

                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MainSceneController.class
                            .getName()).log(Level.SEVERE, null, ex);

                } catch (IOException ex) {
                    Logger.getLogger(MainSceneController.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (saveSettingsFile) {
                //save the setting for the classifier
                StringBuilder settingsDoc = new StringBuilder(customPath);
                settingsDoc.append(File.separator);
                settingsDoc.append("settings.xml");

                SVMSettings svmSettings = new SVMSettings();
                svmSettings.setC("" + classifier.getC());
                svmSettings.setCache_size("" + classifier.getCache());
                svmSettings.setCoef0("" + classifier.getCoef());
                svmSettings.setEps("" + classifier.getEps());
                svmSettings.setGamma("" + classifier.getGamma());
                svmSettings.setKernel_type("" + svmSettings.getKernelTypes().get(classifier.getKernelType()));
                svmSettings.setNr_weight("" + classifier.getNoWeights());
                svmSettings.setNu("" + classifier.getNu());
                svmSettings.setP("" + classifier.getNu());
                svmSettings.setPolDegree("" + classifier.getDegree());
                svmSettings.setProbability("" + classifier.getProbability());
                svmSettings.setShrinking("" + classifier.getShrinking());
                svmSettings.setSvm_type("" + svmSettings.getSvmTypes().get(classifier.getSVMtype()));
                svmSettings.setWeight("" + classifier.getNoWeights());
                svmSettings.setNormalized(classifier.isNormalize());

                Settings settings = new Settings();
                settings.setFeatureUsed("" + classifier.getFeature());
                settings.setPatchSize("" + gridSize);
                settings.setLabels(classifier.getLabels());
                settings.setReiterations(reiteration);
                settings.setSvmSettings(svmSettings);

                try {
                    File saveDir = new File(customPath.toString());
                    if (!saveDir.exists()) {
                        saveDir.mkdirs();
                    }
                    XMLProcessor.init();
                    XMLProcessor.dataToXML(settings, settingsDoc.toString());
                } catch (IOException ex) {
                    Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (saveMask) {
                //save class mask for each image

                for (String label : classifier.getLabels()) {
                    StringBuilder imagePath = new StringBuilder(customPath);
                    StringBuilder interm = new StringBuilder(session);
                    interm.replace(session.substring(0, session.lastIndexOf("_")).lastIndexOf("_") + 1, session.lastIndexOf("_"), label);
                    imagePath.append(File.separator).append(interm).append("_mask.png");
                    ArrayList<Patch> mask = new ArrayList<>();
                    for (Sits s : classifiedPatches) {
                        if (s.getClasa().equals(label) && s.isPositive()) {
                            for (Patch p : s.getSits()) {
                                mask.add(p);
                            }
                        }
                    }
                    for (Sits s : fixedClassifiedPatches) {
                        if (s.getClasa().equals(label) && s.isPositive()) {
                            for (Patch p : s.getSits()) {
                                mask.add(p);
                            }
                        }
                    }

                    ImageProcessor.saveClassMask(
                            mask,
                            buffImages.get(0),
                            imagePath.toString()
                    );

                }

            }

        }

    }

    @FXML
    private void closeApplication() {
        Stage stage = (Stage) fxMainBorderPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void editResultsSaveSettings() throws IOException {
        // Load the fxml file and create a new stage for the popup dialog.
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/SaveResultsSettingsScene.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        // Create the dialog Stage.
        Stage stage = new Stage();
        stage.setTitle("Saving settings...");
        stage.initModality(Modality.WINDOW_MODAL);

        Scene scene = new Scene(page);
        stage.setScene(scene);

        // Set the class into the controller.
        SaveResultsSettingsSceneController controller = loader.getController();
        controller.setParent(this);

        // Show the dialog and wait until the user closes it
        stage.showAndWait();
    }

    /**
     * Removes a negative samples
     *
     * @param x
     * @param y
     */
    public void removeNegativeSample(int x, int y) {
        int index = (int) fxScrollBar.getValue();
        //String clasa = fxComboBox.getSelectionModel().getSelectedItem();
        int width = buffImages.get(index).getWidth();
        int height = buffImages.get(index).getHeight();

        Sits s = new Sits();
        ArrayList<Patch> patchList = new ArrayList<>();
        for (BufferedImage buff : buffImages) {
            patchList.add(new Patch(x * buff.getWidth() / width, y * buff.getHeight() / height, (int) Math.round(buff.getWidth() * xRatio), buffImages.indexOf(buff)));

        }
        s.setSits(patchList);

        if (trainingSamples.contains(s)) {
            trainingSamples.remove(s);
        }
        //updateSelectedNegatives();
    }

    /**
     * Removes a positive sample
     *
     * @param x
     * @param y
     */
    public void removePositiveSample(int x, int y) {
        int index = (int) fxScrollBar.getValue();
        //String clasa = fxComboBox.getSelectionModel().getSelectedItem();
        int width = buffImages.get(index).getWidth();
        int height = buffImages.get(index).getHeight();

        Sits s = new Sits();
        ArrayList<Patch> patchList = new ArrayList<>();
        for (BufferedImage buff : buffImages) {
            patchList.add(new Patch(x * buff.getWidth() / width, y * buff.getHeight() / height, (int) Math.round(buff.getWidth() * xRatio), buffImages.indexOf(buff)));

        }
        s.setSits(patchList);

        if (trainingSamples.contains(s)) {
            trainingSamples.remove(s);
        }
        updateSelectedPositives();
    }

    /**
     * Adds a negative sample
     *
     * @param x
     * @param y
     * @return
     */
    public boolean addNegativeSample(int x, int y) {
        int index = (int) fxScrollBar.getValue();
        String clasa = fxComboBox.getSelectionModel().getSelectedItem();
        int width = buffImages.get(index).getWidth();
        int height = buffImages.get(index).getHeight();

        Sits s = new Sits();
        ArrayList<Patch> patchList = new ArrayList<>();
        for (BufferedImage buff : buffImages) {
            patchList.add(new Patch(x * buff.getWidth() / width, y * buff.getHeight() / height, (int) Math.round(buff.getWidth() * xRatio), buffImages.indexOf(buff)));

        }
        s.setSits(patchList);
        s.setClasa(clasa);
        s.setPositive(false);
        addTrainingSample(s);
        return true;

    }

    /**
     * Adds a positive sample
     *
     * @param x
     * @param y
     * @return
     */
    public boolean addPositiveSample(int x, int y) {
        int index = (int) fxScrollBar.getValue();
        String clasa = fxComboBox.getSelectionModel().getSelectedItem();
        int width = buffImages.get(index).getWidth();
        int height = buffImages.get(index).getHeight();

        Sits s = new Sits();
        ArrayList<Patch> patchList = new ArrayList<>();
        for (BufferedImage buff : buffImages) {
            patchList.add(new Patch(x * buff.getWidth() / width, y * buff.getHeight() / height, (int) Math.round(buff.getWidth() * xRatio), buffImages.indexOf(buff)));

        }
        s.setSits(patchList);
        s.setClasa(clasa);
        s.setPositive(true);
        //System.out.println(s);
        addTrainingSample(s);
        return true;
    }

    public void addTrainingSample(Sits p) {
        if (trainingSamples.contains(p)) {
            trainingSamples.remove(p);
        }
        trainingSamples.add(p);
        updateSelectedPositives();
        //updateSelectedNegatives();

    }

    /**
     * Updates the size of the grid which is displayed on images
     *
     * @throws IOException
     */
    @FXML
    private void updateGrid() throws IOException {
        int index = (int) fxScrollBar.getValue(); //image index from scroll bar
        String clasa = fxComboBox.getSelectionModel().getSelectedItem();
        fxPositiveSamplesLabel.setText("Positive samples for " + clasa);
        updateSelectedPositives();
        updateScene(images.get(index), gridSize, getSelectiveData(clasa, index, true, trainingSamples), getSelectiveData(clasa, index, false, trainingSamples));

    }

    //changes one node from the display panel
    private void updateNode(Sits p) {

    }

    /**
     * Displays the scene with the grid and marked patches
     *
     * @param im
     * @param offset
     * @param positives
     * @param negatives
     */
    @FXML
    private void drawScene(Image im, double offset, ArrayList<Sits> positives, ArrayList<Sits> negatives) {
        fxImagePane.getChildren().removeAll(fxImagePane.getChildren());
        Group group = new Group();

        double width = im.getWidth();
        double height = im.getHeight();
        // create canvas
        myScale = new SimpleDoubleProperty(fxMainBorderPane.getWidth() * 0.5 / width);
        PannableCanvas canvas = new PannableCanvas(fxImagePane.getWidth(), fxImagePane.getHeight(), myScale);

        canvas.scaleXProperty().bind(myScale);
        canvas.scaleYProperty().bind(myScale);
        // create sample nodes which can be clicked
        NodeGestures nodeGestures = new NodeGestures(canvas);
        nodeGestures.setController(this);
        ArrayList<Rectangle> objectList = new ArrayList<>();
        for (int i = 0; i <= width - offset; i += offset) {
            for (int j = 0; j <= height - offset; j += offset) {
                Rectangle rect = new Rectangle(offset, offset);
                rect.setTranslateX(i);
                rect.setTranslateY(j);
                //rect1.setStroke(Color.WHITESMOKE);
                rect.setFill(Color.WHITESMOKE.deriveColor(1, 1, 1, 0.05));
                rect.addEventFilter(MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
                //add to the list of rectangles
                objectList.add(rect);
            }
        }
        try {
            //mark positive samples
            for (Sits s : positives) {
                for (Patch p : s.getSits()) {
                    if (p.getParentID() == images.indexOf(im)) {
                        Rectangle rect = new Rectangle(p.getSize(), p.getSize());
                        rect.setTranslateX(p.getxIndex());
                        rect.setTranslateY(p.getyIndex());
                        rect.setFill(MainSceneController.POSITIVE);
                        rect.addEventFilter(MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
                        objectList.add(rect);
                    }
                }
            }
        } catch (NullPointerException e) {
        }

        try {
            //mark negative samples
            for (Sits s : negatives) {
                for (Patch p : s.getSits()) {
                    if (p.getParentID() == images.indexOf(im)) {
                        Rectangle rect = new Rectangle(p.getSize(), p.getSize());
                        rect.setTranslateX(p.getxIndex());
                        rect.setTranslateY(p.getyIndex());
                        rect.setFill(MainSceneController.NEGATIVE);
                        rect.addEventFilter(MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
                        objectList.add(rect);
                    }
                }

            }
        } catch (NullPointerException e) {
        }
        canvas.getChildren().addAll(objectList);
        group.getChildren().add(canvas);

        fxImagePane.getChildren().add(group);

        SceneGestures sceneGestures = new SceneGestures(canvas);
        fxImagePane.addEventFilter(MouseEvent.MOUSE_PRESSED, sceneGestures.getOnMousePressedEventHandler());
        fxImagePane.addEventFilter(MouseEvent.MOUSE_DRAGGED, sceneGestures.getOnMouseDraggedEventHandler());
        fxImagePane.addEventFilter(ScrollEvent.ANY, sceneGestures.getOnScrollEventHandler());

        canvas.addGrid(im, offset);

    }

    /**
     * Displays the scene with the grid and marked patches
     *
     * @param im
     * @param offset
     * @param positives
     * @param negatives
     */
    @FXML
    private void updateScene(Image im, double offset, ArrayList<Sits> positives, ArrayList<Sits> negatives) {
        //fxImagePane.getChildren().removeAll(fxImagePane.getChildren());
        Group group = (Group) fxImagePane.getChildren().get(0);

        double width = im.getWidth();
        double height = im.getHeight();
        // create canvas
        //myScale = fxImagePane.getChildren().;
        PannableCanvas canvas = (PannableCanvas) group.getChildren().get(0);

        //canvas.scaleXProperty().bind(myScale);
        //canvas.scaleYProperty().bind(myScale);
        // create sample nodes which can be clicked
        NodeGestures nodeGestures = new NodeGestures(canvas);
        nodeGestures.setController(this);
        ArrayList<Rectangle> objectList = new ArrayList<>();
        for (int i = 0; i <= width - offset; i += offset) {
            for (int j = 0; j <= height - offset; j += offset) {
                Rectangle rect = new Rectangle(offset, offset);
                rect.setTranslateX(i);
                rect.setTranslateY(j);
                //rect1.setStroke(Color.WHITESMOKE);
                rect.setFill(Color.WHITESMOKE.deriveColor(1, 1, 1, 0.05));
                rect.addEventFilter(MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
                //add to the list of rectangles
                objectList.add(rect);
            }
        }
        try {
            //mark positive samples
            for (Sits s : positives) {
                for (Patch p : s.getSits()) {
                    if (p.getParentID() == images.indexOf(im)) {
                        Rectangle rect = new Rectangle(p.getSize(), p.getSize());
                        rect.setTranslateX(p.getxIndex());
                        rect.setTranslateY(p.getyIndex());
                        rect.setFill(MainSceneController.POSITIVE);
                        rect.addEventFilter(MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
                        objectList.add(rect);
                    }
                }
            }
        } catch (NullPointerException e) {
        }

        try {
            //mark negative samples
            for (Sits s : negatives) {
                for (Patch p : s.getSits()) {
                    if (p.getParentID() == images.indexOf(im)) {
                        Rectangle rect = new Rectangle(p.getSize(), p.getSize());
                        rect.setTranslateX(p.getxIndex());
                        rect.setTranslateY(p.getyIndex());
                        rect.setFill(MainSceneController.NEGATIVE);
                        rect.addEventFilter(MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
                        objectList.add(rect);
                    }
                }
            }
        } catch (NullPointerException e) {
        }
        canvas.getChildren().removeAll(canvas.getChildren());
        canvas.getChildren().addAll(objectList);
        group.getChildren().removeAll(group.getChildren());
        group.getChildren().add(canvas);
        fxImagePane.getChildren().removeAll(fxImagePane.getChildren());
        fxImagePane.getChildren().add(group);

        //SceneGestures sceneGestures = new SceneGestures(canvas);
        //fxImagePane.addEventFilter(MouseEvent.MOUSE_PRESSED, sceneGestures.getOnMousePressedEventHandler());
        //fxImagePane.addEventFilter(MouseEvent.MOUSE_DRAGGED, sceneGestures.getOnMouseDraggedEventHandler());
        //fxImagePane.addEventFilter(ScrollEvent.ANY, sceneGestures.getOnScrollEventHandler());
        canvas.addGrid(im, offset);
    }

    @FXML
    private void updateSceneToBeClassified(Image im, double offset, ArrayList<Sits> positives, ArrayList<Sits> negatives) {
        //fxImagePane.getChildren().removeAll(fxImagePane.getChildren());
        Group group = (Group) fxImagePane.getChildren().get(0);

        double width = im.getWidth();
        double height = im.getHeight();
        // create canvas
        //myScale = fxImagePane.getChildren().;
        PannableCanvas canvas = (PannableCanvas) group.getChildren().get(0);

        //canvas.scaleXProperty().bind(myScale);
        //canvas.scaleYProperty().bind(myScale);
        // create sample nodes which can be clicked
        NodeGestures nodeGestures = new NodeGestures(canvas);
        nodeGestures.setController(this);
        ArrayList<Rectangle> objectList = new ArrayList<>();
        try {
            //mark positive samples
            for (Sits s : positives) {
                for (Patch p : s.getSits()) {
                    if (p.getParentID() == images.indexOf(im)) {
                        Rectangle rect = new Rectangle(p.getSize(), p.getSize());
                        rect.setTranslateX(p.getxIndex());
                        rect.setTranslateY(p.getyIndex());
                        //canvas.getChildren().remove(rect);
                        rect.setFill(MainSceneController.POSITIVE);
                        rect.addEventFilter(MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
                        objectList.add(rect);
                    }
                }

            }
        } catch (NullPointerException e) {
        }

        try {
            //mark negative samples
            for (Sits s : negatives) {
                for (Patch p : s.getSits()) {
                    if (p.getParentID() == images.indexOf(im)) {
                        Rectangle rect = new Rectangle(p.getSize(), p.getSize());
                        rect.setTranslateX(p.getxIndex());
                        rect.setTranslateY(p.getyIndex());
                        //canvas.getChildren().remove(rect);
                        rect.setFill(MainSceneController.NEGATIVE);
                        rect.addEventFilter(MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
                        objectList.add(rect);
                    }
                }
            }
        } catch (NullPointerException e) {
        }

        canvas.getChildren().removeAll(canvas.getChildren());
        canvas.getChildren().addAll(objectList);
        group.getChildren().removeAll(group.getChildren());
        group.getChildren().add(canvas);
        fxImagePane.getChildren().removeAll(fxImagePane.getChildren());
        fxImagePane.getChildren().add(group);

        //SceneGestures sceneGestures = new SceneGestures(canvas);
        //fxImagePane.addEventFilter(MouseEvent.MOUSE_PRESSED, sceneGestures.getOnMousePressedEventHandler());
        //fxImagePane.addEventFilter(MouseEvent.MOUSE_DRAGGED, sceneGestures.getOnMouseDraggedEventHandler());
        //fxImagePane.addEventFilter(ScrollEvent.ANY, sceneGestures.getOnScrollEventHandler());
        canvas.addGrid(im, offset);
    }

    @FXML
    private void updateSceneFixedClassified(Image im, double offset, ArrayList<Sits> positives, ArrayList<Sits> negatives) {
        //fxImagePane.getChildren().removeAll(fxImagePane.getChildren());
        Group group = (Group) fxImagePane.getChildren().get(0);

        double width = im.getWidth();
        double height = im.getHeight();
        // create canvas
        //myScale = fxImagePane.getChildren().;
        PannableCanvas canvas = (PannableCanvas) group.getChildren().get(0);

        //canvas.scaleXProperty().bind(myScale);
        //canvas.scaleYProperty().bind(myScale);
        // create sample nodes which can be clicked
        NodeGestures nodeGestures = new NodeGestures(canvas);
        nodeGestures.setController(this);
        ArrayList<Rectangle> objectList = new ArrayList<>();
        try {
            //mark positive samples
            for (Sits s : positives) {
                for (Patch p : s.getSits()) {
                    if (p.getParentID() == images.indexOf(im)) {
                        Rectangle rect = new Rectangle(p.getSize(), p.getSize());
                        rect.setTranslateX(p.getxIndex());
                        rect.setTranslateY(p.getyIndex());
                        rect.setFill(MainSceneController.DEACT_POSITIVE);
                        rect.addEventFilter(MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
                        objectList.add(rect);
                    }
                }
            }
        } catch (NullPointerException e) {
        }

        try {
            //mark negative samples
            for (Sits s : negatives) {
                for (Patch p : s.getSits()) {
                    if (p.getParentID() == images.indexOf(im)) {
                        Rectangle rect = new Rectangle(p.getSize(), p.getSize());
                        rect.setTranslateX(p.getxIndex());
                        rect.setTranslateY(p.getyIndex());
                        rect.setFill(MainSceneController.DEACT_NEGATIVE);
                        rect.addEventFilter(MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
                        objectList.add(rect);
                    }
                }
            }
        } catch (NullPointerException e) {
        }

        //canvas.getChildren().removeAll(canvas.getChildren());
        canvas.getChildren().addAll(objectList);
        group.getChildren().removeAll(group.getChildren());
        group.getChildren().add(canvas);
        fxImagePane.getChildren().removeAll(fxImagePane.getChildren());
        fxImagePane.getChildren().add(group);

        //SceneGestures sceneGestures = new SceneGestures(canvas);
        //fxImagePane.addEventFilter(MouseEvent.MOUSE_PRESSED, sceneGestures.getOnMousePressedEventHandler());
        //fxImagePane.addEventFilter(MouseEvent.MOUSE_DRAGGED, sceneGestures.getOnMouseDraggedEventHandler());
        //fxImagePane.addEventFilter(ScrollEvent.ANY, sceneGestures.getOnScrollEventHandler());
        canvas.addGrid(im, offset);
    }
}

/**
 * Custom pane for displaying the image
 *
 * @author Alex
 */
class PannableCanvas extends Pane {

    DoubleProperty myScale;

    public PannableCanvas(double width, double height, DoubleProperty myScale) {
        this.myScale = myScale;
        setPrefSize(width, height);
        //setStyle("-fx-background-color: lightgrey; -fx-border-color: blue;");

        // add scale transform
        scaleXProperty().bind(myScale);
        scaleYProperty().bind(myScale);
    }

    /**
     * Add a grid to the canvas, send it to back
     */
    public void addGrid(Image im, double offset) {

        //double w = getBoundsInLocal().getWidth();
        //double h = getBoundsInLocal().getHeight();
        double w = im.getWidth();
        double h = im.getHeight();

        // add grid
        Canvas grid = new Canvas(w, h);

        // don't catch mouse events
        grid.setMouseTransparent(true);

        GraphicsContext gc = grid.getGraphicsContext2D();
        gc.drawImage(im, 0, 0);
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);

        for (double i = offset; i < w; i += offset) {
            gc.strokeLine(i, 0, i, h);
            gc.strokeLine(0, i, w, i);
        }

        getChildren().add(grid);

        grid.toBack();
    }

    public double getScale() {
        return myScale.get();
    }

    public void setScale(double scale) {
        myScale.set(scale);
    }

    public void setPivot(double x, double y) {
        setTranslateX(getTranslateX() - x);
        setTranslateY(getTranslateY() - y);
    }

}

/**
 * Mouse drag context used for scene and nodes.
 */
class DragContext {

    double mouseAnchorX;
    double mouseAnchorY;

    double translateAnchorX;
    double translateAnchorY;

}

/**
 * Listeners for making the nodes draggable via left mouse button. Considers if
 * parent is zoomed.
 */
class NodeGestures {

    PannableCanvas canvas;
    private MainSceneController controller;

    public NodeGestures(PannableCanvas canvas) {
        this.canvas = canvas;

    }

    void setController(MainSceneController controller) {
        this.controller = controller;
    }

    public EventHandler<MouseEvent> getOnMousePressedEventHandler() {
        return onMousePressedEventHandler;
    }

    private EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

        public void handle(MouseEvent event) {

            // left mouse button => selecting area
            if (!event.isPrimaryButtonDown()) {
                return;
            } else {

                Rectangle node = (Rectangle) event.getSource();
                if (node.getFill().equals(MainSceneController.POSITIVE)) {
                    controller.removePositiveSample((int) node.getTranslateX(), (int) node.getTranslateY());
                    node.setFill(MainSceneController.DEFAULT);

                } else if (node.getFill().equals(MainSceneController.DEFAULT)) {
                    controller.addPositiveSample((int) node.getTranslateX(), (int) node.getTranslateY());
                    node.setFill(MainSceneController.POSITIVE);
                }
            }
        }
    };
}

/**
 * Listeners for making the scene's canvas draggable and zoomable
 */
class SceneGestures {

    private static final double MAX_SCALE = 10.0d;
    private static final double MIN_SCALE = .1d;

    private DragContext sceneDragContext = new DragContext();

    PannableCanvas canvas;

    public SceneGestures(PannableCanvas canvas) {
        this.canvas = canvas;
    }

    public EventHandler<MouseEvent> getOnMousePressedEventHandler() {
        return onMousePressedEventHandler;
    }

    public EventHandler<MouseEvent> getOnMouseDraggedEventHandler() {
        return onMouseDraggedEventHandler;
    }

    public EventHandler<ScrollEvent> getOnScrollEventHandler() {
        return onScrollEventHandler;
    }

    private EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

        public void handle(MouseEvent event) {

            // right mouse button => panning
            if (!event.isSecondaryButtonDown()) {
                return;
            }

            sceneDragContext.mouseAnchorX = event.getSceneX();
            sceneDragContext.mouseAnchorY = event.getSceneY();

            sceneDragContext.translateAnchorX = canvas.getTranslateX();
            sceneDragContext.translateAnchorY = canvas.getTranslateY();

        }

    };

    private EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {

            // right mouse button => panning
            if (!event.isSecondaryButtonDown()) {
                return;
            }

            canvas.setTranslateX(sceneDragContext.translateAnchorX + event.getSceneX() - sceneDragContext.mouseAnchorX);
            canvas.setTranslateY(sceneDragContext.translateAnchorY + event.getSceneY() - sceneDragContext.mouseAnchorY);

            event.consume();
        }
    };

    /**
     * Mouse wheel handler: zoom to pivot point
     */
    private EventHandler<ScrollEvent> onScrollEventHandler = new EventHandler<ScrollEvent>() {

        @Override
        public void handle(ScrollEvent event) {

            double delta = 1.2;

            double scale = canvas.getScale(); // currently we only use Y, same value is used for X
            double oldScale = scale;

            if (event.getDeltaY() < 0) {
                scale /= delta;
            } else {
                scale *= delta;
            }

            scale = clamp(scale, MIN_SCALE, MAX_SCALE);

            double f = (scale / oldScale) - 1;

            double dx = (event.getSceneX() - (canvas.getBoundsInParent().getWidth() / 2 + canvas.getBoundsInParent().getMinX()));
            double dy = (event.getSceneY() - (canvas.getBoundsInParent().getHeight() / 2 + canvas.getBoundsInParent().getMinY()));

            canvas.setScale(scale);

            // note: pivot value must be untransformed, i. e. without scaling
            canvas.setPivot(f * dx, f * dy);

            event.consume();

        }

    };

    public static double clamp(double value, double min, double max) {

        if (Double.compare(value, min) < 0) {
            return min;
        }

        if (Double.compare(value, max) > 0) {
            return max;
        }

        return value;
    }
}
