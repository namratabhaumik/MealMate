package com.groupeleven.mealmate.MealPlanner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.groupeleven.mealmate.Common.utils.ToastUtils;
import com.groupeleven.mealmate.MealPlanner.models.MealPlan;
import com.groupeleven.mealmate.MealPlanner.models.MealRecipe;
import com.groupeleven.mealmate.MealPlanner.utils.MealPlanLoader;
import com.groupeleven.mealmate.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import java.time.LocalDate;
import java.util.List;
import android.content.Intent;
import java.util.ArrayList;

import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.groupeleven.mealmate.MealPlanner.utils.CalendarUtils;
import com.groupeleven.mealmate.MealPlanner.utils.CalendarAdapter;
import static com.groupeleven.mealmate.MealPlanner.utils.CalendarUtils.daysInWeekArray;
import static com.groupeleven.mealmate.MealPlanner.utils.CalendarUtils.monthYearFromDate;

import com.groupeleven.mealmate.MealPlanner.models.Meal;
import com.groupeleven.mealmate.MealPlanner.utils.MealPlanAdapter;

/**
 * Fragment handling meal planning functionality, including UI setup and user interactions.
 */
public class MealPlannerFragment extends Fragment implements CalendarAdapter.OnItemListener {
    private View view;
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private RecyclerView mealPlanRecyclerView;
    private List<Meal> loadedMeals;
    private MealPlan loadMealPlan;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();
    MealPlanLoader mealPlanLoader = new MealPlanLoader(FirebaseFirestore.getInstance());
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_meal_planner, container, false);

        Window appWindow = getActivity().getWindow();
        appWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        appWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        appWindow.setStatusBarColor(ContextCompat.getColor(getActivity(),R.color.black));

        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        monthYearText = view.findViewById(R.id.monthYearTV);

        ImageButton previousWeekBtn = view.findViewById(R.id.previousWeekBtn);
        previousWeekBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousWeekAction(v);
            }
        });

        ImageButton nextWeekBtn = view.findViewById(R.id.nextWeekBtn);
        nextWeekBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextWeekAction(v);
            }
        });

        Button newMealBtn = view.findViewById(R.id.newMealBtn);
        newMealBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newMealAction(v);
            }
        });

        CalendarUtils.selectedDate = LocalDate.now();
        setWeekView();
        loadMealPlan(CalendarUtils.selectedDate);

        return view;
    }

    /**
     * Sets the week view in the calendar RecyclerView based on the selected date.
     */
    private void setWeekView() {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> days = daysInWeekArray(CalendarUtils.selectedDate);
        CalendarAdapter calendarAdapter = new CalendarAdapter(days, this);
        RecyclerView.LayoutManager layoutManager =  new GridLayoutManager(requireContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    /**
     * Loads the meal plan for the selected date and updates the UI accordingly.
     *
     * @param selectedDate The date for which the meal plan is to be loaded.
     */
    public void loadMealPlan(LocalDate selectedDate) {
        String userEmail = currentUser.getEmail();
        mealPlanLoader.loadMealPlanWithMatchingUserAndDate(userEmail, String.valueOf(selectedDate))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadMealPlan = task.getResult();
                        mealPlanRecyclerView = view.findViewById(R.id.mealPlanRecyclerView);
                        TextView noMealPlanText = view.findViewById(R.id.noMealPlanText);
                        if (loadMealPlan != null) {
                            loadedMeals = loadMealPlan.getMeals();
                            mealPlanRecyclerView = view.findViewById(R.id.mealPlanRecyclerView);
                            mealPlanRecyclerView.setLayoutManager(new LinearLayoutManager(
                                    requireContext()));
                            updateMealPlanUI();
                        }
                        mealPlanRecyclerView.setVisibility(loadMealPlan != null ? View.VISIBLE : View.GONE);
                        noMealPlanText.setVisibility(loadMealPlan == null ? View.VISIBLE : View.GONE);
                    } else {
                        // TODO: Handle errors
                        Exception exception = task.getException();
                        if (exception != null) {
                            System.err.println("Error: " + exception.getMessage());
                        }
                    }
                });
    }

    public void previousWeekAction(View view) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusWeeks(1);
        setWeekView();
        loadMealPlan(CalendarUtils.selectedDate);
    }

    public void nextWeekAction(View view) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusWeeks(1);
        setWeekView();
        loadMealPlan(CalendarUtils.selectedDate);
    }

    @Override
    public void onItemClick(int position, LocalDate date) {
        CalendarUtils.selectedDate = date;
        setWeekView();
        loadMealPlan(CalendarUtils.selectedDate);
    }
    private ActivityResultLauncher<Intent> startForNewMealResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                loadMealPlan(CalendarUtils.selectedDate);
            });

    /**
     * Action to perform when adding a new meal.
     *
     * @param view The view clicked to add a new meal.
     */
    public void newMealAction(View view) {
        if (!CalendarUtils.selectedDate.isBefore(LocalDate.now())){
            Intent intent = new Intent(requireContext(), NewMealActivity.class);
            intent.putExtra("selectedDate", CalendarUtils.selectedDate.toString());
            startForNewMealResult.launch(intent);
        } else {
            ToastUtils.showLongToast(requireContext(), "Cannot add meal in past dates!");
        }
    }

    /**
     * Updates the UI to reflect changes in the meal plan after any modifications.
     */
    private void updateMealPlanUI() {
        MealPlanAdapter updatedAdapter = new MealPlanAdapter(requireContext(), loadedMeals);
        updatedAdapter.setOnRecipeUpdateListener(new MealPlanAdapter
                .OnRecipeUpdateListener() {
            @Override
            public void onUpdateRecipe(MealRecipe recipe, String newServingSize) {
                if (!mealPlanDateIsInPresentOrFuture(loadMealPlan)){
                    ToastUtils.showLongToast(requireContext(), "Past Meal Plan cannot " +
                            "be updated!");
                    return;
                }
                for (Meal meal : loadedMeals) {
                    for (MealRecipe r : meal.getMealRecipes()) {
                        if (r.equals(recipe)) {
                            r.setCustomServingSize(Integer.parseInt(
                                    newServingSize));
                            mealPlanLoader.updateExistingMealPlan(loadMealPlan)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            updateMealPlanUI();
                                        }
                                    });
                            break;
                        }
                    }
                }
            }

            @Override
            public void onDeleteRecipe(MealRecipe recipe) {
                if (!mealPlanDateIsInPresentOrFuture(loadMealPlan)){
                    ToastUtils.showLongToast(requireContext(), "Past Meal Plan cannot " +
                            "be deleted!");
                    return;
                }
                for (Meal meal : loadedMeals) {
                    if (meal.getMealRecipes().remove(recipe)) {
                        // Check if there are no more recipes in the meal,
                        // and delete the meal if true
                        if (meal.getMealRecipes().isEmpty()) {
                            loadedMeals.remove(meal);
                            // If loadedMeals is empty, then delete the meal plan
                            if (loadedMeals.isEmpty()) {
                                mealPlanLoader.deleteMealPlan(loadMealPlan.getMealPlanId())
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                loadMealPlan(CalendarUtils.selectedDate);
                                            }
                                        });
                                break;
                            }
                        }
                        mealPlanLoader.updateExistingMealPlan(loadMealPlan)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        updateMealPlanUI();
                                    }
                                });
                        break;
                    }
                }
            }
        });
        mealPlanRecyclerView.setAdapter(updatedAdapter);
    }

    /**
     * Checks if the meal plan date is in the present or future.
     *
     * @param mealPlan The meal plan to check.
     * @return True if the meal plan date is in the present or future, otherwise false.
     */
    private boolean mealPlanDateIsInPresentOrFuture(MealPlan mealPlan) {
        LocalDate mealPlanDate = LocalDate.parse(mealPlan.getMealDate());
        LocalDate currentDate = LocalDate.now();
        return !mealPlanDate.isBefore(currentDate);
    }
}
