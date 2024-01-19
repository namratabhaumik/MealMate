package com.groupeleven.mealmate.Recipe.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a recipe in the MealMate application.
 */
public class Recipe {
    private String recipeId;
    private String recipeName;
    private String servingSize;
    private String imageUrl;
    private String recipeDescription;
    private List<Map<String,Object>> ingredients = new ArrayList<>();
    private ArrayList<String> instructions;
    private String userEmail;
    private boolean isVegetarian;
    private String time;

    /**
     * Default constructor for Firebase deserialization.
     */
    public Recipe() {}

    /**
     * Constructor to create a new recipe with basic information.
     *
     * @param recipeName        The name of the recipe.
     * @param servingSize       The serving size of the recipe.
     * @param recipeDescription A brief description of the recipe.
     */
    public Recipe(String recipeName, String servingSize, String recipeDescription) {
        this.recipeName = recipeName;
        this.servingSize = servingSize;
        this.recipeDescription = recipeDescription;
        this.recipeId = generateUniqueRecipeId();
    }

    // Getters and setters for all fields
    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getServingSize() {
        return servingSize;
    }

    public void setServingSize(String servingSize) {
        this.servingSize = servingSize;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRecipeDescription() {
        return recipeDescription;
    }

    public void setRecipeDescription(String recipeDescription) {
        this.recipeDescription = recipeDescription;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public boolean isVegetarian() {
        return isVegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        isVegetarian = vegetarian;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    /**
     * Generates a unique recipe ID using UUID.
     *
     * @return A unique recipe ID.
     */
    private String generateUniqueRecipeId() {
        return "R_" + UUID.randomUUID().toString();
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<Map<String,Object>> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Map<String,Object>> ingredients) {
        this.ingredients = ingredients;
    }

    public ArrayList<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(ArrayList<String> instructions) {
        this.instructions = instructions;
    }
}