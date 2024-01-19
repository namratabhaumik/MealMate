package com.groupeleven.mealmate.Ingredients;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.groupeleven.mealmate.CategoryAdapter;
import com.groupeleven.mealmate.CategoryData;
import com.groupeleven.mealmate.DBHelper;
import com.groupeleven.mealmate.R;
import com.groupeleven.mealmate.recommendations.RecommendingActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the main activity for managing the inventory, displaying categories,
 * and handling ingredient data. It includes methods to interact with Firestore for category and ingredient data,
 * SQLite database for local storage, and UI elements like RecyclerView, buttons, and text fields.
 *
 * The class extends AppCompatActivity and implements various UI interaction methods,
 * database operations, and Firestore data retrieval callbacks.
 */
public class InventoryActivity extends AppCompatActivity {
    private static final String CREATE_TABLE_QUERY =
            "CREATE TABLE ingredients (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "quantity INTEGER, " +
                    "imgUrl TEXT, " +
                    "category TEXT, " +
                    "unit TEXT)";
    private RecyclerView recyclerView;
    private CategoryAdapter categoryAdapter;
    private CollectionReference collectionRef;
    private List<IngredientsData> ingredientsDataList;
    private InventoryAdapter inventoryAdapter;
    private List<CategoryData> category;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DBHelper dbHelper;
    private SQLiteDatabase sqlDBWrite;
    private SQLiteDatabase sqlDBRead;
    private static final int NO_OF_COOK = 1;
    private static final int CLMN_NAME_INDEX = 1;
    private static final int CLMN_CATEGORY_INDEX = 4;
    private static final int CLMN_QUANTITY_INDEX = 2;
    private static final int CLMN_THIRD_INDEX = 3;
    private static final int CLMN_FIFTH_INDEX = 5;

