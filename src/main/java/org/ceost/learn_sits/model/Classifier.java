/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ceost.learn_sits.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import libsvm.svm;
import static libsvm.svm.svm_predict;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import org.apache.commons.lang.ArrayUtils;
import org.ceost.learn_sits.MainSceneController;
import org.ceost.learn_sits.util.ImageProcessor;

/**
 *
 * @author Alex
 */
public class Classifier implements Serializable {

    private static final long serialVersionUID = 1L;

    //list containing the labels for the classification
    private ArrayList<String> labels = new ArrayList<>();

    //classification
    //
    //initialize parameter object
    private svm_parameter param = new svm_parameter();
    //initialize svm problem
    private svm_problem prob = new svm_problem();
    //instantiate svm model
    private svm_model model = new svm_model();

    //the image feature witch is extracted
    private String feature = new String();

    //use normalization for features
    private boolean normalize = false;
    //use PCA for reduction
    private boolean usePCA = false;
    private int nOfPCAComponents = 3;
    //
    private svm_node[] means;
    private svm_node[] dev;
    private ArrayList<String> kernelTypes = new ArrayList<>();

    //mapping by feature, normalization, pca and patch to svm_node[]
    private transient HashMap<Parameter, HashMap<Sits, svm_node[]>> patchToSVM_NodeMap = new HashMap<>();

    public Classifier() {
        setDefaultParameters();
        kernelTypes.add("linear");
        kernelTypes.add("polynomial");
        kernelTypes.add("radial");
        kernelTypes.add("sigmoid");
    }

