package com.groupeleven.mealmate.AccountManagement;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.groupeleven.mealmate.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FavoriteRecipesActivity class displays a list of favorite recipes stored locally and in Firebase.
 * It uses a local SQLite database to retrieve favorite recipes and fetches additional details from
 * the Firebase Firestore database. The retrieved data is displayed in a RecyclerView using a custom adapter.
 *
 * The class extends AppCompatActivity and initializes the necessary components, including database helpers,
 * RecyclerView, and Firebase. It fetches data from both the local SQLite database and Firebase Firestore,
 * combines the results, and displays them in a RecyclerView using a custom adapter.
 */
public class FavoriteRecipesActivity extends AppCompatActivity {

    private FavoriteRecipeDBHelper favRecipeDbHelper;

    private SQLiteDatabase recipedDBRead;

    private CollectionReference collectionRef;

    private ArrayList<String> recipeList = new ArrayList<>();

    List<Map<String, Object>> favRecipeData = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private RecyclerView favoriteRecipeRecyclerView;

    /**
     * Initializes the activity, sets up the database connections, and configures the RecyclerView.
     * Retrieves favorite recipes from both the local SQLite database and Firebase Firestore.
     *
     * @param savedInstanceState A Bundle containing the data most recently supplied in onSaveInstanceState().
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        favRecipeDbHelper = new FavoriteRecipeDBHelper(this);
        recipedDBRead = favRecipeDbHelper.getReadableDatabase();

        setContentView(R.layout.favorite_recipes_layout);

        favoriteRecipeRecyclerView = findViewById(R.id.recycler_favorite_recipes);
        favoriteRecipeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        getFavoriteRecipesFromDB();
        showDetailsFromFirebase();

        Window appWindow = this.getWindow();
        appWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        appWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        appWindow.setStatusBarColor(ContextCompat.getColor(this, R.color.primary_light_color));
    }

    /**
     * Retrieves additional details of favorite recipes from the Firebase Firestore database
     * and displays the combined data in the RecyclerView using a custom adapter.
     */
    private void showDetailsFromFirebase(){
        collectionRef = db.collection("Recipes");

        collectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map<String, Object> data = document.getData();
                        if(recipeList.contains(data.get("recipeName").toString())){
                            favRecipeData.add(data);
                        }
                    }

                    FavoriteRecipeAdapter favoriteRecipeAdapter = new FavoriteRecipeAdapter(favRecipeData);
                    favoriteRecipeRecyclerView.setAdapter(favoriteRecipeAdapter);

                } else {
                    Exception exception = task.getException();
                    if (exception != null) {
                        Log.e("Firebase Exception", exception.toString());
                    }
                }
            }
        });
    }

    /**
     * Retrieves favorite recipes from the local SQLite database and populates the recipeList.
     */
    private void getFavoriteRecipesFromDB(){
        Cursor cursor = null;

        cursor = recipedDBRead.query("FavoriteRecipe", null, null, null,
                null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                recipeList.add(cursor.getString(1));
            }
        }
    }
}
