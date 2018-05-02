package com.karbyshev.droptobasket.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity (tableName = "dropped_item")
public class DroppedItem implements Serializable{

    @PrimaryKey(autoGenerate = true)
    int id;

    String image;
    String productName;

    public DroppedItem() {
    }

    public DroppedItem(Item item) {
        image = item.image;
        productName = item.productName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