    /**
     * Transforms an RGB image to a vector of svm_nodes
     *
     * @param buffImage
     * @return
     */
    private svm_node[] bufferedImageToNode(Sits s) throws IOException, NullPointerException {
        //System.out.println("Parameters= feature: " + feature + "; normalized: " + normalize + "; PCA: " + usePCA);
        //check if a mapping has been made and return de svm_node[] coresponding Patch P
        Parameter parameter = new Parameter(feature, normalize, usePCA);
        //Check if the mapper is initialized or not
        if (patchToSVM_NodeMap == null) {
            patchToSVM_NodeMap = new HashMap<>();
        }

        if (null != patchToSVM_NodeMap.get(parameter) && null != patchToSVM_NodeMap.get(parameter).get(s)) {
            //System.out.println("Mapping found for patch: " + p);
            return patchToSVM_NodeMap.get(new Parameter(feature, normalize, usePCA)).get(s);
        } else {
            //System.out.println("Mapping NOT found for patch: " + p);
            List<double[]> imgFeature = new ArrayList<>();
            svm_node[] result = null;
            //test the value of feature
            switch (feature) {
                case "Fuzzy":
                    for (Patch p : s.getSits()) {
                        List<double[]> intermFeat = ImageProcessor.bufferedImageToFuzzy(
                                MainSceneController.buffImages.get(p.getParentID()).getSubimage(p.getxIndex(), p.getyIndex(), p.getSize(), p.getSize()), //get patch out of big image
                                MainSceneController.files.get(p.getParentID()).substring(MainSceneController.files.get(p.getParentID()).lastIndexOf(".") + 1)
                        );
                        try {
                            // obtain the features
                            imgFeature.set(0, (double[]) ArrayUtils.addAll((double[]) imgFeature.get(0), (double[]) intermFeat.get(0)));
                        } catch (IndexOutOfBoundsException e) {
                            imgFeature.add((double[]) intermFeat.get(0));
                            //System.out.println("Feature length per band: " + features.get(0).length);
                        }
                    }
                    break;

                case "Haralick":
                    for (Patch p : s.getSits()) {
                        List<double[]> intermFeat = ImageProcessor.bufferedImageToHaralick(MainSceneController.buffImages.get(p.getParentID()).getSubimage(p.getxIndex(), p.getyIndex(), p.getSize(), p.getSize()), //get patch out of big image
                                MainSceneController.files.get(p.getParentID()).substring(MainSceneController.files.get(p.getParentID()).lastIndexOf(".") + 1) //get file type
                        );
                        try {
                            // obtain the features
                            imgFeature.set(0, (double[]) ArrayUtils.addAll((double[]) imgFeature.get(0), (double[]) intermFeat.get(0)));
                        } catch (IndexOutOfBoundsException e) {
                            imgFeature.add((double[]) intermFeat.get(0));
                            //System.out.println("Feature length per band: " + features.get(0).length);
                        }
                    }
                    break;
                case "Histogram":
                    for (Patch p : s.getSits()) {
                        List<double[]> intermFeat = ImageProcessor.bufferedImageToHist(MainSceneController.buffImages.get(p.getParentID()).getSubimage(p.getxIndex(), p.getyIndex(), p.getSize(), p.getSize()), //get patch out of big image
                                MainSceneController.files.get(p.getParentID()).substring(MainSceneController.files.get(p.getParentID()).lastIndexOf(".") + 1) //get file type
                        );
                        try {
                            // obtain the features
                            imgFeature.set(0, (double[]) ArrayUtils.addAll((double[]) imgFeature.get(0), (double[]) intermFeat.get(0)));
                        } catch (IndexOutOfBoundsException e) {
                            imgFeature.add((double[]) intermFeat.get(0));
                            //System.out.println("Feature length per band: " + features.get(0).length);
                        }
                    }
                    break;
                case "Gabor":
                    for (Patch p : s.getSits()) {
                        List<double[]> intermFeat = ImageProcessor.bufferedImageToGabor(MainSceneController.buffImages.get(p.getParentID()).getSubimage(p.getxIndex(), p.getyIndex(), p.getSize(), p.getSize()), //get patch out of big image
                                MainSceneController.files.get(p.getParentID()).substring(MainSceneController.files.get(p.getParentID()).lastIndexOf(".") + 1) //get file type
                        );
                        try {
                            // obtain the features
                            imgFeature.set(0, (double[]) ArrayUtils.addAll((double[]) imgFeature.get(0), (double[]) intermFeat.get(0)));
                        } catch (IndexOutOfBoundsException e) {
                            imgFeature.add((double[]) intermFeat.get(0));
                            //System.out.println("Feature length per band: " + features.get(0).length);
                        }
                    }
                    break;

                case "MeDiB":
                    for (Patch p : s.getSits()) {
                        List<double[]> intermFeat = ImageProcessor.bufferedImageToMeDiB(MainSceneController.buffImages.get(p.getParentID()).getSubimage(p.getxIndex(), p.getyIndex(), p.getSize(), p.getSize()), //get patch out of big image
                                MainSceneController.files.get(p.getParentID()).substring(MainSceneController.files.get(p.getParentID()).lastIndexOf(".") + 1) //get file type
                        );
                        try {
                            // obtain the features
                            imgFeature.set(0, (double[]) ArrayUtils.addAll((double[]) imgFeature.get(0), (double[]) intermFeat.get(0)));
                        } catch (IndexOutOfBoundsException e) {
                            imgFeature.add((double[]) intermFeat.get(0));
                            //System.out.println("Feature length per band: " + features.get(0).length);
                        }
                    }
                    System.out.println("Size of MeDiB feature: " + imgFeature.get(0).length);
                    break;
                case "NDVBI-HB":
                    for (Patch p : s.getSits()) {
                        List<double[]> intermFeat = ImageProcessor.bufferedImageToNDVBI_HB(MainSceneController.buffImages.get(p.getParentID()).getSubimage(p.getxIndex(), p.getyIndex(), p.getSize(), p.getSize()), //get patch out of big image
                                MainSceneController.files.get(p.getParentID()).substring(MainSceneController.files.get(p.getParentID()).lastIndexOf(".") + 1),//get file type
                                MainSceneController.redBand,
                                MainSceneController.nirBand,
                                MainSceneController.mirBand
                        );
                        try {
                            // obtain the features
                            imgFeature.set(0, (double[]) ArrayUtils.addAll((double[]) imgFeature.get(0), (double[]) intermFeat.get(0)));
                        } catch (IndexOutOfBoundsException e) {
                            imgFeature.add((double[]) intermFeat.get(0));
                            //System.out.println("Feature length per band: " + features.get(0).length);
                        }
                    }
                    break;
                case "Pixels":
                    for (Patch p : s.getSits()) {

                        BufferedImage buffImage = MainSceneController.buffImages.get(p.getParentID()).getSubimage(p.getxIndex(), p.getyIndex(), p.getSize(), p.getSize()); //get patch out of big image

                        List<double[]> intermFeat = ImageProcessor.bufferedImageToPixels(MainSceneController.buffImages.get(p.getParentID()).getSubimage(p.getxIndex(), p.getyIndex(), p.getSize(), p.getSize()), //get patch out of big image
                                MainSceneController.files.get(p.getParentID()).substring(MainSceneController.files.get(p.getParentID()).lastIndexOf(".") + 1) //get file type
                        );
                        try {
                            // obtain the features
                            imgFeature.set(0, (double[]) ArrayUtils.addAll((double[]) imgFeature.get(0), (double[]) intermFeat.get(0)));
                        } catch (IndexOutOfBoundsException e) {
                            imgFeature.add((double[]) intermFeat.get(0));
                            //System.out.println("Feature length per band: " + features.get(0).length);
                        }
                    }
                    break;
                case "Sift":
                    for (Patch p : s.getSits()) {
                        List<double[]> intermFeat = ImageProcessor.bufferedImageToSift(
                                MainSceneController.buffImages.get(p.getParentID()).getSubimage(p.getxIndex(), p.getyIndex(), p.getSize(), p.getSize()), //get patch out of big image
                                MainSceneController.files.get(p.getParentID()).substring(MainSceneController.files.get(p.getParentID()).lastIndexOf(".") + 1) //get file type
                        );
                        try {
                            // obtain the features
                            imgFeature.set(0, (double[]) ArrayUtils.addAll((double[]) imgFeature.get(0), (double[]) intermFeat.get(0)));
                        } catch (IndexOutOfBoundsException e) {
                            imgFeature.add((double[]) intermFeat.get(0));
                            //System.out.println("Feature length per band: " + features.get(0).length);
                        }
                    }
                    break;
                case "Surf":
                    for (Patch p : s.getSits()) {
                        List<double[]> intermFeat = ImageProcessor.bufferedImageToSurf(
                                MainSceneController.buffImages.get(p.getParentID()).getSubimage(p.getxIndex(), p.getyIndex(), p.getSize(), p.getSize()), //get patch out of big image
                                MainSceneController.files.get(p.getParentID()).substring(MainSceneController.files.get(p.getParentID()).lastIndexOf(".") + 1) //get file type
                        );
                        try {
                            // obtain the features
                            imgFeature.set(0, (double[]) ArrayUtils.addAll((double[]) imgFeature.get(0), (double[]) intermFeat.get(0)));
                        } catch (IndexOutOfBoundsException e) {
                            imgFeature.add((double[]) intermFeat.get(0));
                            //System.out.println("Feature length per band: " + features.get(0).length);
                        }
                    }
                    break;
            }
            result = featureToSVM_Node(imgFeature);
            if (normalize) {
                result = normalizeSVM_Node(result);
            }
            if (usePCA) {
                //result = ImageProcessor.getPCAfromSVM_Node(result, nOfPCAComponents);
            }
            //System.out.println("Result obtained");
            addPatchToSVM_NodeMapping(s, result);
            //returns the svm_node vector
            return result;
        }

    }

