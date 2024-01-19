package com.groupeleven.mealmate.MealPlanner.models;

import java.util.List;
import java.util.UUID;

/**
 * Represents a meal with its type and associated recipes.
 */
public class Meal {

    /**
     * Enum defining various types of meals.
     */
    public enum MealType {
        BREAKFAST("Breakfast"), LUNCH("Lunch"), DINNER("Dinner"),
        MORNING_SNACK("Morning Snack"), EVENING_SNACK("Evening Snack");
        private final String name;
        MealType(String name) {
            this.name = name;
        }

        /**
         * Retrieves the name of the meal type.
         *
         * @return The name of the meal type.
         */
        public String getName() {
            return name;
        }

        /**
         * Retrieves a MealType based on its name.
         *
         * @param name The name of the meal type.
         * @return The MealType corresponding to the given name, null if not found.
         */
        public static MealType fromName(String name) {
            for (MealType mealType : MealType.values()) {
                if (mealType.getName().equalsIgnoreCase(name)) {
                    return mealType;
                }
            }
            return null;
        }
    }
    private MealType mealType;
    private List<MealRecipe> mealRecipes;
    private String mealId;

    /**
     * Default constructor for Meal.
     */
    public Meal(){
        this.mealId = generateUniqueMealId();
    }

    /**
     * Parameterized constructor for Meal.
     *
     * @param mealType    The type of the meal.
     * @param mealRecipes The list of recipes associated with the meal.
     */
    public Meal(MealType mealType, List<MealRecipe> mealRecipes) {
        this.mealType = mealType;
        this.mealRecipes = mealRecipes;
        this.mealId = generateUniqueMealId();
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public List<MealRecipe> getMealRecipes() {
        return mealRecipes;
    }

    public void setMealRecipes(List<MealRecipe> mealRecipes) {
        this.mealRecipes = mealRecipes;
    }

    /**
     * Generates a unique ID for the meal.
     *
     * @return A unique ID for the meal.
     */
    private String generateUniqueMealId() {
        return "M_" + UUID.randomUUID().toString();
    }

    public String getMealId() {
        return mealId;
    }

    public void setMealId(String mealId) {
        this.mealId = mealId;
    }
}