/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ceost.learn_sits.model;

import java.io.Serializable;
import java.util.Objects;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.ceost.learn_sits.MainSceneController;

/**
 *
 * @author Alex
 */
public class Patch implements Serializable {

    private SimpleIntegerProperty xIndex = new SimpleIntegerProperty();
    private SimpleIntegerProperty yIndex = new SimpleIntegerProperty();
    private SimpleIntegerProperty size = new SimpleIntegerProperty();
    private SimpleIntegerProperty parentID = new SimpleIntegerProperty();    

    public Patch() {
    }

    public Patch(int xIndex, int yIndex, int size, int parentID) {
        this.xIndex.set(xIndex);
        this.yIndex.set(yIndex);
        this.size.set(size);
        this.parentID.set(parentID);

    }

    public int getxIndex() {
        return xIndex.get();
    }

    public void setxIndex(int xIndex) {
        this.xIndex.set(xIndex);
    }

    public int getyIndex() {
        return yIndex.get();
    }

    public void setyIndex(int yIndex) {
        this.yIndex.set(yIndex);
    }

    public int getSize() {
        return size.get();
    }

    public void setSize(int size) {
        this.size.set(size);
    }

    public int getParentID() {
        return parentID.get();
    }

    public void setParentID(int parentID) {
        this.parentID.set(parentID);
    }

    public Image getImage() {
        return new WritableImage(
                MainSceneController.images.get(this.getParentID()).getPixelReader(),
                this.getxIndex(),
                this.getyIndex(),
                this.getSize(),
                this.getSize());
    }

    
    public SimpleIntegerProperty xIndexProperty() {
        return this.xIndex;
    }

    public SimpleIntegerProperty yIndexProperty() {
        return this.yIndex;
    }

    public SimpleIntegerProperty sizeProperty() {
        return this.size;
    }

    public SimpleIntegerProperty parentIDProperty() {
        return this.parentID;
    }

    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.xIndex);
        hash = 97 * hash + Objects.hashCode(this.yIndex);
        hash = 97 * hash + Objects.hashCode(this.size);
        hash = 97 * hash + Objects.hashCode(this.parentID);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Patch other = (Patch) obj;
        if (this.xIndex.get() != other.xIndex.get()) {
            return false;
        }
        if (this.yIndex.get() != other.yIndex.get()) {
            return false;
        }
        if (this.size.get() != other.size.get()) {
            return false;
        }
        if (this.parentID.get() != other.parentID.get()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Patch{" + "xIndex=" + xIndex.get() + ", yIndex=" + yIndex.get() + ", size=" + size.get() + ", parentID=" + parentID.get() + '}';
    }

}
