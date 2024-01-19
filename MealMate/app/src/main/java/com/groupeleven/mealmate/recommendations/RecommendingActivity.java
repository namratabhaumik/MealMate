package com.groupeleven.mealmate.recommendations;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.groupeleven.mealmate.AccountManagement.FavoriteRecipeDBHelper;
import com.groupeleven.mealmate.DBHelper;
import com.groupeleven.mealmate.InstructionsActivity;
import com.groupeleven.mealmate.R;
import com.groupeleven.mealmate.Recipe.models.Recipe;
import com.groupeleven.mealmate.SharedValues;
import com.groupeleven.mealmate.recommendations.RecommendAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The RecommendingActivity class provides functionality for recommending recipes based on selected ingredients.
 * It interacts with Firebase Firestore to retrieve recipe data and uses various adapters for displaying recommendations.
 */
public class RecommendingActivity extends AppCompatActivity {
    private static final double HALF_QUANTITY_THRESHOLD = 0.50;
    private static final int COLUMN_QUANTITY_INDEX = 2;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerRecommendation;

    private RecyclerView myRecyclerRecommendation;

    private RecyclerView similarRecyclerRecommendation;

    private DBHelper dbHelper;

    private FavoriteRecipeDBHelper favRecipeDbHelper;

    private SQLiteDatabase sqlDBRead, recipedDBRead, recipeDBWrite;

    private CollectionReference collectionRef;

    Map<String, Integer> selectedIngredients = new HashMap<>();

    private List<Map<String,Object>> recipeIngredients = new ArrayList<>();

    Map<String,Integer> have = new HashMap<>();

    Map<String,Integer> notHave = new HashMap<>();

    List<Map<String, Object>> recommendRecipes = new ArrayList();

    List<Map<String,Object>> myRecommendedRecipe = new ArrayList<>();

    List<Map<String, Object>> similarRecommendRecipe = new ArrayList<>();

    Map<String, Map<String, Map<String, Integer>>> details = new HashMap<>();

    RecommendAdapter recommendAdapter;
    RecommendAdapter myRecommendAdapter;
    RecommendAdapter similarRecommendAdapter;

    SharedPreferences session;

    String userEmail;

    MaterialSwitch toggle;

    boolean isVeg;

    TextView myRecipeTextView, similarTextView, recommendTextView;

