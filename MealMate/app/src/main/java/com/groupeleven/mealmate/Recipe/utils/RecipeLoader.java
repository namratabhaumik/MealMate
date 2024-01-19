package com.groupeleven.mealmate.Recipe.utils;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.groupeleven.mealmate.FireBaseEntities;
import com.groupeleven.mealmate.Recipe.models.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading recipes from Firestore.
 */
public class RecipeLoader {
    private final FirebaseFirestore db;

    public RecipeLoader(FirebaseFirestore firestore) {
        this.db = firestore;
    }

    /**
     * Loads recipes based on the provided recipe name and vegetarian flag.
     *
     * @param recipeName    The name of the recipe to search for. Can be empty or null to fetch all.
     * @param isVegetarian  Flag indicating whether to fetch vegetarian recipes.
     * @return A {@link Task} that asynchronously returns a list of matching recipes.
     */
    public Task<List<Recipe>> loadRecipes(String recipeName, boolean isVegetarian) {
        CollectionReference recipesCollectionRef = db.collection(FireBaseEntities.RECIPES);
        Query isVegetarianQuery = recipesCollectionRef.whereEqualTo(FireBaseEntities.IS_VEG,
                isVegetarian);
        Task<QuerySnapshot> loadRecipesTask = isVegetarianQuery.get();
        return loadRecipesTask.continueWith(task -> {
                    if (task.isSuccessful()) {
                        List<Recipe> matchingRecipes = new ArrayList<>();
                        List<DocumentSnapshot> fetchedRecipesDocuments = task.getResult().
                                getDocuments();
                        for (DocumentSnapshot document : fetchedRecipesDocuments) {
                            Recipe recipe = document.toObject(Recipe.class);
                            if (isRecipeNameMatching(recipe, recipeName)) {
                                matchingRecipes.add(recipe);
                            }
                        }
                        return matchingRecipes;
                    }
                    return null;
                });
    }

    /**
     * Loads recipes owned by a specific user.
     *
     * @param userEmail The email of the user to fetch recipes for.
     * @return A {@link Task} that asynchronously returns a list of recipes owned by the user.
     */
    public Task<List<Recipe>> loadUserRecipes(String userEmail) {
        CollectionReference recipesCollectionRef = db.collection(FireBaseEntities.RECIPES);
        Query userRecipesQuery = recipesCollectionRef
                .whereEqualTo(FireBaseEntities.R_OWNER_MAIL, userEmail);
        Task<QuerySnapshot> userRecipesTask = userRecipesQuery.get();
        return userRecipesTask.continueWith(task -> {
                    if (task.isSuccessful()) {
                        List<Recipe> userRecipesList = new ArrayList<>();
                        List<DocumentSnapshot> userRecipesDocuments =
                                task.getResult().getDocuments();
                        for (DocumentSnapshot document : userRecipesDocuments) {
                            userRecipesList.add(document.toObject(Recipe.class));
                        }
                        return userRecipesList;
                    }
                    return null;
                });
    }

    /**
     * Fetches recipes that match the provided recipe IDs.
     *
     * @param recipeIds A list of recipe IDs to match in the database.
     * @return A {@link Task} that asynchronously returns a list of recipes matching the provided
     * IDs. If the task fails, an empty list is returned.
     */
    public Task<List<Recipe>> fetchRecipesWithMatchingIds(List<String> recipeIds) {
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        for (String recipeId : recipeIds) {
            CollectionReference recipesCollectionRef = db.collection(FireBaseEntities.RECIPES);
            Query recipeIdQuery = recipesCollectionRef
                    .whereEqualTo(FireBaseEntities.R_ID, recipeId);
            Task<QuerySnapshot> task = recipeIdQuery.get();
            tasks.add(task);
        }

        Task<List<Task<?>>> taskForListOfRecipeTasks = Tasks.whenAllComplete(tasks);
        return taskForListOfRecipeTasks.continueWith(task -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (Task<QuerySnapshot> queryTask : tasks) {
                        QuerySnapshot result = queryTask.getResult();
                        if (result != null && !result.isEmpty()) {
                            recipes.addAll(result.toObjects(Recipe.class));
                        }
                    }
                    return recipes;
                });
    }

    /**
     * Checks if the provided recipe's name matches the given user input.
     *
     * @param recipe     The recipe to check against.
     * @param recipeName The user input recipe name to match against.
     * @return True if the recipe's name matches the input, false otherwise.
     */
    private boolean isRecipeNameMatching(Recipe recipe, String recipeName) {
        boolean isRecipeNameValid = recipe != null;
        if (isRecipeNameValid){
            String userInputRecipeLower = recipeName.toLowerCase();
            String recipeNameLowerCase = recipe.getRecipeName().toLowerCase();
            isRecipeNameValid = userInputRecipeLower.equals("") || recipeNameLowerCase
                    .contains(userInputRecipeLower);
        }
        return isRecipeNameValid;
    }
}