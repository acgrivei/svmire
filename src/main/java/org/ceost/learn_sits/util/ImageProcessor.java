/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ceost.learn_sits.util;

import de.lmu.ifi.dbs.jfeaturelib.LibProperties;
import de.lmu.ifi.dbs.jfeaturelib.features.FuzzyHistogram;
import de.lmu.ifi.dbs.jfeaturelib.features.Gabor;
import de.lmu.ifi.dbs.jfeaturelib.features.Haralick;
import de.lmu.ifi.dbs.jfeaturelib.features.Histogram;
import de.lmu.ifi.dbs.jfeaturelib.features.SURF;
import de.lmu.ifi.dbs.jfeaturelib.features.Sift;
import de.lmu.ifi.dbs.utilities.Arrays2;
import ij.process.FloatProcessor;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;
import libsvm.svm_node;
import org.apache.commons.lang.ArrayUtils;
import org.ceost.learn_sits.model.Patch;
import org.ceost.learn_sits.model.Sits;

/**
 *
 * @author Alex
 */
public class ImageProcessor {

    public static final Color NEGATIVE = Color.BLACK;
    public static final Color POSITIVE = Color.WHITE;
    public static final int neg = 0x000000;
    public static final int poz = 0xFFFFFF;
    public static List<double[]> bufferedImageToNEND;

    /**
     * Saves BufferedImage contained in Patch p in directory d
     *
     * @param p
     * @param d
     */
    public static void savePatch(BufferedImage img, String d) {
        StringBuilder sb = new StringBuilder(d.toString());
        /*
         File inF = new File(p.getParentID() + "");
        
         sb.append("\\").append((int) p.getxIndex()).append("_").append((int) p.getyIndex()).append("_").append(inF.getName());
         */
        //System.out.println(sb.toString());
        //System.out.println(p.getParentID().substring(p.getParentID().lastIndexOf(".") + 1));
        //File f = new File(sb.toString());
        try {
            ImageIO.write(img, sb.substring(sb.lastIndexOf(".") + 1), new File(d));
        } catch (IOException ex) {
            System.out.println("Image could not be saved");
        }
    }

    public static void saveClassMask(ArrayList<Patch> patches, BufferedImage originalImage, String savePath) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        BufferedImage mask = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (Patch p : patches) {
            for (int w = (int) p.getxIndex(); w < (int) p.getxIndex() + p.getSize(); w++) {
                for (int h = (int) p.getyIndex(); h < (int) p.getyIndex() + p.getSize(); h++) {
                    mask.setRGB(w, h, poz);
                }
            }
        }

        File f = new File(savePath);

