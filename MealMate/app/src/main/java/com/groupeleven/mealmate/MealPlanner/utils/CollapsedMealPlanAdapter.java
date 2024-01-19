package com.groupeleven.mealmate.MealPlanner.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.groupeleven.mealmate.MealPlanner.models.MealPlan;
import com.groupeleven.mealmate.R;

import java.util.List;

/**
 * Custom ArrayAdapter to display collapsed meal plan information in a ListView or GridView.
 * Provides a summarized view of meal plan details, allowing the user to view full details in a dialog.
 */
public class CollapsedMealPlanAdapter extends ArrayAdapter<MealPlan> {
    private Context context;

    /**
     * Constructor for the adapter.
     *
     * @param context   The current context.
     * @param mealPlans List of MealPlan objects to be displayed.
     */
    public CollapsedMealPlanAdapter(Context context, List<MealPlan> mealPlans) {
        super(context, 0, mealPlans);
        this.context = context;
    }

    /**
     * Creates and returns the view for each item in the ListView/GridView.
     *
     * @param position    The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent that this view will eventually be attached to.
     * @return The view corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MealPlan mealPlan = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.collapsed_meal_plan_item, parent, false);
        }

        TextView dateTextView = convertView.findViewById(R.id.dateTextView);
        TextView totalMealsTextView = convertView.findViewById(R.id.totalMealsTextView);
        TextView totalRecipesTextView = convertView.findViewById(R.id.totalRecipesTextView);
        Button viewDetailsButton = convertView.findViewById(R.id.viewDetailsButton);

        dateTextView.setText(mealPlan.getMealDate());
        totalMealsTextView.setText("Total Meals - " + mealPlan.getMeals().size());
        totalRecipesTextView.setText("Total Recipes - " + calculateTotalRecipes(mealPlan));

        viewDetailsButton.setOnClickListener(v -> showDetailsDialog(mealPlan));

        return convertView;
    }

    // Helper method to calculate the total number of recipes in a meal plan
    private int calculateTotalRecipes(MealPlan mealPlan) {
        int totalRecipes = 0;
        for (int i = 0; i < mealPlan.getMeals().size(); i++) {
            totalRecipes += mealPlan.getMeals().get(i).getMealRecipes().size();
        }
        return totalRecipes;
    }

    // Method to display detailed information about the meal plan in a dialog
    private void showDetailsDialog(MealPlan mealPlan) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Meal Plan Details");
        builder.setMessage("Meal Plan Date: " + mealPlan.getMealDate() + "\n" +
                "Total Meals: " + mealPlan.getMeals().size() + "\n" +
                "Total Meal Recipes: " + calculateTotalRecipes(mealPlan));

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
}