    /**
     * Initializes the activity and sets up UI elements.
     * It also initializes Firebase, database helpers, and retrieves user preferences.
     *
     * @param savedInstanceState The saved state of the activity.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        dbHelper = new DBHelper(this);
        favRecipeDbHelper = new FavoriteRecipeDBHelper(this);
        sqlDBRead = dbHelper.getReadableDatabase();
        recipeDBWrite = favRecipeDbHelper.getWritableDatabase();
        recipedDBRead = favRecipeDbHelper.getReadableDatabase();

        session = getSharedPreferences("MealMateSharedPreferences", MODE_PRIVATE);
        userEmail = session.getString("email","default");

        setContentView(R.layout.recommending_recipe_layout);

        recyclerRecommendation = findViewById(R.id.recycler_recommendations);
        recyclerRecommendation.setLayoutManager(new LinearLayoutManager(this));

        myRecyclerRecommendation = findViewById(R.id.recycler_my_recipes);
        myRecyclerRecommendation.setLayoutManager(new LinearLayoutManager(this));

        similarRecyclerRecommendation = findViewById(R.id.recycler_similar_recipes);
        similarRecyclerRecommendation.setLayoutManager(new LinearLayoutManager(this));

        toggle = findViewById(R.id.vegSwitch);
        isVeg = !toggle.isChecked();

        myRecipeTextView = findViewById(R.id.myRecipesTextView);
        similarTextView = findViewById(R.id.similarRecommendationsTextView);
        recommendTextView = findViewById(R.id.recommendationsTextView);

        getSelectedIngredients();
        getRecipesFromFirebase();

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isVeg = toggle.isChecked();
                if(isVeg){
                    toggle.setText("Non-Veg");
                }
                else{
                    toggle.setText("Veg");
                }
                recommendRecipes(!isVeg);
            }
        });

        TextInputLayout recipeSearchFieldLayout = findViewById(R.id.recipeSearchField);

        EditText recipeSearchField = recipeSearchFieldLayout.getEditText();

        recipeSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterData(charSequence.toString(), recommendRecipes, recyclerRecommendation);
                filterData(charSequence.toString(), myRecommendedRecipe, myRecyclerRecommendation);
                filterData(charSequence.toString(), similarRecommendRecipe, similarRecyclerRecommendation);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        Window appWindow = this.getWindow();
        appWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        appWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        appWindow.setStatusBarColor(ContextCompat.getColor(this, R.color.primary_light_color));
    }

    /**
     * Filters the recipe data based on the provided search text.
     *
     * @param searchText The text to filter recipes.
     * @param recipeList The list of recipes to filter.
     * @param recyclerView The RecyclerView to update with filtered data.
     */
    public void filterData(String searchText, List<Map<String, Object>> recipeList, RecyclerView recyclerView) {
        List<Map<String, Object>> filteredList = new ArrayList<>();

        for (Map<String, Object> recipe : recipeList) {
            String recipeName = recipe.get("recipeName").toString().toLowerCase();

            // Checking if the recipe name contains the search text
            if (recipeName.contains(searchText.toLowerCase())) {
                filteredList.add(recipe);
            }
        }

        // Updating the RecyclerView with the filtered data
        RecommendAdapter adapter = new RecommendAdapter(filteredList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * Retrieves selected ingredients from the local database and populates the selectedIngredients map.
     */
    private void getSelectedIngredients() {
        Cursor cursor = null;

        cursor = sqlDBRead.query("ingredients", null, null, null,
                null, null, null);

        String ingt;
        int qty;

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                ingt = cursor.getString(1);
                qty = cursor.getInt(COLUMN_QUANTITY_INDEX);
                selectedIngredients.put(ingt, qty);
            }
        }
    }

