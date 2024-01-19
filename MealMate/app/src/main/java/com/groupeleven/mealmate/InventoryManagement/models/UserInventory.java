package com.groupeleven.mealmate.InventoryManagement.models;

import java.util.List;

/**
 * Represents a user's inventory containing their ID and a list of inventory items.
 */
public class UserInventory {
    private String userId;
    private List<UserInventoryItem> inventoryItems;

    /**
     * Default constructor for UserInventory.
     */
    public UserInventory() {}

    /**
     * Parameterized constructor for UserInventory.
     *
     * @param userId          The ID of the user.
     * @param inventoryItems  The list of inventory items associated with the user.
     */
    public UserInventory(String userId, List<UserInventoryItem> inventoryItems) {
        this.userId = userId;
        this.inventoryItems = inventoryItems;
    }

    /**
     * Retrieves the user's ID.
     *
     * @return The ID of the user.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user's ID.
     *
     * @param userId The ID of the user to set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Retrieves the list of inventory items associated with the user.
     *
     * @return The list of inventory items.
     */
    public List<UserInventoryItem> getInventoryItems() {
        return inventoryItems;
    }

    /**
     * Sets the list of inventory items associated with the user.
     *
     * @param inventoryItems The list of inventory items to set.
     */
    public void setInventoryItems(List<UserInventoryItem> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }
}
