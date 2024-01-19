package com.groupeleven.mealmate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.groupeleven.mealmate.Recipe.models.Recipe;
import com.groupeleven.mealmate.Recipe.utils.RecipeListAdapter;

import java.util.ArrayList;
import java.util.Map;

public class SharedRecipesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_recipes);
        RecyclerView recycler_view_home = findViewById(R.id.recycler_view_home);
        ProgressBar loadingPB = findViewById(R.id.loadingPB);
        loadingPB.setVisibility(View.VISIBLE);
        Window appWindow = this.getWindow();
        appWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        appWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        appWindow.setStatusBarColor(ContextCompat.getColor(this,R.color.black));

        DocumentReference shareRecipesReference;
        FirebaseFirestore fbdb = FirebaseFirestore.getInstance();
        shareRecipesReference = fbdb.collection(FireBaseEntities.SHR_RECI_COL).document(FireBaseEntities.SHR_REC_DOC);

        shareRecipesReference.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> data = documentSnapshot.getData();
            assert data != null;
            ArrayList<String> keys = new ArrayList<>(data.keySet());
            String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            ArrayList<String> recipesIds = new ArrayList<>();
            if (keys.contains(userEmail)) {
                recipesIds = (ArrayList<String>) data.get(userEmail);
            }
            ArrayList<Recipe> existingRecipes = SharedValues.getInstance().getExistingRecipies();
            ArrayList<Recipe> recipesToShow = new ArrayList<>();
            for (Recipe recipe : existingRecipes) {
                for (String id : recipesIds) {
                    if (id.contentEquals(recipe.getRecipeId())) {
                        recipesToShow.add(recipe);
                    }
                }
            }
            recycler_view_home.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            RecipeListAdapter adapter = new RecipeListAdapter(recipesToShow, this, "SharedRecipesActivity");
            recycler_view_home.setLayoutManager(layoutManager);
            recycler_view_home.setAdapter(adapter);
            loadingPB.setVisibility(View.GONE);
        });
    }
}