package com.groupeleven.mealmate;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UploadRecipeSingleton {
    private static UploadRecipeSingleton instance;
    private String recipeName = "";
    private String recipeDesc = "";
    private boolean isVegeterian = true;
    private int servingSize;
    private int timeTakenToPrepare;
    private List<Map<String,Object>> ingredients = new ArrayList<>();
    private ArrayList<String> instructions = new ArrayList<>();
    private Uri imageUri;
    private UploadRecipeSingleton() {}

    public static UploadRecipeSingleton getInstance() {
        if (null == instance) {
            instance = new UploadRecipeSingleton();
        }
        return instance;
    }

    public void delete() {
        instance = new UploadRecipeSingleton();
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getRecipeDesc() {
        return recipeDesc;
    }

    public void setRecipeDesc(String recipeDesc) {
        this.recipeDesc = recipeDesc;
    }

    public boolean isVegeterian() {
        return isVegeterian;
    }

    public void setVegeterian(boolean vegeterian) {
        isVegeterian = vegeterian;
    }

    public List<Map<String, Object>> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Map<String, Object>> ingredients) {
        this.ingredients = ingredients;
    }

    public ArrayList<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(ArrayList<String> instructions) {
        this.instructions = instructions;
    }

    public int getServingSize() {
        return servingSize;
    }

    public void setServingSize(int servingSize) {
        this.servingSize = servingSize;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public int getTimeTakenToPrepare() {
        return timeTakenToPrepare;
    }

    public void setTimeTakenToPrepare(int timeTakenToPrepare) {
        this.timeTakenToPrepare = timeTakenToPrepare;
    }
}
