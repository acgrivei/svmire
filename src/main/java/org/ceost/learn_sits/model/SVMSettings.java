/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ceost.learn_sits.model;

import java.util.ArrayList;

/**
 *
 * @author Alex
 */
public class SVMSettings {

    String svm_type;
    String kernel_type;
    boolean normalized;
    String polDegree;
    String gamma;
    String coef0;
    String nu;
    String cache_size;
    String C;
    String eps;
    String p;
    String shrinking;
    String probability;
    String nr_weight;
    String weight_label;
    String weight;
    private ArrayList<String> kernelTypes = new ArrayList<>();
    private ArrayList<String> svmTypes = new ArrayList<>();

    public SVMSettings() {

        //fill svm types list
        svmTypes.add("C-SVC");
        svmTypes.add("nu-SVC");
        svmTypes.add("one-class SVM");
        svmTypes.add("epsilon-SVR");
        svmTypes.add("nu-SVR");

        //fill kernel type list
        kernelTypes.add("linear");
        kernelTypes.add("polynomial");
        kernelTypes.add("radial");
        kernelTypes.add("sigmoid");
    }

    public String getSvm_type() {
        return svm_type;
    }

    public void setSvm_type(String svm_type) {
        this.svm_type = svm_type;
    }

    public String getKernel_type() {
        return kernel_type;
    }

    public void setKernel_type(String kernel_type) {
        this.kernel_type = kernel_type;
    }

    public boolean isNormalized() {
        return normalized;
    }

    public void setNormalized(boolean normalized) {
        this.normalized = normalized;
    }

    public String getPolDegree() {
        return polDegree;
    }

    public void setPolDegree(String polDegree) {
        this.polDegree = polDegree;
    }

    public String getGamma() {
        return gamma;
    }

    public void setGamma(String gamma) {
        this.gamma = gamma;
    }

    public String getCoef0() {
        return coef0;
    }

    public void setCoef0(String coef0) {
        this.coef0 = coef0;
    }

    public String getNu() {
        return nu;
    }

    public void setNu(String nu) {
        this.nu = nu;
    }

    public String getCache_size() {
        return cache_size;
    }

    public void setCache_size(String cache_size) {
        this.cache_size = cache_size;
    }

    public String getC() {
        return C;
    }

    public void setC(String C) {
        this.C = C;
    }

    public String getEps() {
        return eps;
    }

    public void setEps(String eps) {
        this.eps = eps;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public String getShrinking() {
        return shrinking;
    }

    public void setShrinking(String shrinking) {
        this.shrinking = shrinking;
    }

    public String getProbability() {
        return probability;
    }

    public void setProbability(String probability) {
        this.probability = probability;
    }

    public String getNr_weight() {
        return nr_weight;
    }

    public void setNr_weight(String nr_weight) {
        this.nr_weight = nr_weight;
    }

    public String getWeight_label() {
        return weight_label;
    }

    public void setWeight_label(String weight_label) {
        this.weight_label = weight_label;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public ArrayList<String> getKernelTypes() {
        return kernelTypes;
    }

    public void setKernelTypes(ArrayList<String> kernelTypes) {
        this.kernelTypes = kernelTypes;
    }

    public ArrayList<String> getSvmTypes() {
        return svmTypes;
    }

    public void setSvmTypes(ArrayList<String> svmTypes) {
        this.svmTypes = svmTypes;
    }

}
