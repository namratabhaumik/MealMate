package com.groupeleven.mealmate.MealPlanner.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.groupeleven.mealmate.R;
import com.groupeleven.mealmate.Recipe.models.Recipe;

import java.util.List;

/**
 * Custom ArrayAdapter to display a list of recipes in a ListView or Spinner.
 * Each item in the list contains buttons to edit or delete the recipe.
 */
public class RecipeListAdapter extends ArrayAdapter<Recipe> {

    private final LayoutInflater inflater;
    private final RemoveRecipeListener removeRecipeListener;
    private final EditRecipeListener editRecipeListener;

    /**
     * Constructs the RecipeListAdapter.
     *
     * @param context              The context of the adapter.
     * @param recipes              The list of recipes to display.
     * @param removeRecipeListener Listener for handling recipe removal events.
     * @param editRecipeListener   Listener for handling recipe editing events.
     */
    public RecipeListAdapter(Context context, List<Recipe> recipes,
                             RemoveRecipeListener removeRecipeListener,
                             EditRecipeListener editRecipeListener) {
        super(context, 0, recipes);
        inflater = LayoutInflater.from(context);
        this.removeRecipeListener = removeRecipeListener;
        this.editRecipeListener = editRecipeListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = inflater.inflate(R.layout.recipe_list_item, parent, false);
        }

        Recipe recipe = getItem(position);

        TextView nameTextView = itemView.findViewById(R.id.recipeItemName);
        TextView servingTextView = itemView.findViewById(R.id.recipeItemServing);
        Button editButton = itemView.findViewById(R.id.btnEdit);
        Button deleteButton = itemView.findViewById(R.id.btnDelete);

        nameTextView.setText(recipe.getRecipeName());
        servingTextView.setText(String.valueOf(recipe.getServingSize()));

        editButton.setOnClickListener(v -> {
            editRecipeListener.onEditRecipe(recipe);
        });

        deleteButton.setOnClickListener(v -> {
            removeRecipeListener.onRemoveRecipe(recipe);
        });

        return itemView;
    }

    /**
     * Listener interface to handle recipe removal events.
     */
    public interface RemoveRecipeListener {
        void onRemoveRecipe(Recipe recipe);
    }

    /**
     * Listener interface to handle recipe editing events.
     */
    public interface EditRecipeListener {
        void onEditRecipe(Recipe recipe);
    }
}