    /**
     * Transforms feature (List<Double[]>) to svm_node[]
     *
     * @param imgFeature
     * @return
     */
    private svm_node[] featureToSVM_Node(List<double[]> imgFeature) {
        svm_node[] result = new svm_node[imgFeature.size() * imgFeature.get(0).length];
        for (int i = 0; i < imgFeature.size(); i++) {
            int listArraySize = imgFeature.get(0).length;
            for (int j = 0; j < listArraySize; j++) {
                //instantiate svm_node objects
                result[listArraySize * i + j] = new svm_node();
                //set index
                result[listArraySize * i + j].index = listArraySize * i + j + 1;
                //set value 
                result[listArraySize * i + j].value = imgFeature.get(i)[j];

            }
        }
        //System.out.println("Conversion done!");
        return result;
    }

    //normalizes svm_node
    private svm_node[] normalizeSVM_Node(svm_node[] node) {
        svm_node[] normNode = node;

        for (int j = 0; j < node.length; j++) {
            //soft normalizes each value for j index
            normNode[j].value = (node[j].value - means[j].value) / (2 * dev[j].value);

            //System.out.print("Normed value: " + normNode[j].value + ", ");
        }
        return normNode;
    }

    /**
     * Converts a list of Patch objects into a problem object (which is globally
     * defined)
     *
     * @param patches
     * @throws IOException
     */
    private void convertTrainingData(ArrayList<Sits> patches) throws IOException {
        System.out.println("Started training data conversion");
        long time0 = System.currentTimeMillis();//get curent time to calculate the processing time
        prob.l = patches.size();//set training list length
        prob.y = new double[prob.l];//list of labels
        prob.x = new svm_node[prob.l][];//vector of svm_node objects        

        //if normalized mode is choosen
        if (normalize) {
            //set normalization to false to be able to calculate mean and dispersion
            normalize = false;
            //without normalization
            for (int i = 0; i < prob.l; i++) {
                //get the patch from the list passed as argument
                Sits p = patches.get(i);
                //System.out.println(p);
                prob.x[i] = bufferedImageToNode(p);//set i svm_node object
                if (labels.contains(p.getClasa())) {
                    prob.y[i] = labels.indexOf(p.getClasa());//set the label for the svm_object 
                } else {
                    labels.add(p.getClasa());//if the label is not defined add new lable to the label list
                    prob.y[i] = labels.indexOf(p.getClasa());//set the label for the svm_object 
                }

            }
            //System.out.println("Normalization in process!");
            means = new svm_node[prob.x[0].length];
            dev = new svm_node[prob.x[0].length];

            for (int j = 0; j < prob.x[0].length; j++) {
                means[j] = new svm_node();
                means[j].index = j;
                for (int i = 0; i < prob.l; i++) {
                    //sums the values of the training samples for each index
                    means[j].value += prob.x[i][j].value;
                }
            }
            //gets vector of mean values for each index
            for (int j = 0; j < means.length; j++) {
                means[j].value /= prob.l;
            }

            for (int j = 0; j < prob.x[0].length; j++) {
                dev[j] = new svm_node();
                dev[j].index = j;
                for (int i = 0; i < prob.l; i++) {
                    //sums the values of the training samples for each index
                    dev[j].value += Math.pow((prob.x[i][j].value - means[j].value), 2);
                }
                dev[j].value += 0.000001;
            }

            //gets vector of standard deviation values for each index
            for (int j = 0; j < means.length; j++) {
                dev[j].value = Math.sqrt(dev[j].value / prob.l);
            }

            //set normalize back to true and calculate normalized values
            normalize = true;
            //get normalized data
            for (int i = 0; i < prob.l; i++) {
                //get the patch from the list passed as argument
                Sits p = patches.get(i);
                //System.out.println(p);
                prob.x[i] = bufferedImageToNode(p);//set i svm_node object
                if (labels.contains(p.getClasa())) {
                    prob.y[i] = labels.indexOf(p.getClasa());//set the label for the svm_object 
                } else {
                    labels.add(p.getClasa());//if the label is not defined add new lable to the label list
                    prob.y[i] = labels.indexOf(p.getClasa());//set the label for the svm_object 
                }

            }
            System.out.println("Normalization done!");
        } else {
            //without normalization
            for (int i = 0; i < prob.l; i++) {
                //get the patch from the list passed as argument
                Sits p = patches.get(i);
                //System.out.println(p);
                prob.x[i] = bufferedImageToNode(p);//set i svm_node object
                if (labels.contains(p.getClasa())) {
                    prob.y[i] = labels.indexOf(p.getClasa());//set the label for the svm_object 
                } else {
                    labels.add(p.getClasa());//if the label is not defined add new lable to the label list
                    prob.y[i] = labels.indexOf(p.getClasa());//set the label for the svm_object 
                }

            }

        }
        System.out.println("Total time for transformations: " + (System.currentTimeMillis() - time0));//print total processing time for above operations 

    }

