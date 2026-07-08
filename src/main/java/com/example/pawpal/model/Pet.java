package com.example.pawpal.model;

import java.time.LocalDateTime;

public class Pet {

    private int petId;
    private int storeId;
    private String petName;
    private String petType;
    private String breed;
    private int age;
    private String gender;
    private boolean vaccinated;
    private String description;
    private String imagePath;
    private double price;
    private String availabilityStatus;
    private LocalDateTime createdAt;
    private boolean featured;


    private String storeName;
    private double storeAverageRating;
    private String storeApprovalStatus;

    public Pet() {
    }

    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getPetType() {
        return petType;
    }

    public void setPetType(String petType) {
        this.petType = petType;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isVaccinated() {
        return vaccinated;
    }

    public void setVaccinated(boolean vaccinated) {
        this.vaccinated = vaccinated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public double getStoreAverageRating() {
        return storeAverageRating;
    }

    public void setStoreAverageRating(double storeAverageRating) {
        this.storeAverageRating = storeAverageRating;
    }

    public String getStoreApprovalStatus() {
        return storeApprovalStatus;
    }

    public void setStoreApprovalStatus(String storeApprovalStatus) {
        this.storeApprovalStatus = storeApprovalStatus;
    }
}
