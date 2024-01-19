package com.groupeleven.mealmate.InventoryManagement.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.groupeleven.mealmate.InventoryManagement.models.UserInventory;
import com.groupeleven.mealmate.InventoryManagement.models.UserInventoryItem;

/**
 * Utility class to manage user inventories in Firestore.
 */
public class InventoryLoader {
    private FirebaseFirestore db;

    /**
     * Constructor for InventoryLoader.
     *
     * @param firestore Instance of FirebaseFirestore used for database operations.
     */
    public InventoryLoader(FirebaseFirestore firestore) {
        this.db = firestore;
    }

    /**
     * Uploads the loaded grocery list to the user's inventory.
     *
     * @param userId           The ID of the user whose inventory needs to be updated.
     * @param loadedGroceryList The grocery list to be uploaded to the inventory.
     * @param checkInventory   Flag indicating whether to check existing inventory for updates.
     * @return A {@link Task} that represents the success of the upload operation.
     */
    public Task<Boolean> uploadGroceryListToInventory(String userId,
                                                      Map<String, Float> loadedGroceryList,
                                                      boolean checkInventory) {
        if (loadedGroceryList != null && !loadedGroceryList.isEmpty()) {
            return db.collection("UserInventories")
                    .whereEqualTo("userId", userId)
                    .get()
                    .continueWithTask(task -> {
                        if (task.isSuccessful()) {
                            // Check if inventory for the user exists
                            if (!task.getResult().isEmpty()) {
                                UserInventory existingInventory = task.getResult().getDocuments().
                                        get(0).toObject(UserInventory.class);
                                List<UserInventoryItem> inventoryItems = existingInventory
                                        .getInventoryItems();

                                for (Map.Entry<String, Float> entry : loadedGroceryList.entrySet())
                                {
                                    String ingredientName = entry.getKey();
                                    float quantityToAdd = entry.getValue();
                                    // Check if ingredient already exists in the inventory
                                    boolean found = false;
                                    for (UserInventoryItem existingItem : inventoryItems) {
                                        if (existingItem.getIngredientName()
                                                .equals(ingredientName)) {
                                            found = true;
                                            // Update quantity only if the existing quantity
                                            // is less
                                            if (checkInventory &&
                                                    existingItem.getQuantity() < quantityToAdd) {
                                                existingItem.setQuantity(quantityToAdd);
                                            } else if (!checkInventory){
                                                existingItem.setQuantity((float)
                                                        (existingItem.getQuantity() + quantityToAdd
                                                        ));
                                            }
                                            break;
                                        }
                                    }

                                    // If ingredient not found, add it
                                    if (!found) {
                                        UserInventoryItem newItem = new UserInventoryItem
                                                (ingredientName, quantityToAdd);
                                        inventoryItems.add(newItem);
                                    }
                                }

                                // Update the existing inventory in Firestore
                                return db.collection("UserInventories")
                                        .document(task.getResult().getDocuments().get(0).getId())
                                        .set(existingInventory)
                                        .continueWith(Task::isSuccessful);
                            } else {
                                // User inventory doesn't exist, add a new inventory
                                List<UserInventoryItem> inventoryItems =
                                        mapGroceryListToInventoryItems(loadedGroceryList);
                                UserInventory userInventory = new UserInventory
                                        (userId, inventoryItems);
                                return db.collection("UserInventories")
                                        .add(userInventory)
                                        .continueWith(Task::isSuccessful);
                            }
                        } else {
                            return Tasks.forResult(false);
                        }
                    });
        }
        return Tasks.forResult(false);
    }

    /**
     * Maps a grocery list to inventory items.
     *
     * @param groceryList The grocery list to be mapped to inventory items.
     * @return A list of UserInventoryItem objects created from the grocery list.
     */
    private List<UserInventoryItem> mapGroceryListToInventoryItems(Map<String, Float> groceryList) {
        List<UserInventoryItem> inventoryItems = new ArrayList<>();
        for (Map.Entry<String, Float> entry : groceryList.entrySet()) {
            UserInventoryItem inventoryItem = new UserInventoryItem(entry.getKey(),
                    entry.getValue());
            inventoryItems.add(inventoryItem);
        }
        return inventoryItems;
    }

    /**
     * Updates the inventory quantities based on a recipe's ingredients.
     *
     * @param userId                      The ID of the user whose inventory needs to be updated.
     * @param currentRecipeIngredients    The list of ingredients from the current recipe.
     * @param increment                   Flag indicating whether to increment or decrement quantities.
     * @return A {@link Task} that represents the success of the update operation.
     */
    public Task<Boolean> updateInventoryQuantities(String userId,
                                                   List<Map.Entry<String,
                                                           Float>> currentRecipeIngredients,
                                                   boolean increment) {
        if (currentRecipeIngredients != null && !currentRecipeIngredients.isEmpty()) {
            return db.collection("UserInventories")
                    .whereEqualTo("userId", userId)
                    .get()
                    .continueWithTask(task -> {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                UserInventory existingInventory = task.getResult().getDocuments()
                                        .get(0).toObject(UserInventory.class);
                                List<UserInventoryItem> inventoryItems = existingInventory
                                        .getInventoryItems();

                                for (Map.Entry<String, Float> entry : currentRecipeIngredients) {
                                    String ingredientName = entry.getKey();
                                    float quantityToUpdate = entry.getValue();

                                    // Finding the matching inventory item
                                    for (UserInventoryItem existingItem : inventoryItems) {
                                        if (existingItem.getIngredientName().equals(ingredientName)) {
                                            float currentQuantity = (float) existingItem.getQuantity();
                                            // Update quantity based on increment parameter
                                            float updatedQuantity = increment ?
                                                    currentQuantity + quantityToUpdate :
                                                    currentQuantity - quantityToUpdate;
                                            // Ensure the quantity doesn't go negative
                                            if (updatedQuantity >= 0) {
                                                existingItem.setQuantity(updatedQuantity);
                                            } else {
                                                // // Set to 0 if negative quantity
                                                existingItem.setQuantity(0);
                                            }
                                            break;
                                        }
                                    }
                                }

                                // Update the existing inventory in Firestore
                                return db.collection("UserInventories")
                                        .document(task.getResult().getDocuments().get(0).getId())
                                        .set(existingInventory)
                                        .continueWith(Task::isSuccessful);
                            } else {
                                return Tasks.forResult(false);
                            }
                        } else {
                            return Tasks.forResult(false);
                        }
                    });
        }
        return Tasks.forResult(false);
    }
}