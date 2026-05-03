package com.example.apni_svari.models;

public class HistoryCar {
    private String id;
    private String carId;
    private String buyerId;
    private String buyerName;
    private String ownerId;
    private String ownerName;
    private String ownerPhone;
    private String carName;
    private String model;
    private double price;
    private double acceptedPrice;
    private String imageBase64;
    private String imageUrl;
    private long acceptedAt;

    public HistoryCar() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }

    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }

    public String getCarName() { return carName; }
    public void setCarName(String carName) { this.carName = carName; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getAcceptedPrice() { return acceptedPrice; }
    public void setAcceptedPrice(double acceptedPrice) { this.acceptedPrice = acceptedPrice; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(long acceptedAt) { this.acceptedAt = acceptedAt; }
}

