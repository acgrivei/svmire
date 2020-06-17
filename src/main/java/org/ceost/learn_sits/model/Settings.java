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
public class Settings {

    String featureUsed;
    String patchSize;
    ArrayList<String> labels;
    int reiterations;
    SVMSettings svmSettings;

    public String getFeatureUsed() {
        return featureUsed;
    }

    public void setFeatureUsed(String featureUsed) {
        this.featureUsed = featureUsed;
    }

    public String getPatchSize() {
        return patchSize;
    }

    public void setPatchSize(String patchSize) {
        this.patchSize = patchSize;
    }

    public ArrayList<String> getLabels() {
        return labels;
    }

    public void setLabels(ArrayList<String> labels) {
        this.labels = labels;
    }

    public int getReiterations() {
        return reiterations;
    }

    public void setReiterations(int reiterations) {
        this.reiterations = reiterations;
    }

    public SVMSettings getSvmSettings() {
        return svmSettings;
    }

    public void setSvmSettings(SVMSettings svmSettings) {
        this.svmSettings = svmSettings;
    }

}
