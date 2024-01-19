package com.groupeleven.mealmate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.groupeleven.mealmate.Recipe.models.Recipe;

import java.util.ArrayList;
import java.util.HashMap;

public class SharedValues {
    private static SharedValues instance;
    private ArrayList<String> categories = new ArrayList<>();
    private HashMap<String, ArrayList<String>> categorywiseIngredients = new HashMap<>();
    private HashMap<String, String> ingredientsUnits = new HashMap<>();
    private ArrayList<String> instructions;
    private Recipe currentRecipe;
    private ArrayList<Recipe> existingRecipes;
    private int servingSize;
    private boolean editRecipeSelected = false;
    private BottomNavigationView navigationView;
    private String instructionsActivityCaller;

    private SharedValues() {}

    public static SharedValues getInstance() {
        if (null == instance) {
            instance = new SharedValues();
        }
        return instance;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public HashMap<String, ArrayList<String>> getCategorywiseIngredients() {
        return categorywiseIngredients;
    }

    public void setCategorywiseIngredients(HashMap<String, ArrayList<String>> categorywiseIngredients) {
        this.categorywiseIngredients = categorywiseIngredients;
    }

    public HashMap<String, String> getIngredientsUnits() {
        return ingredientsUnits;
    }

    public void setIngredientsUnits(HashMap<String, String> ingredientsUnits) {
        this.ingredientsUnits = ingredientsUnits;
    }

    public ArrayList<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(ArrayList<String> instructions) {
        this.instructions = instructions;
    }

    public void setServingSize(int servingSize) {
        this.servingSize = servingSize;
    }

    public int getServingSize() {
        return servingSize;
    }

    public ArrayList<Recipe> getExistingRecipies() {
        return existingRecipes;
    }

    public void setExistingRecipies(ArrayList<Recipe> existingRecipies) {
        this.existingRecipes = existingRecipies;
    }

    public Recipe getCurrentRecipe() {
        return currentRecipe;
    }

    public void setCurrentRecipe(Recipe currentRecipe) {
        this.currentRecipe = currentRecipe;
    }

    public boolean isEditRecipeSelected() {
        return editRecipeSelected;
    }

    public void setEditRecipeSelected(boolean editRecipeSelected) {
        this.editRecipeSelected = editRecipeSelected;
    }

    public BottomNavigationView getNavigationView() {
        return navigationView;
    }

    public void setNavigationView(BottomNavigationView navigationView) {
        this.navigationView = navigationView;
    }

    public String getInstructionsActivityCaller() {
        return instructionsActivityCaller;
    }

    public void setInstructionsActivityCaller(String instructionsActivityCaller) {
        this.instructionsActivityCaller = instructionsActivityCaller;
    }
}
