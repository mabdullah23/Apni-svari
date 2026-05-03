package com.example.apni_svari;

import java.util.ArrayList;
import java.util.List;

public class ZsellerProduct {
    private String id;
    private String imageBase64;
    private String carName;
    private String model;
    private String price;
    private String offeredPrice;
    private List<String> extraImages;

    public ZsellerProduct() {
        this.extraImages = new ArrayList<>();
    }

    public ZsellerProduct(String id, String imageBase64, String carName, String model, String price, String offeredPrice) {
        this.id = id;
        this.imageBase64 = imageBase64;
        this.carName = carName;
        this.model = model;
        this.price = price;
        this.offeredPrice = offeredPrice;
        this.extraImages = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getOfferedPrice() {
        return offeredPrice;
    }

    public void setOfferedPrice(String offeredPrice) {
        this.offeredPrice = offeredPrice;
    }

    public List<String> getExtraImages() {
        if (extraImages == null) extraImages = new ArrayList<>();
        return extraImages;
    }

    public void setExtraImages(List<String> extraImages) {
        this.extraImages = extraImages;
    }
}
