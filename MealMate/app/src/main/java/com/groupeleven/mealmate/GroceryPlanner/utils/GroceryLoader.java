package com.groupeleven.mealmate.GroceryPlanner.utils;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.groupeleven.mealmate.MealPlanner.models.Meal;
import com.groupeleven.mealmate.MealPlanner.models.MealPlan;
import com.groupeleven.mealmate.MealPlanner.models.MealRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * A class responsible for loading grocery related requirements. For example, loading meal plans
 * with pending grocery etc.
 */
public class GroceryLoader {
    private final FirebaseFirestore db;

    /**
     * Constructor to initialize GroceryLoader with a Firestore instance.
     *
     * @param firestore The Firestore instance.
     */
    public GroceryLoader(FirebaseFirestore firestore) {
        this.db = firestore;
    }

    /**
     * Loads grocery pending meal plans within the specified date range.
     *
     * @param startDate The start date of the range.
     * @param endDate   The end date of the range.
     * @return A Task containing a list of MealPlans with grocery pending recipes.
     */
    public Task<List<MealPlan>> loadGroceryPendingMealPlansWithMatchingDateRange(String startDate,
                                                                                 String endDate, String email) {
        CollectionReference collectionReference = db.collection("MealPlans");
        Query query = collectionReference
                .whereGreaterThanOrEqualTo("mealDate", startDate)
                .whereLessThanOrEqualTo("mealDate", endDate);

        return query.get().continueWith(task -> {
            if (task.isSuccessful()) {
                List<MealPlan> mealPlans = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Deserializing the MealPlan
                    MealPlan mealPlan = document.toObject(MealPlan.class);
                    if (mealPlan != null && mealPlan.getUserEmail().contentEquals(email)) {
                        // Iterating and adding Meals for which
                        List<Meal> filteredMeals = new ArrayList<>();
                        for (Meal meal : mealPlan.getMeals()) {
                            List<MealRecipe> filteredRecipes = new ArrayList<>();
                            // Iterating and adding Meal Recipes for which isGroceryDone = false
                            for (MealRecipe recipe : meal.getMealRecipes()) {
                                if (!recipe.isGroceryDone()) {
                                    filteredRecipes.add(recipe);
                                }
                            }
                            // Add the meal only if it has recipes with groceryDone = false
                            if (!filteredRecipes.isEmpty()) {
                                meal.setMealRecipes(filteredRecipes);
                                filteredMeals.add(meal);
                            }
                        }
                        // Update the meal plan with filtered meals
                        mealPlan.setMeals(filteredMeals);
                        mealPlans.add(mealPlan);
                    }
                }
                // Remove meal plans with 0 filtered meals
                mealPlans.removeIf(plan -> plan.getMeals().isEmpty());
                return mealPlans;
            }
            return null;
        });
    }
}