    /**
     * Builds a classification model out of the list of patches passed as
     * training samples
     *
     * @param patches
     * @throws IOException
     */
    public void buildModel(ArrayList<Sits> patches) throws IOException {
        //convert data to the format neded for classification
        convertTrainingData(patches);

        //model calculation start time
        long time0 = System.currentTimeMillis();
        //build model 
        System.out.println("Transformed objects: " + prob.y.length);
        model = svm.svm_train(prob, param);//function contained in libSVM
        System.out.println(model.SV);
        System.out.println("Model calculation time: " + (System.currentTimeMillis() - time0));

    }

    /**
     * Returns the label for a test Patch
     *
     * @param p
     * @return
     * @throws IOException
     */
    public String predictLabel(Sits p) throws IOException {
        //transform the Patch object into a svm_node object
        svm_node[] toTest = bufferedImageToNode(p);

        //one class classification returns labels -1 or 1        
        if (param.svm_type == svm_parameter.ONE_CLASS) {
            //if's first branch returns first lables list element for -1 and second list element for 1
            if ((int) svm_predict(model, toTest) == 1) {
                return labels.get(0);
            } else {
                return "false" + labels.get(0);
            }
        } else {
            return labels.get((int) svm_predict(model, toTest));
        }

    }

    /**
     * Maps p to svm_node[] result with parameter feature, normalization and pca
     *
     * @param p
     * @param result
     */
    private void addPatchToSVM_NodeMapping(Sits p, svm_node[] result) {
        Parameter parameter = new Parameter(feature, normalize, usePCA);
        if (patchToSVM_NodeMap.containsKey(parameter)) {
            if (!patchToSVM_NodeMap.get(parameter).containsKey(p)) {
                HashMap<Sits, svm_node[]> interm = patchToSVM_NodeMap.get(parameter);
                interm.put(p, result);
                patchToSVM_NodeMap.replace(parameter, interm);
            }
        } else {
            HashMap<Sits, svm_node[]> interm = new HashMap<>();
            interm.put(p, result);
            patchToSVM_NodeMap.put(parameter, interm);
        }
        //System.out.println("Mapping done: " + patchToSVM_NodeMap.keySet().size() + " patche: " + patchToSVM_NodeMap.get(parameter).size());

    }

