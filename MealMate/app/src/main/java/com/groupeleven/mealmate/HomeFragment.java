package com.groupeleven.mealmate;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;
import com.groupeleven.mealmate.Recipe.models.Recipe;
import com.groupeleven.mealmate.Recipe.utils.RecipeListAdapter;

public class HomeFragment extends Fragment {
    RelativeLayout loadingPB;
    RecyclerView recyclerView;
    ImageButton refreshIV;
    /**
     * override method required to initiate this activity
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        loadingPB = v.findViewById(R.id.loadingPB);
        recyclerView = v.findViewById(R.id.recycler_view_home);
        refreshIV = v.findViewById(R.id.refreshIV);
        refreshIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadRecipes();
            }
        });
        loadRecipes();
        return v;
    }

    /**
     * UI task to set up the instructions view adapter
     *
     * Why? To fetch the recipes from firestore database and make an arraylist of Recipe objects to
     * load them on the home screen
     *
     */
    public void loadRecipes() {
        loadingPB.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        refreshIV.setVisibility(View.GONE);

        CollectionReference recipesReference;
        FirebaseFirestore fbdb = FirebaseFirestore.getInstance();
        recipesReference = fbdb.collection(FireBaseEntities.RECIPES);

        recipesReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<Recipe> recipes = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Map<String, Object> data = document.getData();
                    String name = data.get(FireBaseEntities.R_NAME).toString();
                    String desc = data.get(FireBaseEntities.R_DESC).toString();
                    String isVeg = data.get(FireBaseEntities.IS_VEG).toString();
                    String ownerEmail = data.get(FireBaseEntities.R_OWNER_MAIL).toString();
                    String timeTaken = data.get(FireBaseEntities.R_TIME_TAKEN).toString();
                    ArrayList<String> instructions = (ArrayList<String>) data.get(FireBaseEntities.REC_INSTS);
                    ArrayList<Map<String, Object>> ingredients = (ArrayList<Map<String, Object>>) data.get(FireBaseEntities.INGR);

                    String imageName = data.get(FireBaseEntities.IMAGE_URL).toString();
                    String servingSize = String.valueOf(data.get(FireBaseEntities.R_SERV_SIZ));
                    Log.d("",name + desc+ ingredients + instructions);
                    Recipe r = new Recipe(name, servingSize, desc);
                    r.setImageUrl(imageName);
                    r.setIngredients(ingredients);
                    r.setInstructions(instructions);
                    r.setUserEmail(ownerEmail);
                    r.setVegetarian(Boolean.getBoolean(isVeg));
                    r.setTime(timeTaken);
                    r.setRecipeId(document.getId());
                    recipes.add(r);
                }
                Log.d("Recipes: ", recipes.toString());
                SharedValues.getInstance().setExistingRecipies(recipes);
                SharedValues.getInstance().setInstructionsActivityCaller("HomeFragment");

                loadingPB.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                refreshIV.setVisibility(View.VISIBLE);
                recyclerView.setHasFixedSize(true);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext());
                RecipeListAdapter adapter = new RecipeListAdapter(recipes, this.getContext(), "HomeFragment");
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
            } else {
                Exception exception = task.getException();
                if (exception != null) {
                    Log.e("Firebase Exception", exception.toString());
                }
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedValues.getInstance().getNavigationView().getMenu().getItem(0).setChecked(true);
    }
}