    static int quantity = 0;
    Button startRecommendButton;
    Button selectedIngredient;
    private HashMap<String, List<IngredientsData>> categoryIngredientMap = new HashMap<>();
    private List<IngredientsData> selectedIngredients = new ArrayList<>();
    private IngredientsAdapter ingredientsAdapter;
    private CardView editTextCardView;
    private String clickedCategory;
    EditText editText;
    ImageView imgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
    }

    /**
     * Initializes the activity by setting up the Firebase app, SQLite database, and UI components.
     * It also triggers the creation of the ingredients table in the local database.
     */
    private void initialize() {
        FirebaseApp.initializeApp(this);
        dbHelper = new DBHelper(this);
        sqlDBWrite = dbHelper.getWritableDatabase();
        sqlDBWrite.execSQL("DROP TABLE IF EXISTS ingredients");
        createIngredientsTable();

        setContentView(R.layout.inventory_mgmt);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        startRecommendButton = findViewById(R.id.start_recommend_button);
        selectedIngredient = findViewById(R.id.selected_ing);
        LinearLayout buttonsLayout = findViewById(R.id.buttons_layout);
        buttonsLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        imgView = findViewById(R.id.no_of_people);
        editText = findViewById(R.id.people_qty_id);

        populateCategories();

        Window appWindow = this.getWindow();
        appWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        appWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        appWindow.setStatusBarColor(ContextCompat.getColor(this, R.color.primary_light_color));
    }

    /**
     * Creates the "ingredients" table in the local SQLite database using the provided SQL query.
     */
    private void createIngredientsTable() {
        sqlDBWrite.execSQL(CREATE_TABLE_QUERY);
    }

    /**
     * Populates the categories by either loading them from Firestore or using the existing adapter.
     * This method is called when the activity is created or when the user navigates back to the category view.
     */
    private void populateCategories() {
        startRecommendButton.setVisibility(View.GONE);
        selectedIngredient.setVisibility(View.VISIBLE);
        imgView.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);

        if (categoryAdapter != null) {
            recyclerView.setAdapter(categoryAdapter);
        } else {
            loadCategoriesFromFirestore();
        }
    }

    /**
     * Retrieves categories from Firestore and populates the RecyclerView with the CategoryAdapter.
     */
    private void loadCategoriesFromFirestore() {
        collectionRef = db.collection("Categories");

        collectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    category = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map<String, Object> data = document.getData();
                        category.add(new CategoryData(data.get("Name").toString(),
                                data.get("Url").toString()));
                    }
                    categoryAdapter = new CategoryAdapter(category);
                    recyclerView.setAdapter(categoryAdapter);
                } else {
                    handleFirestoreError(task.getException());
                }
            }
        });
    }

    /**
     * Handles errors that occur during Firestore operations, logging the exception details.
     *
     * @param exception The exception that occurred during Firestore operations.
     */
    private void handleFirestoreError(Exception exception) {
        if (exception != null) {
            Log.e("Firebase Exception", exception.toString());
        }
    }

    /**
     * Handles the click event when a category is selected. Retrieves and displays the ingredients
     * belonging to the selected category, either from the local SQLite database or Firestore.
     *
     * @param view The clicked view representing the selected category.
     */
    public void onCategoryClick(View view) {
        CardView cardView = (CardView) view;
        TextView txtQty = cardView.findViewById(R.id.category_name);
        clickedCategory = txtQty.getText().toString();
        populateIngredientsOnCategories(clickedCategory);
    }

    /**
     * Populates the RecyclerView with ingredient data based on the selected category.
     * Retrieves data from the local SQLite database if available, otherwise queries Firestore.
     *
     * @param category The selected category for which ingredients need to be populated.
     */
    public void populateIngredientsOnCategories(String category) {
        selectedIngredient.setVisibility(View.VISIBLE);
        startRecommendButton.setVisibility(View.GONE);
        imgView.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);

        List<IngredientsData> retrievedData = checkAndGetCategory(category.toLowerCase());
        if (retrievedData.size() > 0) {
            setupInventoryAdapter(retrievedData);
        } else {
            if (categoryIngredientMap.containsKey(category)) {
                setupInventoryAdapter(categoryIngredientMap.get(category));
            } else {
                retrieveIngredientsDataFromFirestore(category);
            }
        }
    }

    /**
     * Retrieves ingredient data from Firestore for the specified category and updates the local SQLite database.
     *
     * @param category The category for which ingredient data is to be retrieved.
     */
    private void retrieveIngredientsDataFromFirestore(String category) {
        collectionRef = db.collection("Ingredients");

        Query query = collectionRef.whereEqualTo("category", category);
        collectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ContentValues value = new ContentValues();
                    ingredientsDataList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map<String, Object> data = document.getData();
                        String url = data.get("imageUrl").toString();
                        String name = data.get("name").toString();
                        String unit = data.get("unit").toString();
                        String ctgry = data.get("category").toString();

                        if (ctgry.toLowerCase().equals(category.toLowerCase())) {
                            IngredientsData ingredient = new IngredientsData();
                            ingredient.setIngredientImg(url);
                            ingredient.setIngredientName(name);
                            ingredient.setIngredientQty(0);
                            ingredient.setNoOfCook(0);
                            ingredient.setUnit(unit);
                            ingredient.setIngredientCategory(ctgry);

                            ingredientsDataList.add(ingredient);

                            value.put("name", name.toLowerCase());
                            value.put("quantity", 0);
                            value.put("category", ctgry.toLowerCase());
                            value.put("unit", unit);
                            value.put("imgUrl", url);
                            sqlDBWrite.insertWithOnConflict("ingredients", null, value, SQLiteDatabase.CONFLICT_REPLACE);
                            value.clear();
                        }
                    }

                    categoryIngredientMap.put(category, ingredientsDataList);
                    setupInventoryAdapter(ingredientsDataList);
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
     * Sets up the InventoryAdapter with the provided ingredient data and attaches it to the RecyclerView.
     *
     * @param data The list of ingredient data to be displayed.
     */
    private void setupInventoryAdapter(List<IngredientsData> data) {
        inventoryAdapter = new InventoryAdapter(data);
        recyclerView.setAdapter(inventoryAdapter);
    }

    /**
     * Queries the local SQLite database to retrieve a list of IngredientsData objects for the specified category.
     * The method uses the DBHelper to get a readable database, performs a query, and maps the result to IngredientsData.
     * In case of an exception, an empty list is returned.
     *
     * @param category The category for which ingredient data is to be retrieved.
     * @return A list of IngredientsData objects for the specified category, or an empty list if an exception occurs.
     */
    public List<IngredientsData> checkAndGetCategory(String category) {
        try {
            sqlDBRead = dbHelper.getReadableDatabase();
            Cursor cursor = null;

            String[] selectAttrs = {"category"};

            String whereClause = "category = ?";

            String[] matchValue = {category};

            cursor = sqlDBRead.query("ingredients", null, whereClause, matchValue,
                    null, null, null);

            List<IngredientsData> fetchedData = new ArrayList<>();

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    IngredientsData data = new IngredientsData();
                    data.setIngredientName(cursor.getString(CLMN_NAME_INDEX));
                    data.setIngredientCategory(cursor.getString(CLMN_CATEGORY_INDEX));
                    data.setIngredientQty(cursor.getInt(CLMN_QUANTITY_INDEX));
                    data.setNoOfCook(NO_OF_COOK);
                    data.setUnit(cursor.getString(CLMN_FIFTH_INDEX));
                    data.setIngredientImg(cursor.getString(CLMN_THIRD_INDEX));

                    fetchedData.add(data);
                }
                cursor.close();
            }
            return fetchedData;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Handles the click event when the "Add" icon is clicked for a specific ingredient.
     * Increases the quantity of the selected ingredient and updates the UI accordingly.
     *
     * @param view The clicked view representing the "Add" icon.
     */
    public void onAddButtonClick(View view) {
        CardView cardView = (CardView) view.getParent().getParent();

        TextView txtQty = cardView.findViewById(R.id.ingt_qty_num);
        TextView ingtName = cardView.findViewById(R.id.ingt_name);

        int pos = recyclerView.getChildAdapterPosition((View) cardView);
        IngredientsData ingredient = ingredientsDataList.get(pos);
        IngredientsData modifiedIngredient = categoryIngredientMap.get(ingredient.getIngredientCategory()).get(pos);
        int updatedQty = modifiedIngredient.getIngredientQty() + 1;
        txtQty.setText(updatedQty + "");
        modifiedIngredient.setIngredientQty(updatedQty);
    }

    /**
     * Handles the click event when the "Subtract" icon is clicked for a specific ingredient.
     * Decreases the quantity of the selected ingredient and updates the UI accordingly.
     *
     * @param view The clicked view representing the "Subtract" icon.
     */
    public void onSubtractButtonClick(View view) {
        CardView cardView = (CardView) view.getParent().getParent();

        TextView txtQty = cardView.findViewById(R.id.ingt_qty_num);

        int pos = recyclerView.getChildAdapterPosition((View) cardView);
        IngredientsData ingredient = ingredientsDataList.get(pos);
        int updatedQty = ingredient.getIngredientQty() - 1 >= 0 ?
                ingredient.getIngredientQty() - 1 : 0;
        txtQty.setText(updatedQty + "");
        ingredient.setIngredientQty(updatedQty);
    }

    /**
     * Handles the click event when the quantity of a specific ingredient is edited.
     * Updates the quantity in the corresponding IngredientsData object and the UI.
     *
     * @param view The clicked view representing the edited quantity.
     */
    public void onEditQuantity(View view) {
        editTextCardView = (CardView) view.getParent().getParent().getParent();

        EditText updatedQty = editTextCardView.findViewById(R.id.ingt_qty_num);

        updatedQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int updatedQty = Integer.parseInt(s.toString());
                    int pos = recyclerView.getChildAdapterPosition((View) editTextCardView);
                    IngredientsData ingredient = ingredientsDataList.get(pos);
                    IngredientsData modifiedIngredient = categoryIngredientMap.get(ingredient.getIngredientCategory()).get(pos);
                    modifiedIngredient.setIngredientQty(updatedQty);
                    System.out.println("nsal");
                } catch (Exception e) {
                    Log.d("Error Parsing Int in Updating Qty", e.toString());
                }
            }
        });
    }

    /**
     * Updates the quantity of ingredients based on the edited values in the UI.
     */
    private void updateEditedQuantity(){
        int childCount = recyclerView.getChildCount();
        for(int i = 0; i<childCount;i++) {
            CardView ingtCardView = (CardView) recyclerView.getChildAt(i);
            EditText updatedQty = editTextCardView.findViewById(R.id.ingt_qty_num);
            TextView txtView = editTextCardView.findViewById(R.id.ingt_name);

            List<IngredientsData> selectedIngredients = categoryIngredientMap.get(clickedCategory);

            for(IngredientsData data : selectedIngredients){
                if(data.getIngredientName().equals(txtView.getText().toString())){
                    data.setIngredientQty(Integer.parseInt(updatedQty.getText().toString()));
                }
            }
        }
    }

    private boolean onSelectedIngredientPage(){
        return recyclerView.getAdapter() instanceof IngredientsAdapter;
    }

    private boolean onCategoryPage(){
        return recyclerView.getAdapter() instanceof CategoryAdapter;
    }

    /**
     * Overrides the behavior of the back button to handle navigation within the activity.
     * Calls methods to update quantity, populate categories, or navigate back based on the current view.
     */
    @Override
    public void onBackPressed() {
        Log.d("Not null1","Calling Edit Quantity");
        if(editTextCardView!=null){
            Log.d("Not null","Calling Edit Quantity");
            updateEditedQuantity();
        }
        if (onIngredientPage()) {
            updateToSql();
            populateCategories();
        } else if (onSelectedIngredientPage()){
            if(clickedCategory!=null) {
                populateIngredientsOnCategories(clickedCategory);
            }
            populateCategories();
        }
        else if(onCategoryPage()){
            setResult(RESULT_OK, new Intent().putExtra("RETURN_TO_HOME", true));
            finish();
        }
        else{
            super.onBackPressed();
        }
    }

    /**
     * Updates the local SQLite database with the edited quantity of ingredients.
     * Iterates through the RecyclerView items and updates the database entries accordingly.
     */
    private void updateToSql(){
        int childCount = recyclerView.getChildCount();
        for(int i = 0; i<childCount;i++) {
            CardView ingtCardView = (CardView) recyclerView.getChildAt(i);
            EditText updatedQty = ingtCardView.findViewById(R.id.ingt_qty_num);
            TextView txtView = ingtCardView.findViewById(R.id.ingt_name);

            updateQtyToSql(txtView.getText().toString().toLowerCase(),updatedQty.getText().toString());
        }
    }

    /**
     * Updates the quantity of a specific ingredient in the local SQLite database.
     *
     * @param ingtName  The name of the ingredient.
     * @param quantity  The updated quantity of the ingredient.
     */
    public void updateQtyToSql(String ingtName, String quantity){
        String whereClause = "name = ?";
        String[] checkAttrs = {ingtName};
        ContentValues setValues = new ContentValues();
        setValues.put("quantity", quantity);


        int success = sqlDBWrite.update("ingredients", setValues, whereClause, checkAttrs);

        Log.d("Update Error", String.valueOf(success));
    }

    private boolean onIngredientPage() {
        return recyclerView.getAdapter() instanceof InventoryAdapter;
    }

    /**
     * Displays the list of selected ingredients with a quantity greater than zero.
     * Retrieves data from the local SQLite database and updates the UI accordingly.
     *
     * @param view The clicked view triggering the display of selected ingredients.
     */
    public void showSelectedIngredients(View view) {
        final int VISIBLE = View.VISIBLE;
        final int GONE = View.GONE;

        final String WHERE_CLAUSE = "quantity > ?";
        final String[] MATCH_VALUE = {"0"};

        startRecommendButton.setVisibility(VISIBLE);
        selectedIngredient.setVisibility(GONE);
        imgView.setVisibility(VISIBLE);
        editText.setVisibility(VISIBLE);

        if (onIngredientPage()) {
            updateToSql();
        }
        sqlDBRead = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        cursor = sqlDBRead.query("ingredients", null, WHERE_CLAUSE, MATCH_VALUE,
                null, null, null);

        List<IngredientsData> fetchedData = new ArrayList<>();

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(CLMN_NAME_INDEX);
                String category = cursor.getString(CLMN_CATEGORY_INDEX);
                int quantity = cursor.getInt(CLMN_QUANTITY_INDEX);
                String thirdValue = cursor.getString(CLMN_THIRD_INDEX);
                String fifthValue = cursor.getString(CLMN_FIFTH_INDEX);

                IngredientsData ingredientsData = new IngredientsData();
                ingredientsData.setIngredientName(name);
                ingredientsData.setIngredientCategory(category);
                ingredientsData.setIngredientQty(quantity);
                ingredientsData.setNoOfCook(1);
                ingredientsData.setIngredientImg(thirdValue);
                ingredientsData.setUnit(fifthValue);

                fetchedData.add(ingredientsData);
            }
            cursor.close();
        }

        ingredientsAdapter = new IngredientsAdapter(fetchedData);
        recyclerView.setAdapter(ingredientsAdapter);
    }

    /**
     * Retrieves the quantity of an existing ingredient from the local SQLite database.
     * Used to check if an ingredient already exists in the database before adding it.
     *
     * @param checkIngredient The name of the ingredient to check.
     * @return The quantity of the existing ingredient or 0 if not found.
     */
    public int getExistIngredientQty(String checkIngredient) {
        sqlDBRead = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        String[] selectAttrs = {"quantity"};

        String whereClause = "name = ?";

        String[] matchValue = {checkIngredient};

        cursor = sqlDBRead.query("user_ingredients", selectAttrs, whereClause, matchValue,
                null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex("quantity");
                if (index >= 0) {
                    int previousQty = cursor.getInt(index);
                    cursor.close();
                    return previousQty;
                }
            }
        }
        return 0;
    }

    /**
     * Navigates to the RecommendingActivity when the "Start Recommending" button is clicked.
     *
     * @param view The clicked view representing the "Start Recommending" button.
     */
    public void startRecommending(View view){
        Intent intent = new Intent(this, RecommendingActivity.class);
        startActivity(intent);
    }
}
