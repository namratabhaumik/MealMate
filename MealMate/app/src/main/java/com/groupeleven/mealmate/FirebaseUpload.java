package com.groupeleven.mealmate;

public class FirebaseUpload {

    private String name;
    private String imageUrl;

    // For us
    public FirebaseUpload(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    // Required for Firebase
    public FirebaseUpload() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
