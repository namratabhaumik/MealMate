package com.groupeleven.mealmate.GroceryPlanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.groupeleven.mealmate.Common.utils.ToastUtils;
import com.groupeleven.mealmate.GroceryPlanner.utils.GroceryListAdapter;
import com.groupeleven.mealmate.GroceryPlanner.utils.GroceryLoader;
import com.groupeleven.mealmate.MealPlanner.models.MealPlan;
import com.groupeleven.mealmate.MealPlanner.utils.CollapsedMealPlanAdapter;
import com.groupeleven.mealmate.MealPlanner.utils.MealPlanLoader;
import com.groupeleven.mealmate.R;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

/**
 * Activity for managing the grocery planner.
 */
public class GroceryPlannerActivity extends AppCompatActivity {
    GroceryLoader groceryLoader;
    MealPlanLoader mealPlanLoader;
    List<MealPlan> loadedMealPlans;
    Map<String, Float> loadedGroceryList;
    CollapsedMealPlanAdapter collapsedMealPlanAdapter;
    private ListView mealPlansListView;
    private TextView noMealPlansTextView;
    private TextView noGroceryTextView;
    private ListView groceryListView;
    private GroceryListAdapter groceryListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_planner);

        setStatusBarColor();

        groceryLoader = new GroceryLoader(FirebaseFirestore.getInstance());
        mealPlanLoader = new MealPlanLoader(FirebaseFirestore.getInstance());

        Button openDatePickerButton = findViewById(R.id.openDatePickerButton);
        openDatePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        groceryListView = findViewById(R.id.groceryListView);
        groceryListAdapter = new GroceryListAdapter(this,  new ArrayList<>());
        groceryListView.setAdapter(groceryListAdapter);

        mealPlansListView = findViewById(R.id.mealPlansListView);
        collapsedMealPlanAdapter = new CollapsedMealPlanAdapter(this, new ArrayList<>());
        mealPlansListView.setAdapter(collapsedMealPlanAdapter);

        noGroceryTextView = findViewById(R.id.noGroceryTextView);
        hideGroceryList();

        noMealPlansTextView = findViewById(R.id.noMealPlansTextView);
        hideMealPlansList();

        Button addShoppingBtn = findViewById(R.id.addShoppingBtn);
        addShoppingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ConfirmGroceryActivity.class);
                intent.putExtra("loadedGroceryList", new Gson().toJson(loadedGroceryList));
                startActivity(intent);
            }
        });
    }

    /**
     * Sets the color of the status bar to black.
     */
    private void setStatusBarColor() {
        Window appWindow = this.getWindow();
        appWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        appWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        appWindow.setStatusBarColor(ContextCompat.getColor(this,R.color.black));
    }

    /**
     * Opens a MaterialDatePicker to select a date range.
     */
    private void openDatePicker() {
        MaterialDatePicker<Pair<Long, Long>> dateRangePicker = MaterialDatePicker.Builder
                .dateRangePicker()
                .setTitleText("Select dates")
                .build();

        dateRangePicker.addOnPositiveButtonClickListener(
                selection -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                    String formattedStartDate = sdf.format(new Date(selection.first));
                    String formattedEndDate = sdf.format(new Date(selection.second));

                    String selectedDates = "Selected dates: " + formattedStartDate + " to "
                            + formattedEndDate;

                    loadMealPlans(formattedStartDate, formattedEndDate);
                });

        dateRangePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    /**
     * Loads meal plans within the specified date range.
     *
     * @param startDate The start date of the range.
     * @param endDate   The end date of the range.
     */
    private void loadMealPlans(String startDate, String endDate) {
        groceryLoader.loadGroceryPendingMealPlansWithMatchingDateRange(startDate, endDate, FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadedMealPlans = task.getResult();
                        if (loadedMealPlans != null && !loadedMealPlans.isEmpty()) {
                            collapsedMealPlanAdapter.clear();
                            collapsedMealPlanAdapter.addAll(loadedMealPlans);
                            collapsedMealPlanAdapter.notifyDataSetChanged();
                            showMealPlansList();
                            loadGroceryList(loadedMealPlans);
                        } else {
                            // No meal plans found
                            hideMealPlansList();
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception != null) {
                            ToastUtils.showLongToast(this, "Failed to " +
                                    "load meal plan!");
                        }
                    }
                });
    }

    /**
     * Loads the grocery list based on the fetched meal plans.
     *
     * @param loadedMealPlans The list of loaded meal plans.
     */
    private void loadGroceryList(List<MealPlan> loadedMealPlans){
        mealPlanLoader.computeIngredientsForMealPlans(loadedMealPlans)
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()){
                       loadedGroceryList = task.getResult();
                       if (loadedGroceryList != null && !loadedGroceryList.isEmpty()) {
                           List<Map.Entry<String, Float>> groceryEntries = new ArrayList<>(
                                   loadedGroceryList.entrySet());

                           // Update the adapter data
                           groceryListAdapter.clear();
                           groceryListAdapter.addAll(groceryEntries);
                           groceryListAdapter.notifyDataSetChanged();
                           showGroceryList();
                       } else {
                           hideGroceryList();
                       }
                   } else {
                       Exception exception = task.getException();
                       if (exception != null) {
                           ToastUtils.showLongToast(this, "Failed to " +
                                   "load grocery list!");
                       }
                   }
                });
    }

    /**
     * Shows the list of meal plans.
     */
    private void showMealPlansList() {
        mealPlansListView.setVisibility(View.VISIBLE);
        noMealPlansTextView.setVisibility(View.GONE);
    }

    /**
     * Hides the list of meal plans.
     */
    private void hideMealPlansList() {
        mealPlansListView.setVisibility(View.GONE);
        noMealPlansTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Shows the grocery list.
     */
    private void showGroceryList() {
        groceryListView.setVisibility(View.VISIBLE);
        noGroceryTextView.setVisibility(View.GONE);
    }

    /**
     * Hides the grocery list.
     */
    private void hideGroceryList(){
        groceryListView.setVisibility(View.GONE);
        noGroceryTextView.setVisibility(View.VISIBLE);
    }
}