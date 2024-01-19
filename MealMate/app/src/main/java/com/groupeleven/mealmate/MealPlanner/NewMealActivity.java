package com.groupeleven.mealmate.MealPlanner;

import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.groupeleven.mealmate.Common.utils.ToastUtils;
import com.groupeleven.mealmate.MealPlanner.models.Meal;
import com.groupeleven.mealmate.MealPlanner.models.MealPlan;
import com.groupeleven.mealmate.MealPlanner.models.MealRecipe;
import com.groupeleven.mealmate.MealPlanner.utils.MealPlanLoader;
import com.groupeleven.mealmate.MealPlanner.utils.RecipeListAdapter;
import com.groupeleven.mealmate.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;


import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.MenuRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.groupeleven.mealmate.Recipe.models.Recipe;

import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;

/**
 * Activity responsible for creating a new meal plan by selecting recipes and defining meal type.
 */
public class NewMealActivity extends AppCompatActivity {

    private static final String RECIPE_PREFERENCES = "RecipePreferences";
    private static final String SELECTED_RECIPES_KEY = "selectedRecipes";
    private List<Recipe> selectedRecipes = new ArrayList<>();
    private RecipeListAdapter recipeListAdapter;
    private String selectedDateStr;
    private TextView selectedMealType;
    MealPlanLoader mealPlanLoader;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meal);

        Window appWindow = this.getWindow();
        appWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        appWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        appWindow.setStatusBarColor(ContextCompat.getColor(this,R.color.black));

        selectedDateStr = getIntent().getStringExtra("selectedDate");

        mealPlanLoader = new MealPlanLoader(FirebaseFirestore.getInstance());

        loadSelectedRecipes();

        selectedMealType = findViewById(R.id.mealTypeTxt);

        Button mealTypeBtn = findViewById(R.id.btnAddMealType);
        mealTypeBtn.setOnClickListener(v -> showMealTypeMenu(v, R.menu.mealtype_menu));
        Button searchRecipeButton = findViewById(R.id.btnSearchRecipe);
        searchRecipeButton.setOnClickListener(view -> {
            openRecipeSearchActivity();
        });

        Button saveMealPlanButton = findViewById(R.id.btnSaveMealPlan);
        saveMealPlanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMealPlanOnFirebase();
            }
        });

        Button cancelButton = findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearRecipePreferences();
                finish();
            }
        });
    }

    /**
     * Saves the meal plan on Firebase after selecting recipes and defining the meal type.
     */
    private void saveMealPlanOnFirebase() {
        String mealType = selectedMealType.getText().toString();

        if (mealType.equals("-")) {
            ToastUtils.showLongToast(this, "Please choose a meal type!");
            return;
        }

        if (selectedRecipes.isEmpty()) {
            ToastUtils.showLongToast(this, "Please select at least one recipe.");
            return;
        }

        MealPlan mealPlan = new MealPlan();
        mealPlan.setMealDate(selectedDateStr);
        mealPlan.setUserEmail(currentUser.getEmail());

        List<Meal> meals = new ArrayList<>();
        Meal meal = new Meal(Meal.MealType.fromName(mealType), createMealRecipes(selectedRecipes));
        meals.add(meal);

        mealPlan.setMeals(meals);

        mealPlanLoader.uploadMealPlan(mealPlan)
                .addOnSuccessListener(result -> {
                    if (result) {
                        ToastUtils.showLongToast(this, "Meal Plan uploaded!");
                        clearRecipePreferences();
                        finish();
                    } else {
                        ToastUtils.showLongToast(this, "Meal Plan upload failed!");
                    }
                })
                .addOnFailureListener(e -> {
                    ToastUtils.showLongToast(this, "Meal Plan upload failed!");
                });
    }


    private List<MealRecipe> createMealRecipes(List<Recipe> recipes) {
        List<MealRecipe> mealRecipes = new ArrayList<>();
        for (Recipe recipe : recipes) {
            MealRecipe mealRecipe = new MealRecipe(
                    recipe.getRecipeId(),
                    Integer.parseInt(recipe.getServingSize()),
                    false
            );
            mealRecipes.add(mealRecipe);
        }
        return mealRecipes;
    }

    /**
     * Loads selected recipes from shared preferences.
     */
    private void loadSelectedRecipes() {
        SharedPreferences preferences = getSharedPreferences(RECIPE_PREFERENCES, Context.MODE_PRIVATE);
        String recipesJson = preferences.getString(SELECTED_RECIPES_KEY, "");
        if (!recipesJson.isEmpty()) {
            Type listType = new TypeToken<List<Recipe>>() {}.getType();
            selectedRecipes = new Gson().fromJson(recipesJson, listType);
        }
        updateUIWithSelectedRecipe();
    }

    /**
     * Saves the selected recipes into shared preferences.
     */
    private void saveSelectedRecipes() {
        SharedPreferences preferences = getSharedPreferences(RECIPE_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String recipesJson = new Gson().toJson(selectedRecipes);

        editor.putString(SELECTED_RECIPES_KEY, recipesJson);
        editor.apply();
    }

    /**
     * Clears the recipe preferences stored in SharedPreferences.
     */
    private void clearRecipePreferences() {
        SharedPreferences preferences = getSharedPreferences(RECIPE_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(SELECTED_RECIPES_KEY);
        editor.apply();
    }

    /**
     * Removes a recipe from the list of selected recipes and SharedPreferences.
     *
     * @param recipe The recipe to be removed.
     */
    private void removeRecipeFromListAndPreferences(Recipe recipe) {
        selectedRecipes.remove(recipe);
        saveSelectedRecipes();
        updateUIWithSelectedRecipe();
    }

    /**
     * Opens the RecipeSearchActivity to search for recipes.
     */
    private void openRecipeSearchActivity() {
        Intent intent = new Intent(this, RecipeSearchActivity.class);
        startForSearchedRecipeResult.launch(intent);
    }

    /**
     * Handles the result from RecipeSearchActivity after searching for recipes.
     */
    private final ActivityResultLauncher<Intent> startForSearchedRecipeResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String recipeJson = data.getStringExtra("selectedRecipe");
                        Recipe selectedRecipe = new Gson().fromJson(recipeJson, Recipe.class);

                        selectedRecipes.add(selectedRecipe);
                        saveSelectedRecipes();
                        updateUIWithSelectedRecipe();
                    }
                }
            }
    );

    /**
     * Updates the UI with the selected recipes.
     */
    private void updateUIWithSelectedRecipe() {
        ListView newRecipesList = findViewById(R.id.newRecipesList);
        recipeListAdapter = new RecipeListAdapter(this, selectedRecipes,
                this::removeRecipeFromListAndPreferences,
                this::showRecipeEditDialog);
        newRecipesList.setAdapter(recipeListAdapter);
    }

    /**
     * Displays a dialog to edit a recipe's serving size.
     *
     * @param recipe The recipe to be edited.
     */
    private void showRecipeEditDialog(Recipe recipe) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_recipe_dialog, null);
        builder.setView(dialogView);

        EditText servingSizeEditText = dialogView.findViewById(R.id.editServingSize);
        Button updateButton = dialogView.findViewById(R.id.btnUpdate);
        Button closeButton = dialogView.findViewById(R.id.btnClose);

        servingSizeEditText.setText(String.valueOf(recipe.getServingSize()));

        AlertDialog alertDialog = builder.create();
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recipe.setServingSize(servingSizeEditText.getText().toString());
                saveSelectedRecipes();
                updateUIWithSelectedRecipe();
                alertDialog.dismiss();
            }
        });
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    /**
     * Displays the meal type selection menu.
     *
     * @param v       The view triggering the menu display.
     * @param menuRes The menu resource ID.
     */
    private void showMealTypeMenu(View v, @MenuRes int menuRes) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(menuRes, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                selectedMealType.setText(menuItem.getTitle());
                return true;
            }
        });

        popup.show();
    }
}