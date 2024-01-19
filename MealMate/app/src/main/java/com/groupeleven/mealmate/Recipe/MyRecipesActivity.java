package com.groupeleven.mealmate.Recipe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.groupeleven.mealmate.Common.constants.CommonConstants;
import com.groupeleven.mealmate.Common.utils.ToastUtils;
import com.groupeleven.mealmate.R;
import com.groupeleven.mealmate.Recipe.models.Recipe;
import com.groupeleven.mealmate.Recipe.utils.RecipeListAdapter;
import com.groupeleven.mealmate.Recipe.utils.RecipeLoader;
import com.groupeleven.mealmate.SharedValues;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display recipes owned by the current user.
 */
public class MyRecipesActivity extends AppCompatActivity {
    Context activityContext = this;
    RecyclerView myRecipesRecyclerView;
    ProgressBar loadingPB;
    RecipeLoader recipeLoader = new RecipeLoader(FirebaseFirestore.getInstance());
    FirebaseAuth auth = FirebaseAuth.getInstance();
    List<Recipe> userRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipes);

        myRecipesRecyclerView = findViewById(R.id.recycler_view_home);
        loadingPB = findViewById(R.id.loadingPB);
        loadingPB.setVisibility(View.VISIBLE);
        setUpStatusbarColor();

        loadUserRecipes();
    }

    /**
     * Loads recipes owned by the current user.
     * Uses Firebase authentication to get the current user's email and fetches their recipes.
     */
    private void loadUserRecipes(){
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String currentUserEmail = currentUser.getEmail();
            Task<List<Recipe>> userRecipeLoadingTask =
                    recipeLoader.loadUserRecipes(currentUserEmail);

            userRecipeLoadingTask.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    userRecipes = task.getResult();
                    if (userRecipes != null) {
                        RecyclerView.LayoutManager layoutManager =
                                new LinearLayoutManager(activityContext);
                        RecipeListAdapter adapter = new RecipeListAdapter(
                                (ArrayList<Recipe>) userRecipes,
                                activityContext,
                                "MyRecipesActivity");
                        myRecipesRecyclerView.setHasFixedSize(true);
                        myRecipesRecyclerView.setLayoutManager(layoutManager);
                        myRecipesRecyclerView.setAdapter(adapter);

                        SharedValues.getInstance()
                                .setInstructionsActivityCaller("MyRecipesActivity");

                    }
                } else {
                    ToastUtils.showLongToast(activityContext, "Couldn't" +
                            "load your recipes!");
                }
                loadingPB.setVisibility(View.GONE);
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!isInstructionsRequestValid(requestCode, resultCode, data)) {
            return;
        }
        loadUserRecipes();
    }

    /**
     * Sets up the status bar color for the activity.
     */
    private void setUpStatusbarColor() {
        Window appWindow = this.getWindow();
        appWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        appWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        appWindow.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
    }

    /**
     * Checks if the received result is from an instructions request.
     *
     * @param requestCode The request code received from the activity result.
     * @param resultCode  The result code received from the activity result.
     * @param data        The data received from the activity result.
     * @return True if the received result is from an instructions request and is valid; otherwise, false.
     */
    private boolean isInstructionsRequestValid(int requestCode, int resultCode, Intent data) {
        boolean isInstructionsRequest = requestCode == CommonConstants.INSTRUCTIONS_REQUEST_CODE;
        boolean isResultOK = resultCode == RESULT_OK;
        boolean isDataNotNull = data != null;

        return isInstructionsRequest && isResultOK && isDataNotNull;
    }
}