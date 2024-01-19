package com.groupeleven.mealmate.MealPlanner.utils;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.groupeleven.mealmate.FireBaseEntities;
import com.groupeleven.mealmate.MealPlanner.models.Meal;
import com.groupeleven.mealmate.MealPlanner.models.MealPlan;
import com.groupeleven.mealmate.MealPlanner.models.MealRecipe;
import com.groupeleven.mealmate.Recipe.models.Recipe;
import com.groupeleven.mealmate.Recipe.utils.RecipeLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MealPlanLoader {
    private FirebaseFirestore db;
    private RecipeLoader recipeLoader;

    /**
     * Constructor for MealPlanLoader class that allows injecting a custom Firestore instance.
     *
     * @param firestore The Firestore instance to be used.
     */
    public MealPlanLoader(FirebaseFirestore firestore){
        this.db = firestore;
        this.recipeLoader = new RecipeLoader(db);
    }

    /**
     * Loads a MealPlan based on the matching user email and date from the Firestore database.
     *
     * @param userEmail The email of the user for whom the meal plan is to be loaded.
     * @param date The date for which the meal plan is to be loaded.
     * @return A Task containing the loaded MealPlan. Null if not found or unsuccessful.
     */
    public Task<MealPlan> loadMealPlanWithMatchingUserAndDate(String userEmail, String date) {
        CollectionReference collectionReference = db.collection(FireBaseEntities
                .MEAL_PLAN_COLLECTION_NAME);
        Query query = collectionReference
                .whereEqualTo(FireBaseEntities.MEAL_PLAN_USER_EMAIL, userEmail)
                .whereEqualTo(FireBaseEntities.MEAL_DATE, date);
        return query.get().continueWith(task -> {
                    if (task.isSuccessful() && !task.getResult().getDocuments().isEmpty()) {
                        return task.getResult().getDocuments().get(0).toObject(MealPlan.class);
                    }
                    return null;
                });
    }

    /**
     * Loads a recipe for a given recipe ID from the Firestore database.
     *
     * @param recipeId - The unique identifier of the recipe to load.
     * @return {Task<Recipe>} - A task representing the asynchronous operation to load the recipe.
     */
    public Task<Recipe> loadRecipe(String recipeId) {
        CollectionReference mealPlansCollection = db.collection(FireBaseEntities
                .RECIPES);
        Query query = mealPlansCollection.whereEqualTo("recipeId", recipeId);
        return query.get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().getDocuments().get(0).toObject(Recipe.class);
                    }
                    return null;
                });
    }

    /**
     * Uploads a meal plan to the Firestore database. Checks for existing meal plans,
     * and either updates the existing one or adds a new one.
     *
     * @param mealPlan The meal plan to be uploaded.
     * @return A {@link Task} indicating the success or failure of the operation.
     */
    public Task<Boolean> uploadMealPlan(MealPlan mealPlan) {
        Task<MealPlan> existingMealPlanTask = loadMealPlanWithMatchingUserAndDate(
                mealPlan.getUserEmail(), mealPlan.getMealDate());

        return existingMealPlanTask.continueWithTask(task -> {
            if (task.isSuccessful()) {
                MealPlan existingMealPlan = task.getResult();
                if (existingMealPlan != null) {
                    // If an existing meal plan is found, update it
                    return updateExistingMealPlan(existingMealPlan, mealPlan.getMeals().get(0));
                } else {
                    // If no existing meal plan is found, add a new one
                    return addNewMealPlan(mealPlan);
                }
            }
            return Tasks.forResult(false);
        }).continueWith(task -> task.getResult().booleanValue());
    }

    /**
     * Adds a new meal plan to the Firestore database.
     *
     * @param mealPlan The meal plan to be added.
     * @return A {@link Task} indicating the success or failure of the operation.
     */
    private Task<Boolean> addNewMealPlan(MealPlan mealPlan) {
        return db.collection(FireBaseEntities.MEAL_PLAN_COLLECTION_NAME)
                .add(mealPlan)
                .continueWith(task -> task.isSuccessful());
    }

    /**
     * Updates meal recipes or adds new meal to an existing meal plan on the Firestore database.
     *
     * @param existingMealPlan The existing meal plan to update.
     * @param newMeal The new meal to be added or used for updating an existing meal.
     * @return A {@link Task} indicating the success or failure of the operation.
     */
    private Task<Boolean> updateExistingMealPlan(MealPlan existingMealPlan, Meal newMeal) {
        String existingMealPlanId = existingMealPlan.getMealPlanId();

        // Reference to the Firestore collection "MealPlans" with matching "mealPlanId"
        CollectionReference mealPlansCollection = db.collection(FireBaseEntities
                .MEAL_PLAN_COLLECTION_NAME);
        Query query = mealPlansCollection.whereEqualTo(FireBaseEntities
                .MEAL_PLAN_ID, existingMealPlanId);

        return query.get().continueWithTask(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                // Check if the query result is not empty
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    // Get the document snapshot of the first result
                    DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);

                    // Iterate through each existing meal to find a match based on meal type
                    List<Meal> existingMeals = existingMealPlan.getMeals();
                    for (Meal existingMeal : existingMeals) {
                        if (existingMeal.getMealType().getName().equals(
                                newMeal.getMealType().getName())) {
                            // Combine the existing meal recipes with the new meal recipes
                            List<MealRecipe> combinedMealRecipes = new ArrayList<>
                                    (existingMeal.getMealRecipes());
                            combinedMealRecipes.addAll(newMeal.getMealRecipes());
                            existingMeal.setMealRecipes(combinedMealRecipes);

                            // Update the Firestore document with the modified meal plan
                            documentSnapshot.getReference().set(existingMealPlan);
                            return Tasks.forResult(true);
                        }
                    }

                    // If no matching meal type is found, add the new meal to the list
                    // of existing meals
                    existingMeals.add(newMeal);

                    // Update the Firestore document with the modified meal plan
                    documentSnapshot.getReference().set(existingMealPlan);
                    return Tasks.forResult(true);
                }
            }
            return Tasks.forResult(false);
        }).continueWith(task -> task.getResult().booleanValue());
    }

    /**
     * Updates an existing MealPlan in the Firestore Database with the provided modified MealPlan.
     *
     * @param modifiedMealPlan The modified MealPlan to be updated.
     * @return A Task<Boolean> indicating the success or failure of the update operation.
     */
    public Task<Boolean> updateExistingMealPlan(MealPlan modifiedMealPlan) {
        CollectionReference mealPlansCollection = db.collection(FireBaseEntities
                .MEAL_PLAN_COLLECTION_NAME);
        Query query = mealPlansCollection.whereEqualTo(FireBaseEntities.MEAL_PLAN_ID
                , modifiedMealPlan.getMealPlanId());
        return query.get().continueWithTask(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                    // Update the Firestore document with the modified meal plan
                    documentSnapshot.getReference().set(modifiedMealPlan);
                    return Tasks.forResult(true);
                }
            }
            return Tasks.forResult(false);
        }).continueWith(task -> task.getResult().booleanValue());
    }

    /**
     * Calculates the total ingredients required for all meal recipes in a given list of MealPlans.
     *
     * @param mealPlans The MealPlan list containing the list of meals and their recipes.
     * @return A Map containing the unique ingredients and their total quantities required for the
     * MealPlan.
     */
    public Task<Map<String, Float>> computeIngredientsForMealPlans(List<MealPlan> mealPlans) {
        List<String> recipeIds = new ArrayList<>();

        // Collect all recipe IDs from meal plans
        for (MealPlan plan : mealPlans) {
            for (Meal meal : plan.getMeals()) {
                for (MealRecipe recipe : meal.getMealRecipes()) {
                    recipeIds.add(recipe.getRecipeId());
                }
            }
        }

        // Fetch recipes in bulk based on collected recipe IDs
        return recipeLoader.fetchRecipesWithMatchingIds(recipeIds).continueWith(task -> {
            if (task.isSuccessful()) {
                Map<String, Float> ingredientQuantities = new HashMap<>();
                List<Recipe> recipes = task.getResult();
                for (Recipe recipe : recipes) {
                    List<Map<String,Object>> ingredients = recipe.getIngredients();
                    // Getting default serving size of recipe
                    int defaultServingSize = Integer.parseInt(recipe.getServingSize());
                    // Getting custom serving size of meal recipe
                    int customServingSize = getCustomServingSizeForRecipe(recipe.getRecipeId(),
                            mealPlans);
                    // Iterating over the recipe ingredients to adjust updated quantity according
                    // to ratio
                    for (Map<String, Object> ingredient : ingredients) {
                        String ingredientName = (String) ingredient.get("name");
                        Long standardQuantity = (Long) ingredient.get("quantity");

                        // Calculate ratio between custom and default serving sizes
                        float servingSizeRatio = (float) customServingSize / defaultServingSize;

                        // Adjust quantity to standard serving size (1 unit)
                        float adjustedQuantity = standardQuantity * servingSizeRatio;

                        // Update ingredient quantities in the map
                        Float existingQuantity = ingredientQuantities.get(ingredientName);

                        // Adding adjusted quantity to already existing quantity for an already
                        // existing ingredient.
                        if (existingQuantity != null) {
                            float updatedQuantity = existingQuantity + adjustedQuantity;
                            ingredientQuantities.put(ingredientName, updatedQuantity);
                        } else {
                            ingredientQuantities.put(ingredientName, adjustedQuantity);
                        }
                    }
                }
                return ingredientQuantities;
            }
            return Collections.emptyMap();
        });
    }

    /**
     * Retrieves the custom serving size for a specific recipe ID from the provided list of
     * MealPlans.
     * @param recipeId   The ID of the recipe to find the custom serving size for.
     * @param mealPlans  The list of MealPlans containing meal recipes.
     * @return The custom serving size for the given recipe ID. If not found, returns 0.
     */
    private int getCustomServingSizeForRecipe(String recipeId, List<MealPlan> mealPlans) {
        for (MealPlan plan : mealPlans) {
            for (Meal meal : plan.getMeals()) {
                for (MealRecipe recipe : meal.getMealRecipes()) {
                    if (recipe.getRecipeId().equals(recipeId)) {
                        return recipe.getCustomServingSize();
                    }
                }
            }
        }
        return 1;
    }

    /**
     * Deletes a meal plan from the Firestore Database based on the provided meal plan ID.
     *
     * @param mealPlanId The ID of the meal plan to be deleted.
     * @return A Task<Boolean> indicating the success or failure of the delete operation.
     */
    public Task<Boolean> deleteMealPlan(String mealPlanId) {
        CollectionReference mealPlansCollection = db.collection(FireBaseEntities
                .MEAL_PLAN_COLLECTION_NAME);
        Query query = mealPlansCollection.whereEqualTo(FireBaseEntities.MEAL_PLAN_ID, mealPlanId);
        return query.get().continueWithTask(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                    documentSnapshot.getReference().delete();
                    return Tasks.forResult(true);
                }
            }
            return Tasks.forResult(false);
        });
    }
}