    /**
     * Calculates the euclidian distance between a Patch and the model defined
     * in the training process
     *
     * @param p
     * @return
     * @throws java.io.IOException
     */
    public double getDistance(Sits p) throws IOException {
        //initialize distance as the max value for double
        double dist = Double.MAX_VALUE;
        try {
            double sumW = 0;
            //get vector from patch
            svm_node[] pct = bufferedImageToNode(p);

            //get sv_coef
            double[][] coef = model.sv_coef;
            //get sv
            svm_node[][] sv = model.SV;

            //get b
            double[] rho = model.rho;
            double b = -rho[0];
            //create weights
            double[] w = new double[sv[0].length];

            //calculate distance for one class
            if (param.svm_type == svm_parameter.ONE_CLASS) {
                for (int i = 0; i < sv[0].length; i++) {
                    w[i] = 0;
                    for (int j = 0; j < coef[0].length; j++) {
                        w[i] += coef[0][j] * sv[j][i].value;
                    }
                    sumW += w[i] * w[i];

                }
            } else {
                //needs development for multiclass
            }

            double d = 0;
            for (int k = 0; k < w.length; k++) {
                d += w[k] * pct[k].value;
            }
            d += b;
            d = Math.abs(d) / (Math.sqrt(sumW) + 0.000001);
            double[] dec = new double[this.labels.size() * (this.labels.size() - 1) / 2];
            svm.svm_predict_values(model, pct, dec);
            //System.out.println(dec.length);
            return d;
        } catch (Exception e) {
            return dist;
        }
    }

    //works only when number of classes is = 2
    public double getDistance2(Sits p) throws IOException {
        double[] dec = new double[this.labels.size() * (this.labels.size() - 1) / 2];
        //double[] dec = new double[this.labels.size()];
        svm_node[] pct = bufferedImageToNode(p);
        svm.svm_predict_values(model, pct, dec);

        //svm.svm_predict_probability(model, pct, dec);
        //System.out.println(Arrays.toString(dec));
        return Math.abs(dec[0]);
        //Arrays.sort(dec);
        //return dec[dec.length-1];
    }

