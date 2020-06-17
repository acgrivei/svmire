/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ceost.learn_sits;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.ceost.learn_sits.util.ImageProcessor;

/**
 * FXML Controller class
 *
 * @author alexandru.grivei
 */
public class RGBSelectorController implements Initializable {

    @FXML
    private AnchorPane fxAnchorPaneVisible;
    @FXML
    private Label nBandsLabel;
    @FXML
    private ChoiceBox redChoiceBox;
    @FXML
    private ChoiceBox greenChoiceBox;
    @FXML
    private ChoiceBox blueChoiceBox;
    @FXML
    private ChoiceBox nirChoiceBox;
    @FXML
    private ChoiceBox mirChoiceBox;
    @FXML
    private CheckBox fxCheckBoxDataUsage;
    @FXML
    private ImageView previewImageView;

    List<File> loadedFiles;
    private ArrayList<Raster> loadedRasters = new ArrayList<>();
    private ArrayList<BufferedImage> bImages = new ArrayList<>();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    public void previewRGBMapping() {
        int rBand = redChoiceBox.getSelectionModel().getSelectedIndex();
        int gBand = greenChoiceBox.getSelectionModel().getSelectedIndex();
        int bBand = blueChoiceBox.getSelectionModel().getSelectedIndex();

        previewImageView.setFitHeight(261);
        previewImageView.setFitWidth(300);
        previewImageView.setImage(SwingFXUtils.toFXImage(
                ImageProcessor.getRGBBufferedImage(
                        ImageProcessor.createRGBRaster(loadedRasters.get(0), rBand, gBand, bBand)),
                null
        ));

    }

    @FXML
    public void previewNDVIMapping() {
        int redBand = redChoiceBox.getSelectionModel().getSelectedIndex();
        int nirBand = nirChoiceBox.getSelectionModel().getSelectedIndex();

        previewImageView.setFitHeight(261);
        previewImageView.setFitWidth(300);
        //previewImageView.setImage(SwingFXUtils.toFXImage(new Image()));

    }

    @FXML
    public void previewNDBIMapping() {
        int nirBand = nirChoiceBox.getSelectionModel().getSelectedIndex();
        int mirBand = mirChoiceBox.getSelectionModel().getSelectedIndex();

        previewImageView.setFitHeight(261);
        previewImageView.setFitWidth(300);
        //previewImageView.setImage(SwingFXUtils.toFXImage());

    }

    @FXML
    public void previewSAVIMapping() {
        int redBand = redChoiceBox.getSelectionModel().getSelectedIndex();
        int nirBand = nirChoiceBox.getSelectionModel().getSelectedIndex();

        previewImageView.setFitHeight(261);
        previewImageView.setFitWidth(300);
        //previewImageView.setImage(SwingFXUtils.toFXImage(new Image()));

    }

    @FXML
    public void previewMNDWIMapping() {
        int greenBand = greenChoiceBox.getSelectionModel().getSelectedIndex();
        int mirBand = mirChoiceBox.getSelectionModel().getSelectedIndex();

        previewImageView.setFitHeight(261);
        previewImageView.setFitWidth(300);
        //previewImageView.setImage(SwingFXUtils.toFXImage());

    }

    @FXML
    public void returnSettings() {
        int rBand = redChoiceBox.getSelectionModel().getSelectedIndex();
        int gBand = greenChoiceBox.getSelectionModel().getSelectedIndex();
        int bBand = blueChoiceBox.getSelectionModel().getSelectedIndex();
        int nirBand = nirChoiceBox.getSelectionModel().getSelectedIndex();
        int mirBand = mirChoiceBox.getSelectionModel().getSelectedIndex();
        boolean useMapped = fxCheckBoxDataUsage.isSelected();

        ArrayList<Image> visImages = new ArrayList<>();

        for (Raster r : loadedRasters) {
            visImages.add(
                    SwingFXUtils.toFXImage(
                            ImageProcessor.getRGBBufferedImage(
                                    ImageProcessor.createRGBRaster(r, rBand, gBand, bBand)),
                            null
                    )
            );

        }

        if (useMapped) {
            ArrayList<BufferedImage> usedData = new ArrayList<>();
            int nOfBands = 0;
            if (rBand != -1) {
                nOfBands++;
            }
            if (gBand != -1) {
                nOfBands++;
            }
            if (bBand != -1) {
                nOfBands++;
            }
            if (nirBand != -1) {
                nOfBands++;
            }
            if (mirBand != -1) {
                nOfBands++;
            }
            for (int i = 0; i < bImages.size(); i++) {
                int w = bImages.get(i).getRaster().getWidth();
                int h = bImages.get(i).getRaster().getHeight();
                int dt = bImages.get(i).getRaster().getDataBuffer().getDataType();

                WritableRaster wr = WritableRaster.createBandedRaster(
                        dt,
                        w,
                        h,
                        nOfBands,
                        null
                );
                wr.setSamples(
                        0,
                        0,
                        w,
                        h,
                        0,
                        bImages.get(i).getRaster().getSamples(
                        0,
                        0,
                        w,
                        h,
                        bBand, (float[]) null
                )
                );
                wr.setSamples(
                        0,
                        0,
                        w,
                        h,
                        1,
                        bImages.get(i).getRaster().getSamples(
                        0,
                        0,
                        w,
                        h,
                        gBand, (float[]) null)
                );
                wr.setSamples(
                        0,
                        0,
                        w,
                        h,
                        2,
                        bImages.get(i).getRaster().getSamples(
                        0,
                        0,
                        w,
                        h,
                        rBand, (float[]) null)
                );
                wr.setSamples(
                        0,
                        0,
                        w,
                        h,
                        3,
                        bImages.get(i).getRaster().getSamples(
                        0,
                        0,
                        w,
                        h,
                        nirBand, (float[]) null)
                );
                System.out.println("Number of bands used in data: " + wr.getNumBands());
                
                BufferedImage intermBuff = new BufferedImage(w, h, dt);
                intermBuff.setData(wr);
                bImages.set(i, intermBuff);
            }
        }
        MainSceneController.buffImages = bImages;
        MainSceneController.images = visImages;
        MainSceneController.redBand = rBand;
        MainSceneController.greenBand = gBand;
        MainSceneController.blueBand = bBand;
        MainSceneController.nirBand = nirBand;
        MainSceneController.mirBand = mirBand;

        Stage stage = (Stage) fxAnchorPaneVisible.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void saveNDVIbands() {

    }

    @FXML
    public void saveNEARbands() {

    }

    public void setFiles(List<File> files) {

        for (File f : files) {

            try {
                bImages.add(ImageIO.read(f));
                loadedRasters.add(
                        normalizeRaster(
                                bImages.get(bImages.size() - 1).getData()
                        )
                );
            } catch (IOException e) {
                System.out.println("IO Error!");
            }

        }

        int nBands = loadedRasters.get(0).getNumBands();
        setNofBands(nBands);
        fillChoiceBoxes(nBands);
    }

    private void setNofBands(int b) {
        nBandsLabel.setText(b + "");
    }

    private void fillChoiceBoxes(int nBands) {
        ObservableList<String> values = FXCollections.observableArrayList();
        for (int i = 0; i < nBands; i++) {
            values.add("" + i);
        }
        redChoiceBox.setItems(values);
        greenChoiceBox.setItems(values);
        blueChoiceBox.setItems(values);
        nirChoiceBox.setItems(values);
        mirChoiceBox.setItems(values);
    }

    private Raster normalizeRaster(Raster r) {
        return ImageProcessor.normalize(r);
    }
}
