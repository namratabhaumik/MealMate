package com.groupeleven.mealmate.MealPlanner.models;

/**
 * Represents a recipe included in a meal within a meal plan, with additional meal-specific details.
 */
public class MealRecipe {
    private String recipeId;
    private int customServingSize;
    private boolean isGroceryDone;

    /**
     * Default constructor for MealRecipe.
     * Initializes an empty instance.
     */
    public MealRecipe() {}

    /**
     * Parameterized constructor for MealRecipe.
     *
     * @param recipeId           The ID of the recipe.
     * @param customServingSize  The custom serving size for the recipe within the meal.
     * @param isGroceryDone      Indicates if grocery shopping for this recipe is done or not.
     */
    public MealRecipe(String recipeId, int customServingSize, boolean isGroceryDone) {
        this.recipeId = recipeId;
        this.customServingSize = customServingSize;
        this.isGroceryDone = isGroceryDone;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public int getCustomServingSize() {
        return customServingSize;
    }

    public void setCustomServingSize(int customServingSize) {
        this.customServingSize = customServingSize;
    }

    public boolean isGroceryDone() {
        return isGroceryDone;
    }

    public void setGroceryDone(boolean groceryDone) {
        isGroceryDone = groceryDone;
    }
}
