package com.groupeleven.mealmate.Ingredients;

/**
 * The `IngredientsData` class represents the data structure for individual ingredients
 * in the MealMate application. It includes properties such as the ingredient image URL,
 * name, quantity, number of cooks, category, and unit. The class provides getter and setter
 * methods for accessing and modifying these properties.
 *
 * This class follows a simple POJO (Plain Old Java Object) pattern, providing a default
 * constructor and accessor methods for each property.
 */
public class IngredientsData {
    private String ingredientImg;
    private String ingredientName;
    private int ingredientQty;
    private int noOfCook;
    private String ingredientCategory;
    private String unit;

    /**
     * Default constructor for the IngredientsData class.
     * Creates an instance of the class with default values.
     */
    public IngredientsData() {
    }

    public String getIngredientImg() {
        return ingredientImg;
    }

    public void setIngredientImg(String ingredientImg) {
        this.ingredientImg = ingredientImg;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public int getIngredientQty() {
        return ingredientQty;
    }

    public void setIngredientQty(int ingredientQty) {
        this.ingredientQty = ingredientQty;
    }

    public int getNoOfCook() {
        return noOfCook;
    }

    public void setNoOfCook(int noOfCook) {
        this.noOfCook = noOfCook;
    }

    public String getIngredientCategory() {
        return ingredientCategory;
    }

    public void setIngredientCategory(String ingredientCategory) {
        this.ingredientCategory = ingredientCategory;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
