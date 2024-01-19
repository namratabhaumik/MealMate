package com.groupeleven.mealmate.InventoryManagement.models;

/**
 * Represents an item in a user's inventory, containing an ingredient name and its quantity.
 */
public class UserInventoryItem {
    private String ingredientName;
    private float quantity;

    /**
     * Default constructor for UserInventoryItem.
     */
    public UserInventoryItem() {}

    /**
     * Parameterized constructor for UserInventoryItem.
     *
     * @param ingredientName The name of the ingredient.
     * @param quantity       The quantity of the ingredient.
     */
    public UserInventoryItem(String ingredientName, float quantity) {
        this.ingredientName = ingredientName;
        this.quantity = quantity;
    }

    /**
     * Retrieves the name of the ingredient.
     *
     * @return The name of the ingredient.
     */
    public String getIngredientName() {
        return ingredientName;
    }

    /**
     * Sets the name of the ingredient.
     *
     * @param ingredientName The name of the ingredient to set.
     */
    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    /**
     * Retrieves the quantity of the ingredient.
     *
     * @return The quantity of the ingredient.
     */
    public double getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of the ingredient.
     *
     * @param quantity The quantity of the ingredient to set.
     */
    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }
}
