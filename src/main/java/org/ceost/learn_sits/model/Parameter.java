/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ceost.learn_sits.model;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author ceost-alex
 */
public class Parameter implements Serializable{

    private String feature;
    private boolean normalization;
    private boolean PCA;

    public Parameter(String feature, boolean normalization, boolean PCA) {
        this.feature = feature;
        this.normalization = normalization;
        this.PCA = PCA;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public boolean isNormalization() {
        return normalization;
    }

    public void setNormalization(boolean normalization) {
        this.normalization = normalization;
    }

    public boolean isPCA() {
        return PCA;
    }

    public void setPCA(boolean PCA) {
        this.PCA = PCA;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.feature);
        hash = 31 * hash + (this.normalization ? 1 : 0);
        hash = 31 * hash + (this.PCA ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Parameter other = (Parameter) obj;
        if (this.normalization != other.normalization) {
            return false;
        }
        if (this.PCA != other.PCA) {
            return false;
        }
        if (!Objects.equals(this.feature, other.feature)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Parameter{" + "feature=" + feature + ", normalization=" + normalization + ", PCA=" + PCA + '}';
    }

}