    /**
     * Retrieves recipes from Firebase Firestore and processes the data for recommendations.
     */
    private void getRecipesFromFirebase() {
        collectionRef = db.collection("Recipes");
        collectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();
                        Map<String, Object> data = document.getData();
                        recipeIngredients.add(data);
                    }
                    recommendRecipes(true);
                } else {
                    Exception exception = task.getException();
                    if (exception != null) {
                        // Handle the exception
                        Log.e("Firebase Exception", exception.toString());
                    }
                }
            }
        });
    }

    /**
     * Recommends recipes based on selected ingredients and user preferences.
     *
     * @param isVeg Specifies whether the user prefers vegetarian recipes.
     */
    private void recommendRecipes(boolean isVeg) {
        clearRecommendations();
        showTextViews();
        for (Map<String, Object> data : recipeIngredients) {
            processRecipeData(data, isVeg);
        }
        updateTextViewVisibility();
        setupRecommendAdapters();
    }

    /**
     * Clears the recommendation lists and shows the relevant TextViews.
     */
    private void clearRecommendations() {
        recommendRecipes.clear();
        myRecommendedRecipe.clear();
        similarRecommendRecipe.clear();
    }

    /**
     * Displays the relevant TextViews for recipe recommendations based on the presence of recommendations.
     */
    private void showTextViews() {
        myRecipeTextView.setVisibility(View.VISIBLE);
        similarTextView.setVisibility(View.VISIBLE);
        recommendTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Processes the recipe data, updating the "have" and "notHave" maps.
     *
     * @param data The recipe data.
     * @param isVeg Specifies whether the user prefers vegetarian recipes.
     */
    private void processRecipeData(Map<String, Object> data, boolean isVeg) {
        Map<String, Object> details = new HashMap<>();
        Map<String, Integer> have = new HashMap<>();
        Map<String, Integer> notHave = new HashMap<>();

        processIngredients(data, have, notHave);
        details.put("have", have);
        details.put("notHave", notHave);

        data.put("isFavorite",false);
        if(checkFavorite(data.get("recipeName").toString())){
            data.put("isFavorite",true);
        }

        if (isRecipeRecommended(data, isVeg, have, notHave)) {
            updateRecommendations(data, details);
        }

        have.clear();
        notHave.clear();
    }

    /**
     * Processes the list of ingredients for a recipe, updating the "have" and "notHave" maps.
     *
     * @param data The recipe data containing a list of ingredients.
     * @param have A map representing the ingredients the user has.
     * @param notHave A map representing the ingredients the user does not have.
     */
    private void processIngredients(Map<String, Object> data, Map<String, Integer> have, Map<String, Integer> notHave) {
        for (Object key : (ArrayList<Object>) data.get("ingredients")) {
            Map<String, Object> newKey = (Map<String, Object>) key;
            String ingredientName = newKey.get("name").toString().toLowerCase();
            int ingredientQuantity = Integer.parseInt(newKey.get("quantity").toString());

            updateIngredientMaps(ingredientName, ingredientQuantity, have, notHave);
        }
    }

    /**
     * Updates the "have" and "notHave" maps based on recipe ingredients.
     *
     * @param ingredientName The name of the ingredient.
     * @param ingredientQuantity The quantity of the ingredient.
     * @param have The "have" map.
     * @param notHave The "notHave" map.
     */
    private void updateIngredientMaps(String ingredientName, int ingredientQuantity, Map<String, Integer> have, Map<String, Integer> notHave) {
        if (selectedIngredients.containsKey(ingredientName)) {
            if (ingredientQuantity <= selectedIngredients.get(ingredientName)) {
                have.put(ingredientName, selectedIngredients.get(ingredientName));
            } else {
                notHave.put(ingredientName, ingredientQuantity - selectedIngredients.get(ingredientName));
            }
        } else {
            notHave.put(ingredientName, ingredientQuantity);
        }
    }

    /**
     * Checks if a recipe is recommended based on user preferences and ingredient availability.
     *
     * @param data The recipe data.
     * @param isVeg Specifies whether the user prefers vegetarian recipes.
     * @param have The "have" map.
     * @param notHave The "notHave" map.
     * @return True if the recipe is recommended, false otherwise.
     */
    private boolean isRecipeRecommended(Map<String, Object> data, boolean isVeg, Map<String, Integer> have, Map<String, Integer> notHave) {
        float total = have.size() + notHave.size();

        boolean isVegetarianMatch = (boolean) data.get("isVegetarian") == isVeg;
        boolean isUserMatch = userEmail.equals(data.get("userEmail").toString());

        boolean isUserRecipeMatch = isUserMatch && have.size() / total >= HALF_QUANTITY_THRESHOLD;
        boolean isNoNotHaveIngredients = notHave.size() == 0;
        boolean isHalfQtyMatch = have.size() / total >= HALF_QUANTITY_THRESHOLD;

        return isVegetarianMatch && (isUserRecipeMatch || isNoNotHaveIngredients || isHalfQtyMatch);
    }

    /**
     * Updates the recommendation lists based on the processed recipe data.
     *
     * @param data The recipe data.
     * @param details The details map.
     */
    private void updateRecommendations(Map<String, Object> data, Map<String, Object> details) {
        if (userEmail.equals(data.get("userEmail").toString())) {
            myRecommendedRecipe.add(data);
        } else if (((Map<?, ?>) details.get("notHave")).size() == 0) {
            recommendRecipes.add(data);
        } else {
            similarRecommendRecipe.add(data);
        }
    }

    /**
     * Updates the visibility of TextViews based on the recommendation lists.
     */
    private void updateTextViewVisibility() {
        myRecipeTextView.setVisibility(myRecommendedRecipe.isEmpty() ? View.GONE : View.VISIBLE);
        similarTextView.setVisibility(similarRecommendRecipe.isEmpty() ? View.GONE : View.VISIBLE);
        recommendTextView.setVisibility(recommendRecipes.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /**
     * Sets up the adapters for the recommendation RecyclerViews.
     */
    private void setupRecommendAdapters() {
        myRecommendAdapter = new RecommendAdapter(myRecommendedRecipe);
        myRecyclerRecommendation.setAdapter(myRecommendAdapter);
        setFavClickListener(myRecommendAdapter);

        similarRecommendAdapter = new RecommendAdapter(similarRecommendRecipe);
        similarRecyclerRecommendation.setAdapter(similarRecommendAdapter);
        setFavClickListener(similarRecommendAdapter);

        recommendAdapter = new RecommendAdapter(recommendRecipes);
        recyclerRecommendation.setAdapter(recommendAdapter);
        setFavClickListener(recommendAdapter);
    }

    /**
     * Sets the favorite click listener for a given adapter.
     *
     * @param recommendAdapter The adapter for which to set the listener.
     */
    public void setFavClickListener (RecommendAdapter recommendAdapter){
        recommendAdapter.setOnFavoriteClickListener(new RecommendAdapter.OnFavoriteClickListener() {
            @Override
            public void onFavoriteClick(int position) {
                handleFavoriteClick(recommendAdapter, position);
            }
        });
    }

    /**
     * Displays details of the selected recipe in an AlertDialog.
     *
     * @param view The view that triggered the method.
     */
    public void showDetails(View view){

        RecyclerView recyclerView = (RecyclerView) view.getParent().getParent();

        int clickedRecyclerViewId = recyclerView.getId(), pos;

        Map<String, Object> tappedRecipe;

        if(clickedRecyclerViewId == R.id.recycler_recommendations){
            pos = recyclerRecommendation.getChildAdapterPosition((View) view.getParent());
            tappedRecipe = recommendRecipes.get(pos);
        }else if(clickedRecyclerViewId == R.id.recycler_my_recipes){
            pos = myRecyclerRecommendation.getChildAdapterPosition((View) view.getParent());
            tappedRecipe = myRecommendedRecipe.get(pos);
        }else{
            pos = similarRecyclerRecommendation.getChildAdapterPosition((View) view.getParent());
            tappedRecipe = similarRecommendRecipe.get(pos);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View detailsView = getLayoutInflater().inflate(R.layout.recipe_popup,null);

        List<String> ingredients = new ArrayList<>();
        for(Map<String, Object> ingredientsData : (ArrayList<Map<String,Object>>) tappedRecipe.get("ingredients")){
            ingredients.add(ingredientsData.get("name").toString()+":\t"+ingredientsData.get("quantity").toString());
        }

        ListView ingredientsList = detailsView.findViewById(R.id.ingredientsList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ingredients);


        ingredientsList.setAdapter(adapter);

        builder.setView(detailsView);

        AlertDialog dialog = builder.create();
        dialog.show();

        TextView time = detailsView.findViewById(R.id.timeTextView);

        time.setText(tappedRecipe.get("time").toString());

        //Displaying recipe instructions on clicking start cooking button
        Button startCookingButton = detailsView.findViewById(R.id.startCooking);
        startCookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Recipe currentRecipe = getCurrentRecipe(tappedRecipe);
                SharedValues.getInstance().setInstructions((ArrayList<String>) tappedRecipe.get("instructions"));
                SharedValues.getInstance().setServingSize(Integer.parseInt((String) tappedRecipe.get("servingSize")));
                SharedValues.getInstance().setCurrentRecipe(currentRecipe);
                Intent instructionActivity = new Intent(v.getContext(), InstructionsActivity.class);
                v.getContext().startActivity(instructionActivity);
            }
        });

        recommendAdapter.setOnFavoriteClickListener(new RecommendAdapter.OnFavoriteClickListener() {
            @Override
            public void onFavoriteClick(int position) {
                handleFavoriteClick(recommendAdapter,position);
            }
        });

        myRecommendAdapter.setOnFavoriteClickListener(new RecommendAdapter.OnFavoriteClickListener() {
            @Override
            public void onFavoriteClick(int position) {
                handleFavoriteClick(myRecommendAdapter, position);
            }
        });

        similarRecommendAdapter.setOnFavoriteClickListener(new RecommendAdapter.OnFavoriteClickListener() {
            @Override
            public void onFavoriteClick(int position) {
                handleFavoriteClick(similarRecommendAdapter, position);
            }
        });
    }

    public Recipe getCurrentRecipe(Map<String, Object> tappedRecipe){
        Recipe recipe = new Recipe();
        recipe.setUserEmail(tappedRecipe.get("userEmail").toString());
        recipe.setImageUrl(tappedRecipe.get("imageUrl").toString());
        recipe.setIngredients((ArrayList<Map<String, Object>>) tappedRecipe.get("ingredients"));
        recipe.setVegetarian(Boolean.getBoolean(tappedRecipe.get("isVegetarian").toString()));
        recipe.setTime(tappedRecipe.get("time").toString());
        recipe.setInstructions((ArrayList<String>) tappedRecipe.get("instructions"));
        recipe.setRecipeDescription(tappedRecipe.get("recipeDescription").toString());
        recipe.setServingSize(tappedRecipe.get("servingSize").toString());
        return recipe;
    }

    /**
     * Handles the favorite click for a specific position in the recommendation list.
     *
     * @param recommendAdapter The adapter for the recommendation list.
     * @param position The position of the clicked item.
     */
    public void handleFavoriteClick(RecommendAdapter recommendAdapter, int position) {
        Map<String, Object> tappedRecipe = recommendAdapter.recommendRecipeList.get(position);
        boolean isFavorite = (boolean) tappedRecipe.get("isFavorite");

        if(isFavorite){
           deleteFromDB(tappedRecipe.get("recipeName").toString());
        }
        else{
            insertFavRecipeToDB(tappedRecipe.get("recipeName").toString());
        }

        // Toggle the favorite status (you can replace this logic with your own)
        tappedRecipe.put("isFavorite", !isFavorite);

        // Update your data set and refresh the adapter
        recommendAdapter.notifyDataSetChanged();
    }

    /**
     * Checks if a recipe is marked as a favorite in the local database.
     *
     * @param recipeName The name of the recipe.
     * @return True if the recipe is a favorite, false otherwise.
     */
    public boolean checkFavorite(String recipeName){
        Cursor cursor = null;

        String[] selectAttrs = {"name"};

        String whereClause = "name = ?";

        String[] matchValue = {recipeName};

        cursor = recipedDBRead.query("FavoriteRecipe", selectAttrs, whereClause, matchValue,
                null, null, null);

        if(cursor.getCount()!=0){
            return true;
        }
        return false;
    }

    /**
     * Inserts a favorite recipe into the local database.
     *
     * @param recipeName The name of the recipe to insert.
     */
    private void insertFavRecipeToDB(String recipeName){
        ContentValues value = new ContentValues();
        value.put("name",recipeName);
        recipeDBWrite.insertWithOnConflict("FavoriteRecipe", null, value, SQLiteDatabase.CONFLICT_REPLACE);

    }

    /**
     * Deletes a recipe from the local database.
     *
     * @param recipeName The name of the recipe to delete.
     */
    private void deleteFromDB(String recipeName){

        String whereClause = "name = ?";
        String[] whereArgs = {recipeName};

        recipeDBWrite.delete("FavoriteRecipe", whereClause, whereArgs);
    }
}
