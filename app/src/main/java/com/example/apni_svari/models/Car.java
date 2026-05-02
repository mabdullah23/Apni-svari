package com.example.apni_svari.models;

public class Car {
    private String id;
    private String name;
    private String ownerId;
    private String ownerName;
    private String ownerPhone;
    private double price;
    private String imageUrl;
    private String imageBase64;
    private String model;

    public Car() { }

    public Car(String id, String name, String ownerId, double price, String imageUrl, String model) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.price = price;
        this.imageUrl = imageUrl;
        this.model = model;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}