    /**
     * Save features as text file
     *
     * @param file
     * @throws IOException
     */
    /*
    public void printFeatures(File file) throws IOException {

        for (Parameter parameter : patchToSVM_NodeMap.keySet()) {
            StringBuilder output = new StringBuilder();
            for (Sits p : patchToSVM_NodeMap.get(parameter).keySet()) {
                output.append(p.getxIndex()).
                        append(" ").
                        append(p.getyIndex()).
                        append(" ");
                for (svm_node node : patchToSVM_NodeMap.get(parameter).get(p)) {
                    output.append(node.value).append(" ");
                }
                output.append(System.lineSeparator());
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
                    file.getAbsolutePath()
                    + "/" + feature
                    + "_" + (normalize ? "norm" : "raw")
                    + "_" + (usePCA ? "PCA" : "noPCA")
                    + ".txt"))
            );
            writer.write(output.toString());
            writer.close();
        }
    }
     */
    /**
     * Initialize default values for param object
     */
    public void setDefaultParameters() {

        // default values
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.degree = 3;
        param.gamma = 0.01;
        param.coef0 = 0;
        param.nu = 0.5;
        param.cache_size = 40;
        param.C = 1;
        param.eps = 1e-3;
        param.p = 0.1;
        param.shrinking = 0;
        param.probability = 1;
        param.nr_weight = 0;
        param.weight_label = new int[0];
        param.weight = new double[0];

        feature = "Histogram";
        normalize = false;
        usePCA = false;
        nOfPCAComponents = 3;
    }

    /////////////////////////////////////////////////////////////
    //Geters and seters for modiffing the list of parameters
    public int getSVMtype() {
        return param.svm_type;
    }

    public int getKernelType() {
        return param.kernel_type;
    }

    public int getDegree() {
        return param.degree;
    }

    public double getGamma() {
        return param.gamma;
    }

    public double getCoef() {
        return param.coef0;
    }

    public double getCache() {
        return param.cache_size;
    }

    public double getEps() {
        return param.eps;
    }

    public double getC() {
        return param.C;
    }

    public int getNoWeights() {
        return param.nr_weight;
    }

    public double getNu() {
        return param.nu;
    }

    public double getP() {
        return param.p;
    }

    public int getShrinking() {
        return param.shrinking;
    }

    public int getProbability() {
        return param.probability;
    }

    public String getFeature() {
        return feature;
    }

    public void setSVMtype(int svmType) {
        param.svm_type = svmType;
    }

    public void setKernelType(int kernelType) {
        param.kernel_type = kernelType;
    }

    public void setDegree(int degree) {
        param.degree = degree;
    }

    public void setGamma(double gamma) {
        param.gamma = gamma;
    }

    public void setCoef(double coef) {
        param.coef0 = coef;
    }

    public void setCache(double cache) {
        param.cache_size = cache;
    }

    public void setEps(double epsilon) {
        param.eps = epsilon;
    }

    public void setC(double c) {
        param.C = c;
    }

    public void setNoWeights(int nOfWeights) {
        param.nr_weight = nOfWeights;
    }

    public void setNu(double nu) {
        param.nu = nu;
    }

    public void setP(double p) {
        param.p = p;
    }

    public void setShrinking(int shrink) {
        param.shrinking = shrink;
    }

    public void setProbability(int prob) {
        param.probability = prob;
    }

    public void setFeature(String feat) {
        feature = feat;
    }

    public ArrayList<String> getLabels() {
        return labels;
    }

    public void setLabels(ArrayList<String> labels) {
        this.labels = labels;
    }

    public svm_parameter getParam() {
        return param;
    }

    public void setParam(svm_parameter param) {
        this.param = param;
    }

    public boolean isNormalize() {
        return normalize;
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public ArrayList<String> getKernelTypes() {
        return kernelTypes;
    }

    public void setKernelTypes(ArrayList<String> kernelTypes) {
        this.kernelTypes = kernelTypes;
    }

    public int getNofPCAComponents() {
        return nOfPCAComponents;
    }

    public void setNofPCAComponents(int nOfPCAComponents) {
        this.nOfPCAComponents = nOfPCAComponents;
    }

    public void setPCA(boolean b) {
        usePCA = b;
    }

    public boolean isPCAused() {
        return usePCA;
    }

}
