package com.example.apni_svari.models;

public class Proposal {
	private String id;
	private String carId;
	private String carName;
	private String buyerId;
	private String buyerName;
	private String buyerEmail;
	private String ownerId;
	private double proposedPrice;
	private double originalPrice;
	private String carModel;
	private String carImage;
	private String status;
	private long timestamp;

	public Proposal() {
	}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getCarId() { return carId; }
	public void setCarId(String carId) { this.carId = carId; }

	public String getCarName() { return carName; }
	public void setCarName(String carName) { this.carName = carName; }

	public String getBuyerId() { return buyerId; }
	public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

	public String getBuyerName() { return buyerName; }
	public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

	public String getBuyerEmail() { return buyerEmail; }
	public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }

	public String getOwnerId() { return ownerId; }
	public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

	public double getProposedPrice() { return proposedPrice; }
	public void setProposedPrice(double proposedPrice) { this.proposedPrice = proposedPrice; }

	public double getOriginalPrice() { return originalPrice; }
	public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }

	public String getCarModel() { return carModel; }
	public void setCarModel(String carModel) { this.carModel = carModel; }

	public String getCarImage() { return carImage; }
	public void setCarImage(String carImage) { this.carImage = carImage; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public long getTimestamp() { return timestamp; }
	public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

