package com.groupeleven.mealmate.MealPlanner;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.groupeleven.mealmate.MealPlanner.utils.RecipeSearchAdapter;
import com.groupeleven.mealmate.R;
import com.groupeleven.mealmate.Recipe.models.Recipe;
import com.groupeleven.mealmate.Recipe.utils.RecipeLoader;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Activity responsible for searching and displaying recipes based on user input.
 */
public class RecipeSearchActivity extends AppCompatActivity {
    ListView recipeSearchResultsListView;
    TextView noRecipesTextView;
    RecipeLoader recipeLoader = new RecipeLoader(FirebaseFirestore.getInstance());
    private boolean isVegetarian = false;

    /**
     * Initializes the activity's UI components and sets up listeners.
     *
     * @param savedInstanceState Previously saved state (if exists)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window appWindow = this.getWindow();
        appWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        appWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        appWindow.setStatusBarColor(ContextCompat.getColor(this,R.color.black));

        setContentView(R.layout.activity_recipe_search);
        recipeSearchResultsListView = findViewById(R.id.recipeSearchList);
        noRecipesTextView = findViewById(R.id.noRecipesTextView);

        searchRecipes("", isVegetarian);

        TextInputLayout recipeSearchField = findViewById(R.id.recipeSearchField);
        MaterialSwitch vegSwitch = findViewById(R.id.vegSwitch);
        recipeSearchField.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchRecipes(s.toString(), isVegetarian);
            }
        });

        vegSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isVegetarian = isChecked;
            recipeSearchField.getEditText().setText("");
            searchRecipes("", isVegetarian);
        });

        Button cancelButton = findViewById(R.id.recipe_search_cancel_btn);
        cancelButton.setOnClickListener(view -> finish());
    }

    /**
     * Searches for recipes based on the provided criteria.
     *
     * @param recipeName    Name of the recipe to search for
     * @param isVegetarian  Flag indicating whether to search for vegetarian recipes
     */
    void searchRecipes(String recipeName, boolean isVegetarian) {
        recipeLoader.loadRecipes(recipeName, isVegetarian)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        handleRecipeSearchResults(task.getResult());
                    } else {
                        System.err.println("Error loading recipes: " + task.getException());
                    }
                });
    }

    /**
     * Handles the results of the recipe search and updates the UI accordingly.
     *
     * @param recipes The list of recipes retrieved from the search
     */
    private void handleRecipeSearchResults(List<Recipe> recipes) {
        int isRecipeListVisible = (recipes != null && recipes.size() > 0)
                ? View.VISIBLE : View.GONE;
        int isNoRecipesTxtVisible = (recipes != null && recipes.size() > 0)
                ? View.GONE : View.VISIBLE;

        recipeSearchResultsListView.setVisibility(isRecipeListVisible);
        noRecipesTextView.setVisibility(isNoRecipesTxtVisible);

        if (recipes != null && recipes.size() > 0) {
            RecipeSearchAdapter adapter = new RecipeSearchAdapter(this, recipes);
            recipeSearchResultsListView.setAdapter(adapter);
        }
    }

    /**
     * Handles the selection of a recipe by returning the selected recipe to the caller activity.
     *
     * @param selectedRecipe The recipe selected by the user
     */
    public void handleRecipeSelect(Recipe selectedRecipe) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selectedRecipe", new Gson().toJson(selectedRecipe));
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}