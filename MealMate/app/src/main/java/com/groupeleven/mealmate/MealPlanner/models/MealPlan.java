package com.groupeleven.mealmate.MealPlanner.models;

import java.util.List;
import java.util.UUID;

/**
 * Represents a meal plan containing meals scheduled for a specific date for a user.
 */
public class MealPlan {
    private String mealPlanId;
    private String mealDate;
    private List<Meal> meals;
    private String userEmail;

    /**
     * Default constructor for MealPlan.
     * Initializes a unique ID for the meal plan.
     */
    public MealPlan() {
        this.mealPlanId = generateUniqueMealPlanId();
    }

    /**
     * Parameterized constructor for MealPlan.
     * Initializes a meal plan with provided parameters and generates a unique ID for the meal plan.
     *
     * @param mealDate   The date for the meal plan.
     * @param meals      The list of meals scheduled for the meal plan.
     * @param userEmail  The email of the user for whom the meal plan is created.
     */
    public MealPlan(String mealDate, List<Meal> meals, String userEmail) {
        this.mealDate = mealDate;
        this.meals = meals;
        this.userEmail = userEmail;
        this.mealPlanId = generateUniqueMealPlanId();
    }

    public String getMealDate() {
        return mealDate;
    }

    public void setMealDate(String mealDate) {
        this.mealDate = mealDate;
    }

    public List<Meal> getMeals() {
        return meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Generates a unique ID for the meal plan.
     *
     * @return A unique ID for the meal plan.
     */
    private String generateUniqueMealPlanId() {
        return "MP_" + UUID.randomUUID().toString();
    }

    public String getMealPlanId() {
        return mealPlanId;
    }

    public void setMealPlanId(String mealPlanId) {
        this.mealPlanId = mealPlanId;
    }
}