        try {
            if (!f.exists()) {
                f.mkdirs();
            }
            ImageIO.write(mask, "png", f);
        } catch (IOException ex) {
            System.out.println("Image could not be saved");
        }
    }

    /**
     * Converts the BufferedImage saved in Patch object to InputStream keeping
     * the image format of the file
     *
     * @param p
     * @return
     * @throws IOException
     */
    public static InputStream bufferedImageToInputStream(BufferedImage img, String imageType) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ImageIO.write(img, imageType, byteOut);
        InputStream stream = new ByteArrayInputStream(byteOut.toByteArray());
        return stream;
    }

    public static int getNofBands(File f) {
        int nBands = 0;
        nBands = getRaster(f).getNumBands();
        return nBands;
    }

    public static Raster getRaster(File f) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(f);
        } catch (Exception e) {
        }
        //System.out.println("Raster extracted");
        return img.getData();
    }

    public static Raster createRGBRaster(Raster source, int redBand, int greenBand, int blueBand) {
        WritableRaster raster = WritableRaster.createBandedRaster(
                source.getDataBuffer().getDataType(),
                source.getWidth(),
                source.getHeight(),
                3,
                null);
        raster.setSamples(
                0,
                0,
                source.getWidth(),
                source.getHeight(),
                0,
                source.getSamples(0, 0, source.getWidth(), source.getHeight(), redBand, (float[]) null)
        );
        raster.setSamples(
                0,
                0,
                source.getWidth(),
                source.getHeight(),
                1,
                source.getSamples(0, 0, source.getWidth(), source.getHeight(), greenBand, (float[]) null)
        );
        raster.setSamples(
                0,
                0,
                source.getWidth(),
                source.getHeight(),
                2,
                source.getSamples(0, 0, source.getWidth(), source.getHeight(), blueBand, (float[]) null)
        );
        //System.out.println("Raster created");
        //System.out.println(raster.getNumBands());
        //System.out.println(raster.getWidth());
        //System.out.println(raster.getHeight());
        return raster;
    }

    public static Raster createNDVIRaster(Raster source, int nirBand, int redBand) {
        WritableRaster raster = WritableRaster.createBandedRaster(
                DataBuffer.TYPE_DOUBLE,
                source.getWidth(),
                source.getHeight(),
                1,
                null);
        for (int w = 0; w < raster.getWidth(); w++) {
            for (int h = 0; h < raster.getHeight(); h++) {
                raster.setSample(w, h, 0, (source.getSample(w, h, nirBand) - source.getSample(w, h, redBand)) / (source.getSample(w, h, nirBand) + source.getSample(w, h, redBand)));
            }
        }
        return raster;
    }

    public static Raster createNDBIRaster(Raster source, int nirBand, int mirBand) {
        WritableRaster raster = WritableRaster.createBandedRaster(
                DataBuffer.TYPE_DOUBLE,
                source.getWidth(),
                source.getHeight(),
                1,
                null);
        for (int w = 0; w < raster.getWidth(); w++) {
            for (int h = 0; h < raster.getHeight(); h++) {
                raster.setSample(w, h, 0, (source.getSample(w, h, mirBand) - source.getSample(w, h, nirBand)) / (source.getSample(w, h, mirBand) + source.getSample(w, h, nirBand)));
            }
        }
        return raster;
    }

    public static Raster createSAVIsRaster(Raster source, int redBand, int nirBand) {
        WritableRaster raster = WritableRaster.createBandedRaster(
                DataBuffer.TYPE_FLOAT,
                source.getWidth(),
                source.getHeight(),
                1,
                null);
        for (int w = 0; w < raster.getWidth(); w++) {
            for (int h = 0; h < raster.getHeight(); h++) {
                raster.setSample(w, h, 0, (source.getSample(w, h, nirBand) - source.getSample(w, h, redBand)) * (1.5) / (source.getSample(w, h, nirBand) + source.getSample(w, h, redBand) + 0.5));
            }
        }
        return raster;
    }

    public static Raster createMNDWIRaster(Raster source, int greenBand, int mirBand) {
        WritableRaster raster = WritableRaster.createBandedRaster(
                DataBuffer.TYPE_FLOAT,
                source.getWidth(),
                source.getHeight(),
                1,
                null);
        for (int w = 0; w < raster.getWidth(); w++) {
            for (int h = 0; h < raster.getHeight(); h++) {
                raster.setSample(w, h, 0, (source.getSample(w, h, greenBand) - source.getSample(w, h, mirBand)) / (source.getSample(w, h, greenBand) + source.getSample(w, h, mirBand)));
            }
        }
        return raster;
    }

    public static BufferedImage getRGBBufferedImage(Raster r) {
        BufferedImage buff = new BufferedImage(r.getWidth(), r.getHeight(), BufferedImage.TYPE_INT_RGB);
        buff.setData(r);
        return buff;

    }

    public static BufferedImage getRGB_NDVI_BufferedImage(Raster r) {
        BufferedImage buff = new BufferedImage(r.getWidth(), r.getHeight(), BufferedImage.TYPE_INT_RGB);
        buff.setData(r);
        return buff;
    }

    public static BufferedImage getRGB_NDBI_BufferedImage(Raster r) {
        BufferedImage buff = new BufferedImage(r.getWidth(), r.getHeight(), BufferedImage.TYPE_INT_RGB);
        buff.setData(r);
        return buff;
    }

    public static BufferedImage getRGB_SAVI_BufferedImage(Raster r) {
        BufferedImage buff = new BufferedImage(r.getWidth(), r.getHeight(), BufferedImage.TYPE_INT_RGB);
        buff.setData(r);
        return buff;
    }

    public static BufferedImage getRGB_MNDWI_BufferedImage(Raster r) {
        BufferedImage buff = new BufferedImage(r.getWidth(), r.getHeight(), BufferedImage.TYPE_INT_RGB);
        buff.setData(r);
        return buff;
    }

    public static Raster normalize(Raster rast) {

        int width = rast.getWidth();
        int height = rast.getHeight();
        int bands = rast.getNumBands();

        WritableRaster raster = WritableRaster.createBandedRaster(
                DataBuffer.TYPE_INT,
                width,
                height,
                bands,
                null);

        double[] means = new double[bands];
        double[] dev = new double[bands];

        //initialize mean and dev
        for (int b = 0; b < bands; b++) {
            means[b] = 0;
            dev[b] = 0.00001;
        }

        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                for (int b = 0; b < bands; b++) {
                    means[b] += rast.getSample(w, h, b);
                }
            }
        }

        //gets vector of mean values for each band
        for (int b = 0; b < bands; b++) {
            means[b] /= (width * height);
        }

        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                for (int b = 0; b < bands; b++) {
                    dev[b] += Math.pow((rast.getSample(w, h, b) - means[b]), 2);
                }
            }
        }

        //gets vector of standard deviation values for each band
        for (int b = 0; b < bands; b++) {
            dev[b] = Math.sqrt(dev[b] / (width * height));
        }
        /*
        //initialize mean and dev
        for (int b = 0; b < bands; b++) {
            System.out.println(means[b]);
            System.out.println(dev[b]);
        }
         */

        //normalize training data in [0,255] range
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                for (int b = 0; b < bands; b++) {
                    raster.setSample(
                            w,
                            h,
                            b,
                            (int) (255 / (1 + Math.exp(-(rast.getSample(w, h, b) - means[b]) / dev[b])))
                    );
                    //System.out.println((int) (255 / (1 + Math.exp(-(rast.getSample(w, h, b) - means[b]) / dev[b]))));
                }
            }
        }

        return raster;
    }

    public static Raster normalizeSmarter(Raster rast) {

        int width = rast.getWidth();
        int height = rast.getHeight();
        int bands = rast.getNumBands();

        WritableRaster raster = WritableRaster.createBandedRaster(
                rast.getDataBuffer().getDataType(),
                width,
                height,
                bands,
                null);

        double[] means = new double[bands];
        double[] dev = new double[bands];

        for (int b = 0; b < bands; b++) {
            //means[b] = Mean.evaluate((double[]) rast.getSamples(0, 0, width, height, b, (float[]) null));
        }

        //gets vector of mean values for each band
        for (int b = 0; b < bands; b++) {
            means[b] /= (width * height);
        }

        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                for (int b = 0; b < bands; b++) {
                    dev[b] += Math.pow((rast.getSample(w, h, b) - means[b]), 2);
                }
            }
        }

        //gets vector of standard deviation values for each band
        for (int b = 0; b < bands; b++) {
            dev[b] = Math.sqrt(dev[b] / (width * height));
        }

        //normalize training data in [0,255] range
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                for (int b = 0; b < bands; b++) {
                    raster.setSample(
                            w,
                            h,
                            b,
                            (255 / (1 + Math.exp(-(rast.getSample(w, h, b) - means[b]) / dev[b])))
                    );

                }
            }
        }

        return raster;
    }

    /**
     * Extracts Fuzzy feature from BufferedImage stored in Patch p
     *
     * @param img
     * @param p
     * @return
     * @throws IOException
     */
    public static List<double[]> bufferedImageToFuzzy(BufferedImage img, String imageType) throws IOException {
        List<double[]> features = new ArrayList<>();
        int nBands = img.getRaster().getNumBands();
        for (int n = 0; n < nBands; n++) {
            //img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (int[]) null);
            //InputStream stream = bufferedImageToInputStream(img, imageType);
            //ColorProcessor image = new ColorProcessor(ImageIO.read(stream));
            FloatProcessor im = new FloatProcessor(
                    img.getWidth(),
                    img.getHeight(),
                    img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (float[]) null),
                    null
            );

            // initialize the descriptor
            FuzzyHistogram descriptor = new FuzzyHistogram();

            // run the descriptor and extract the features
            descriptor.run(im);

            try {
                // obtain the features
                features.set(0, (double[]) ArrayUtils.addAll((double[]) features.get(0), (double[]) descriptor.getFeatures().get(0)));
            } catch (IndexOutOfBoundsException e) {
                features.add((double[]) descriptor.getFeatures().get(0));
                System.out.println("Feature length per band: " + features.get(0).length);
            }
        }
        System.out.println("Feature total length: " + features.get(0).length);
        // print the features to system out
        //for (double[] feature : features) {
        //    System.out.println(Arrays2.join(feature, ", ", "%.5f"));
        //}
        return features;
    }

    /**
     * Extracts Gabor feature from BufferedImage stored in Patch p
     *
     * @param p
     * @return
     * @throws IOException
     */
    public static List<double[]> bufferedImageToGabor(BufferedImage img, String imageType) throws IOException {
        /*InputStream stream = bufferedImageToInputStream(img, imageType);
        ColorProcessor image = new ColorProcessor(ImageIO.read(stream));

        // initialize the descriptor
        Gabor descriptor = new Gabor();

        // run the descriptor and extract the features
        descriptor.run(image);

        // obtain the features
        List<double[]> features = descriptor.getFeatures();

        // print the features to system out
        //for (double[] feature : features) {
        //    System.out.println(Arrays2.join(feature, ", ", "%.5f"));
        //}
        return features;*/
        List<double[]> features = new ArrayList<>();
        int nBands = img.getRaster().getNumBands();
        for (int n = 0; n < nBands; n++) {
            //img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (int[]) null);
            //InputStream stream = bufferedImageToInputStream(img, imageType);
            //ColorProcessor image = new ColorProcessor(ImageIO.read(stream));
            FloatProcessor im = new FloatProcessor(
                    img.getWidth(),
                    img.getHeight(),
                    img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (float[]) null),
                    null
            );

            // initialize the descriptor
            Gabor descriptor = new Gabor();

            // run the descriptor and extract the features
            descriptor.run(im);

            try {
                // obtain the features
                features.set(0, (double[]) ArrayUtils.addAll((double[]) features.get(0), (double[]) descriptor.getFeatures().get(0)));
            } catch (IndexOutOfBoundsException e) {
                features.add((double[]) descriptor.getFeatures().get(0));
                //System.out.println("Feature length per band: " + features.get(0).length);
            }
        }
        //System.out.println("Feature total length: " + features.get(0).length);
        // print the features to system out
        //for (double[] feature : features) {
        //    System.out.println(Arrays2.join(feature, ", ", "%.5f"));
        //}
        return features;
    }

    /**
     * Extracts Haralick feature from BufferedImage stored in Patch p
     *
     * @param p
     * @return
     * @throws IOException
     */
    public static List<double[]> bufferedImageToHaralick(BufferedImage img, String imageType) throws IOException {
        /*InputStream stream = bufferedImageToInputStream(img, imageType);
        ColorProcessor image = new ColorProcessor(ImageIO.read(stream));

        // initialize the descriptor
        Haralick descriptor = new Haralick();

        // run the descriptor and extract the features
        descriptor.run(image);

        // obtain the features
        List<double[]> features = descriptor.getFeatures();

        // print the features to system out
        //for (double[] feature : features) {
        //    System.out.println(Arrays2.join(feature, ", ", "%.5f"));
        //}
        return features;
         */
        List<double[]> features = new ArrayList<>();
        int nBands = img.getRaster().getNumBands();
        for (int n = 0; n < nBands; n++) {
            //img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (int[]) null);
            //InputStream stream = bufferedImageToInputStream(img, imageType);
            //ColorProcessor image = new ColorProcessor(ImageIO.read(stream));
            FloatProcessor im = new FloatProcessor(
                    img.getWidth(),
                    img.getHeight(),
                    img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (float[]) null),
                    null
            );
            //System.out.println(Arrays.toString(img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (float[]) null)));
            //System.out.println(im);
            /// initialize the descriptor
            Haralick descriptor = new Haralick();

            // run the descriptor and extract the features
            descriptor.run(im);

            try {
                // obtain the features
                features.set(0, (double[]) ArrayUtils.addAll((double[]) features.get(0), (double[]) descriptor.getFeatures().get(0)));
            } catch (IndexOutOfBoundsException e) {
                features.add((double[]) descriptor.getFeatures().get(0));
                //System.out.println("Feature length per band: " + features.get(0).length);
            }
        }
        //System.out.println("Feature total length: " + features.get(0).length);
        //for (double[] feature : features) {
        //    System.out.println(Arrays2.join(feature, ", ", "%.5f"));
        //}
        // print the features to system out
        //for (double[] feature : features) {
        //    System.out.println(Arrays2.join(feature, ", ", "%.5f"));
        //}
        return features;
    }

    /**
     * Extracts histogram feature from BufferedImage stored in Patch p
     *
     * @param p
     * @return
     * @throws IOException
     */
    public static List<double[]> bufferedImageToHist(BufferedImage img, String imageType) throws IOException {
        /*
        // load the image
        InputStream stream = bufferedImageToInputStream(img, imageType);
        ColorProcessor image = new ColorProcessor(ImageIO.read(stream));

        // load the properties from the default properties file
        // change the histogram to span just 2 bins
        // and let's just extract the histogram for the RED channel
        LibProperties prop = LibProperties.get();
        prop.setProperty(LibProperties.HISTOGRAMS_BINS, 30);
        prop.setProperty(LibProperties.HISTOGRAMS_TYPE, "RGB");

        // after v 1.0.1 you will be able to use this:
        // prop.setProperty(LibProperties.HISTOGRAMS_TYPE, Histogram.TYPE.Red.name());
        // initialize the descriptor, set the properties and run it
        Histogram descriptor = new Histogram();
        descriptor.setProperties(prop);
        descriptor.run(image);

        // obtain the features
        List<double[]> features = descriptor.getFeatures();

        //int total = 0;
        // print the features to system out
        //for (double[] feature : features) {
        //    for (double d : feature) {
        //      System.out.print(d / Arrays2.sum(feature) + ", ");
        //  }
        //  System.out.println();
        //tota+=Arrays2.join(feature);
        // }
        return features;
         */
        List<double[]> features = new ArrayList<>();
        int nBands = img.getRaster().getNumBands();
        for (int n = 0; n < nBands; n++) {
            //img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (int[]) null);
            //InputStream stream = bufferedImageToInputStream(img, imageType);
            //ColorProcessor image = new ColorProcessor(ImageIO.read(stream));
            FloatProcessor im = new FloatProcessor(
                    img.getWidth(),
                    img.getHeight(),
                    img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (float[]) null),
                    null
            );
            // load the properties from the  default properties file
            // change the histogram to span just 2 bins
            // and let's just extract the histogram for the RED channel
            LibProperties prop = LibProperties.get();
            prop.setProperty(LibProperties.HISTOGRAMS_BINS, 30);
            prop.setProperty(LibProperties.HISTOGRAMS_TYPE, "Gray");

            // after v 1.0.1 you will be able to use this:
            // prop.setProperty(LibProperties.HISTOGRAMS_TYPE, Histogram.TYPE.Red.name());
            // initialize the descriptor, set the properties and run it
            Histogram descriptor = new Histogram();
            descriptor.setProperties(prop);
            descriptor.run(im);

            try {
                // obtain the features
                features.set(0, (double[]) ArrayUtils.addAll((double[]) features.get(0), (double[]) descriptor.getFeatures().get(0)));
            } catch (IndexOutOfBoundsException e) {
                features.add((double[]) descriptor.getFeatures().get(0));
                //System.out.println("Feature length per band: " + features.get(0).length);
            }
        }
        //System.out.println("Feature total length: " + features.get(0).length);
        // print the features to system out
        //for (double[] feature : features) {
        //    System.out.println(Arrays2.join(feature, ", ", "%.5f"));
        //}
        return features;
    }

    public static List<double[]> rasterToHist(Raster rast) throws IOException {
        List<double[]> features = new ArrayList<>();
        int nBands = rast.getNumBands();
        for (int n = 0; n < nBands; n++) {
            //img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (int[]) null);
            //InputStream stream = bufferedImageToInputStream(img, imageType);
            //ColorProcessor image = new ColorProcessor(ImageIO.read(stream));
            FloatProcessor im = new FloatProcessor(
                    rast.getWidth(),
                    rast.getHeight(),
                    rast.getSamples(0, 0, rast.getWidth(), rast.getHeight(), n, (float[]) null),
                    null
            );
            // load the properties from the  default properties file
            // change the histogram to span just 2 bins
            // and let's just extract the histogram for the RED channel
            LibProperties prop = LibProperties.get();
            prop.setProperty(LibProperties.HISTOGRAMS_BINS, 16);
            prop.setProperty(LibProperties.HISTOGRAMS_TYPE, "Gray");

            // after v 1.0.1 you will be able to use this:
            // prop.setProperty(LibProperties.HISTOGRAMS_TYPE, Histogram.TYPE.Red.name());
            // initialize the descriptor, set the properties and run it
            Histogram descriptor = new Histogram();
            descriptor.setProperties(prop);
            descriptor.run(im);

            try {
                // obtain the features
                features.set(0, (double[]) ArrayUtils.addAll((double[]) features.get(0), (double[]) descriptor.getFeatures().get(0)));
            } catch (IndexOutOfBoundsException e) {
                features.add((double[]) descriptor.getFeatures().get(0));
                //System.out.println("Feature length per band: " + features.get(0).length);
            }
        }
        //System.out.println("Feature total length: " + features.get(0).length);
        // print the features to system out
        //for (double[] feature : features) {
        //    System.out.println(Arrays2.join(feature, ", ", "%.5f"));
        //}
        return features;
    }

    public static List<double[]> arrayToHist(float[][] array) throws IOException {
        List<double[]> features = new ArrayList<>();
        double[] bins = new double[10];

        for (float[] f1 : array) {
            for (float f2 : f1) {
                if (f2 <= -0.8) {
                    bins[0]++;
                } else if (f2 <= -0.6) {
                    bins[1]++;
                } else if (f2 <= -0.4) {
                    bins[2]++;
                } else if (f2 <= -0.2) {
                    bins[3]++;
                } else if (f2 <= 0) {
                    bins[4]++;
                } else if (f2 <= 0.2) {
                    bins[5]++;
                } else if (f2 <= 0.4) {
                    bins[6]++;
                } else if (f2 <= 0.6) {
                    bins[7]++;
                } else if (f2 <= 0.8) {
                    bins[8]++;
                } else {
                    bins[9]++;
                }
            }
        }
        features.add(bins);
        return features;
    }

    public static List<double[]> bufferedImageToMeDiB(BufferedImage img, String imageType) {
        List<double[]> features = new ArrayList<>();
        int nBands = img.getRaster().getNumBands();
        for (int n = 0; n < nBands; n++) {
            double mean = 0;
            double dev = 0;
            for (double value : (double[]) img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (double[]) null)) {
                mean += value;
            }
            mean /= img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (double[]) null).length;

            for (double value : (double[]) img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (double[]) null)) {

                dev += Math.pow((value - mean), 2);
            }
            dev += 0.000001;

            dev = Math.sqrt(dev / img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (double[]) null).length);

            // obtain the features
            try {
                // obtain the features
                features.set(0, (double[]) ArrayUtils.addAll((double[]) features.get(0), new double[]{mean, dev}));
            } catch (IndexOutOfBoundsException e) {
                features.add(new double[]{mean, dev});
                //System.out.println("Feature length per band: " + features.get(0).length);
            }

        }
        //System.out.println("Size of MeDiB feature: " + features.get(0).length);
        return features;
    }

    /**
     * Extracts the histogram from the NDVI and NDBI spectral features
     *
     * @param subimage
     * @param substring
     * @param redBand red band
     * @param nirBand NIR band
     * @param mirBand MIR band
     * @return
     */
    public static List<double[]> bufferedImageToNDVBI_HB(BufferedImage subimage, String substring, int redBand, int nirBand, int mirBand) {
        List<double[]> result = new ArrayList<>();
        try {
            List<double[]> ndvi = arrayToHist(getNDVIfromBufferedImage(subimage, nirBand, redBand));
            List<double[]> ndbi = arrayToHist(getNDBIfromBufferedImage(subimage, nirBand, mirBand));
            result.add((double[]) ArrayUtils.addAll((double[]) ndvi.get(0), (double[]) ndbi.get(0)));
        } catch (IOException ex) {
            Logger.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        /*
        for (double[] feature : result) {

            System.out.println(Arrays2.join(feature, ", ", "%.5f"));
        }*/
        return result;
    }

    public static List<double[]> bufferedImageToPixels(BufferedImage img, String imageType) throws IOException {
        List<double[]> features = new ArrayList<>();
        int nBands = img.getRaster().getNumBands();
        for (int n = 0; n < nBands; n++) {
            // obtain the features
            try {
                // obtain the features
                features.set(0, (double[]) ArrayUtils.addAll((double[]) features.get(0), (double[]) img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (double[]) null)));
            } catch (IndexOutOfBoundsException e) {
                features.add((double[]) img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (double[]) null));
                //System.out.println("Feature length per band: " + features.get(0).length);
            }

        }
        //System.out.println("Feature total length: " + features.get(0).length);
        // print the features to system out
        //for (double[] feature : features) {
        //    System.out.println(Arrays2.join(feature, ", ", "%.5f"));
        //}
        return features;
    }

    /**
     * Extracts Sift feature from BufferedImage stored in Patch p
     *
     * @param p
     * @return
     * @throws IOException
     */
    public static List<double[]> bufferedImageToSift(BufferedImage img, String imageType) throws IOException {
        /*
        InputStream stream = bufferedImageToInputStream(img, imageType);
        //ImageIO.write(p.getImage(), "pnm", new File("image.pgm"));
        //ColorProcessor image = new ColorProcessor(ImageIO.read(stream));

        // initialize the descriptor
        Sift descriptor = new Sift(new File("image.pgm"));

        // run the descriptor and extract the features
        //descriptor.run(image);
        // obtain the features
        List<double[]> features = descriptor.getFeatures();
        System.out.println(features.size());
        // print the features to system out
        //for (double[] feature : features) {
        //    System.out.println(Arrays2.join(feature, ", ", "%.5f"));
        //}
        return features;
         */
        List<double[]> features = new ArrayList<>();
        int nBands = img.getRaster().getNumBands();
        for (int n = 0; n < nBands; n++) {
            //img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (int[]) null);
            //InputStream stream = bufferedImageToInputStream(img, imageType);
            //ColorProcessor image = new ColorProcessor(ImageIO.read(stream));
            FloatProcessor im = new FloatProcessor(
                    img.getWidth(),
                    img.getHeight(),
                    img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (float[]) null),
                    img.getColorModel()
            );

            // initialize the descriptor
            Sift descriptor = new Sift(new File("image.pgm"));

            // run the descriptor and extract the features
            descriptor.run(im);

            try {
                // obtain the features
                features.set(0, (double[]) ArrayUtils.addAll((double[]) features.get(0), (double[]) descriptor.getFeatures().get(0)));
            } catch (IndexOutOfBoundsException e) {
                features.add((double[]) descriptor.getFeatures().get(0));
            }
        }
        System.out.println("Feature total length: " + features.get(0).length);
        // print the features to system out
        //for (double[] feature : features) {
        //    System.out.println(Arrays2.join(feature, ", ", "%.5f"));
        //}
        return features;
    }

    /**
     * Extracts Surf feature from BufferedImage stored in Patch p
     *
     * @param p
     * @return
     * @throws IOException
     */
    public static List<double[]> bufferedImageToSurf(BufferedImage img, String imageType) throws IOException {
        /*
        InputStream stream = bufferedImageToInputStream(img, imageType);
        ColorProcessor image = new ColorProcessor(ImageIO.read(stream));

        // initialize the descriptor
        SURF descriptor = new SURF(neg, neg, neg, neg, true, true, true, neg, true);

        // run the descriptor and extract the features
        descriptor.run(image);

        // obtain the features
        List<double[]> features = descriptor.getFeatures();
        System.out.println(features.size());
        //System.out.println(features.get(0).length);

        // print the features to system out
        //for (double[] feature : features) {
        //    System.out.println(Arrays2.join(feature, ", ", "%.5f"));
        //}
        return features;
         */
        List<double[]> features = new ArrayList<>();
        int nBands = img.getRaster().getNumBands();
        for (int n = 0; n < nBands; n++) {
            //img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (int[]) null);
            //InputStream stream = bufferedImageToInputStream(img, imageType);
            //ColorProcessor image = new ColorProcessor(ImageIO.read(stream));
            FloatProcessor im = new FloatProcessor(
                    img.getWidth(),
                    img.getHeight(),
                    img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), n, (float[]) null),
                    img.getColorModel()
            );

            // initialize the descriptor
            SURF descriptor = new SURF(neg, neg, neg, neg, true, true, true, neg, true);

            // run the descriptor and extract the features
            descriptor.run(im);

            try {
                // obtain the features
                features.set(0, (double[]) ArrayUtils.addAll((double[]) features.get(0), (double[]) descriptor.getFeatures().get(0)));
            } catch (IndexOutOfBoundsException e) {
                features.add((double[]) descriptor.getFeatures().get(0));
            }
        }
        System.out.println("Feature total length: " + features.get(0).length);
        // print the features to system out
        //for (double[] feature : features) {
        //    System.out.println(Arrays2.join(feature, ", ", "%.5f"));
        //}
        return features;
    }

    public static float[][] getNDVIfromBufferedImage(BufferedImage image, int nirBand, int redBand) {

        float[][] ndvi = new float[image.getHeight()][image.getWidth()];

        for (int w = 0; w < image.getWidth(); w++) {
            for (int h = 0; h < image.getHeight(); h++) {
                ndvi[h][w] = (float) (image.getRaster().getSample(w, h, nirBand) - image.getRaster().getSample(w, h, redBand))
                        / (image.getRaster().getSample(w, h, nirBand) + image.getRaster().getSample(w, h, redBand));
            }
        }
        return ndvi;
    }

    public static float[][] getNDBIfromBufferedImage(BufferedImage image, int mirBand, int nirBand) {

        float[][] ndbi = new float[image.getHeight()][image.getWidth()];

        for (int w = 0; w < image.getWidth(); w++) {
            for (int h = 0; h < image.getHeight(); h++) {

                ndbi[h][w] = (float) (image.getRaster().getSample(w, h, mirBand) - image.getRaster().getSample(w, h, nirBand))
                        / (image.getRaster().getSample(w, h, mirBand) + image.getRaster().getSample(w, h, nirBand));
            }
        }

        return ndbi;
    }

    public static float[][] getSAVIfromBufferedImage(BufferedImage image, int nirBand, int redBand) {

        float[][] savi = new float[image.getHeight()][image.getWidth()];

        for (int w = 0; w < image.getWidth(); w++) {
            for (int h = 0; h < image.getHeight(); h++) {
                savi[h][w] = (float) (((image.getRaster().getSample(w, h, nirBand) - image.getRaster().getSample(w, h, redBand)) * 1.5)
                        / (image.getRaster().getSample(w, h, nirBand) + image.getRaster().getSample(w, h, redBand) + 0.5));
            }
        }

        return savi;
    }

    public static float[][] getMNDWIfromBufferedImage(BufferedImage image, int greenBand, int mirBand) {

        float[][] mndwi = new float[image.getHeight()][image.getWidth()];

        for (int w = 0; w < image.getWidth(); w++) {
            for (int h = 0; h < image.getHeight(); h++) {

                mndwi[h][w] = (float) (image.getRaster().getSample(w, h, greenBand) - image.getRaster().getSample(w, h, mirBand))
                        / (image.getRaster().getSample(w, h, greenBand) + image.getRaster().getSample(w, h, mirBand));
            }
        }

        return mndwi;
    }

    /**
     * Returns an array containing in order the number of truePositives,
     * trueNegatives, falsePositives, and falseNegatives
     *
     * @param groundTruth File containing the binary classification for class
     * @param clas
     * @param clas The class of interest from a binary classification
     * @param classifiedResults First set of classified list of Patch objects
     * @param toBeClassifiedResults Additional set of classified list of Patch
     * objects
     * @return
     * @throws IOException
     */
    /*
    public static int[] getStatistics(File groundTruth, String clas, List<Sits> classifiedResults, List<Sits> toBeClassifiedResults) throws IOException {
        int truePos = 0;
        int trueNeg = 0;
        int falsePos = 0;
        int falseNeg = 0;
        BufferedImage grdTruthImg = ImageIO.read(groundTruth);
        System.out.println("Image size width: " + grdTruthImg.getWidth() + " height: " + grdTruthImg.getHeight());
        System.out.println("Class = " + clas + " number of objects = " + (classifiedResults.size() + toBeClassifiedResults.size()));
        for (Sits p : classifiedResults) {
            if (p.getClasa().equals(clas) && p.isPositive() && ((grdTruthImg.getRGB(p.getxIndex(), p.getyIndex()) & 0xff) == 255)) {
                truePos++;
            }
            if (p.getClasa().equals(clas) && p.isPositive() && ((grdTruthImg.getRGB(p.getxIndex(), p.getyIndex()) & 0xff) == 0)) {
                falsePos++;
            }
            if (!p.getClasa().equals(clas) && ((grdTruthImg.getRGB(p.getxIndex(), p.getyIndex()) & 0xff) == 0)) {
                trueNeg++;
            }
            if (!p.getClasa().equals(clas) && ((grdTruthImg.getRGB(p.getxIndex(), p.getyIndex()) & 0xff) == 255)) {
                falseNeg++;
            }
        }

        for (Sits p : toBeClassifiedResults) {
            if (p.getClasa().equals(clas) && p.isPositive() && ((grdTruthImg.getRGB(p.getxIndex(), p.getyIndex()) & 0xff) == 255)) {
                truePos++;
            }
            if (p.getClasa().equals(clas) && p.isPositive() && ((grdTruthImg.getRGB(p.getxIndex(), p.getyIndex()) & 0xff) == 0)) {
                falsePos++;
            }
            if (!p.getClasa().equals(clas) && ((grdTruthImg.getRGB(p.getxIndex(), p.getyIndex()) & 0xff) == 0)) {
                trueNeg++;
            }
            if (!p.getClasa().equals(clas) && ((grdTruthImg.getRGB(p.getxIndex(), p.getyIndex()) & 0xff) == 255)) {
                falseNeg++;
            }
        }

        return new int[]{truePos, trueNeg, falsePos, falseNeg};

    }

    public static float getPrecision(int[] statistics) {

        int truePos = statistics[0];
        //int trueNeg = statistics[1];
        int falsePos = statistics[2];
        //int falseNeg = statistics[3];        

        return (float) truePos / (truePos + falsePos);
    }

    public static float getRecall(int[] statistics) {
        int truePos = statistics[0];
        //int trueNeg = statistics[1];
        //int falsePos = statistics[2];
        int falseNeg = statistics[3];

        return (float) truePos / (truePos + falseNeg);
    }

    public static float getAccuracy(int[] statistics) {
        int truePos = statistics[0];
        int trueNeg = statistics[1];
        int falsePos = statistics[2];
        int falseNeg = statistics[3];

        return (float) (truePos + trueNeg) / (truePos + trueNeg + falsePos + falseNeg);
    }

    public static float getFmeasure(int[] statistics) {
        //int truePos = statistics[0];
        //int trueNeg = statistics[1];
        //int falsePos = statistics[2];
        //int falseNeg = statistics[3];

        return (float) 2 * getPrecision(statistics) * getRecall(statistics) / (getPrecision(statistics) + getRecall(statistics));
    }
     */
}
