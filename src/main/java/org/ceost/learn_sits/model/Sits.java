/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ceost.learn_sits.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Alex
 */
public class Sits implements Serializable {

    private SimpleObjectProperty<ArrayList<Patch>> sits = new SimpleObjectProperty();
    private SimpleDoubleProperty relevance = new SimpleDoubleProperty();
    private SimpleStringProperty clasa = new SimpleStringProperty();
    private SimpleBooleanProperty positive = new SimpleBooleanProperty();

    public Sits() {
    }

    public Sits(ArrayList<Patch> sits, String clasa, boolean positive) {
        this.sits.set(sits);
        this.clasa.set(clasa);
        this.positive.set(positive);
    }

    public Sits(ArrayList<Patch> sits, double relevance, String clasa, boolean positive) {
        this.sits.set(sits);
        this.relevance.set(relevance);
        this.clasa.set(clasa);
        this.positive.set(positive);
    }

    public ArrayList<Patch> getSits() {
        return sits.get();
    }

    public void setSits(ArrayList<Patch> sits) {
        this.sits.set(sits);
    }
    
    
    public double getRelevance() {
        return relevance.get();
    }

    public void setRelevance(double relevance) {
        this.relevance.set(relevance);
    }

    public String getClasa() {
        return clasa.get();
    }

    public void setClasa(String clasa) {
        this.clasa.set(clasa);
    }

    public boolean isPositive() {
        return positive.get();
    }

    public void setPositive(boolean pos) {
        this.positive.set(pos);
    }

    public SimpleObjectProperty<ArrayList<Patch>> sitsProperty() {
        return this.sits;
    }

    public SimpleDoubleProperty relevanceProperty() {
        return this.relevance;
    }

    public SimpleStringProperty clasaProperty() {
        return this.clasa;
    }

    public SimpleBooleanProperty positiveProperty() {
        return this.positive;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.sits);
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
        final Sits other = (Sits) obj;
        if (!Objects.equals(this.sits, other.sits)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Sits{" + "sits=" + sits + ", relevance=" + relevance + ", clasa=" + clasa + ", positive=" + positive + '}';
    }

}
