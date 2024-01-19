package com.groupeleven.mealmate.GroceryPlanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.groupeleven.mealmate.Common.utils.ToastUtils;
import com.groupeleven.mealmate.GroceryPlanner.utils.GroceryListAdapter;
import com.groupeleven.mealmate.InventoryManagement.utils.InventoryLoader;
import com.groupeleven.mealmate.MainActivity;
import com.groupeleven.mealmate.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Activity for confirming and uploading grocery items to the inventory.
 */
public class ConfirmGroceryActivity extends AppCompatActivity {
    FirebaseAuth auth;
    private Map<String, Float> loadedGroceryList;
    private ListView groceryListView;
    private GroceryListAdapter groceryListAdapter;
    private SharedPreferences sharedPreferences;
    private static final String GROCERY_PREFERENCES = "GroceryPreferences";
    private static final String GROCERY_LIST_KEY = "GroceryListKey";
    InventoryLoader inventoryLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_grocery);

        setStatusBarColor();

        inventoryLoader = new InventoryLoader(FirebaseFirestore.getInstance());

        auth = FirebaseAuth.getInstance();

        Button confirmGroceryBtn = findViewById(R.id.confirmGroceryBtn);

        sharedPreferences = getSharedPreferences(GROCERY_PREFERENCES, MODE_PRIVATE);

        confirmGroceryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loadedGroceryList != null && !loadedGroceryList.isEmpty()){
                    CheckBox checkBox = findViewById(R.id.tickmarkCheckBox);
                    inventoryLoader.uploadGroceryListToInventory(auth.getCurrentUser().getEmail(),
                                    loadedGroceryList, checkBox.isChecked())
                            .addOnSuccessListener(aVoid -> {
                                ToastUtils.showLongToast(v.getContext(), "Inventory updated" +
                                        " successfully.");
                                clearGroceryListFromSharedPreferences();
                                Intent intent = new Intent(v.getContext(), MainActivity.class);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                ToastUtils.showLongToast(v.getContext(), "Inventory update " +
                                        "failed.");
                            });
                } else {
                    ToastUtils.showLongToast(v.getContext(), "Grocery list is empty!");
                }
            }
        });

        Intent intent = getIntent();

        String jsonGroceryList = intent.getStringExtra("loadedGroceryList");
        loadedGroceryList = new Gson().fromJson(
                jsonGroceryList,
                new TypeToken<Map<String, Float>>(){}.getType()
        );

        // Check if loadedGroceryList is null or empty, load from SharedPreferences if needed
        if (loadedGroceryList == null || ((Map) loadedGroceryList).isEmpty()) {
            loadedGroceryList = loadGroceryListFromSharedPreferences();
        }

        // Show message for no grocery items found in SharedPreferences as well
        // or display the grocery items list
        if (loadedGroceryList == null || ((Map) loadedGroceryList).isEmpty()) {
            showNoGroceryItemsMessage();
        } else {
            saveGroceryListToSharedPreferences();
            displayGroceryItems();
        }
    }

    /**
     * Sets the color of the status bar.
     */
    private void setStatusBarColor() {
        Window appWindow = this.getWindow();
        appWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        appWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        appWindow.setStatusBarColor(ContextCompat.getColor(this,R.color.black));
    }

    /**
     * Displays a message when no grocery items are found.
     */
    private void showNoGroceryItemsMessage() {
        TextView noGroceryItemsTextView = findViewById(R.id.noGroceryItemsTextView);
        groceryListView = findViewById(R.id.groceryListView);

        noGroceryItemsTextView.setVisibility(View.VISIBLE);
        groceryListView.setVisibility(View.GONE);
    }

    /**
     * Displays the list of grocery items if available.
     */
    private void displayGroceryItems(){
        TextView noGroceryItemsTextView = findViewById(R.id.noGroceryItemsTextView);
        groceryListView = findViewById(R.id.groceryListView);

        noGroceryItemsTextView.setVisibility(View.GONE);
        groceryListView.setVisibility(View.VISIBLE);

        groceryListView = findViewById(R.id.groceryListView);
        groceryListAdapter = new GroceryListAdapter(this,  new ArrayList<>());
        groceryListView.setAdapter(groceryListAdapter);

        List<Map.Entry<String, Float>> groceryEntries = new ArrayList<>(
                loadedGroceryList.entrySet());

        groceryListAdapter.clear();
        groceryListAdapter.addAll(groceryEntries);
        groceryListAdapter.notifyDataSetChanged();
    }

    /**
     * Saves the grocery list to SharedPreferences.
     */
    private void saveGroceryListToSharedPreferences() {
        String jsonGroceryList = new Gson().toJson(loadedGroceryList);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(GROCERY_LIST_KEY, jsonGroceryList);
        editor.apply();
    }

    /**
     * Loads the grocery list from SharedPreferences.
     *
     * @return A Map containing grocery items and their quantities.
     */
    private Map<String, Float> loadGroceryListFromSharedPreferences() {
        String jsonGroceryList = sharedPreferences.getString(GROCERY_LIST_KEY, "");
        return new Gson().fromJson(
                jsonGroceryList,
                new TypeToken<Map<String, Float>>(){}.getType()
        );
    }

    /**
     * Clears the grocery list from SharedPreferences.
     */
    private void clearGroceryListFromSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(GROCERY_LIST_KEY);
        editor.apply();
    }
}