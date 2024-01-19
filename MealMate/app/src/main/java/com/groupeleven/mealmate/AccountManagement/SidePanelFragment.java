package com.groupeleven.mealmate.AccountManagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.groupeleven.mealmate.GroceryPlanner.GroceryPlannerActivity;
import com.groupeleven.mealmate.R;
import com.groupeleven.mealmate.Recipe.MyRecipesActivity;
import com.groupeleven.mealmate.SharedRecipesActivity;
import com.groupeleven.mealmate.SplashActivity;

/**
 * Fragment class representing the side panel navigation in the MealMate application.
 * This panel provides a list of account management options, allowing users to navigate
 * to different activities such as the grocery planner, favorite recipes, and more.
 */
public class SidePanelFragment extends Fragment {
    private static final int POSITION_GROCERY_PLANNER = 0;
    private static final int POSITION_FAVORITE_RECIPES = 1;
    private static final int POSITION_MY_RECIPES = 2;
    private static final int POSITION_SHARED_RECIPES = 3;
    private static final int POSITION_YOUR_PROFILE = 4;
    private static final int POSITION_DOWNLOADS = 5;
    private static final int POSITION_LOG_OUT = 6;

    private ListView listView;
    private ArrayAdapter<String> adapter;

    /**
     * Inflates the layout for the side panel fragment and initializes UI components.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to. The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
     *                           saved state as given here.
     * @return The root view for the fragment's layout.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.side_panel_layout, container, false);

        listView = view.findViewById(R.id.list_view);

        String[] accountManagementArray = getResources().getStringArray(R.array.accnt_mgmt);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, accountManagementArray);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, itemView, position, id) -> onListItemClick(position));
        return view;
    }

    /**
     * Handles the item click events in the account management list and navigates to
     * the corresponding activity based on the selected position.
     *
     * @param position The position of the clicked item in the account management list.
     */
    private void onListItemClick(int position) {
        switch (position) {
            case POSITION_GROCERY_PLANNER:
                openActivity(GroceryPlannerActivity.class);
                break;
            case POSITION_FAVORITE_RECIPES:
                openActivity(FavoriteRecipesActivity.class);
                break;
            case POSITION_MY_RECIPES:
                openActivity(MyRecipesActivity.class);
                break;
            case POSITION_SHARED_RECIPES:
                openActivity(SharedRecipesActivity.class);
                break;
            case POSITION_YOUR_PROFILE:
                openActivity(YourProfileActivity.class);
                break;
            case POSITION_DOWNLOADS:
                openActivity(DownloadsActivity.class);
                break;
            case POSITION_LOG_OUT:
                logout();
                break;
        }
    }

    /**
     * Opens the specified activity using an Intent.
     *
     * @param activityClass The target activity class to open.
     */
    private void openActivity(Class<?> activityClass) {
        startActivity(new Intent(getActivity(), activityClass));
    }

    /**
     * Logs out the user by updating shared preferences and navigating to the splash screen.
     */
    private void logout() {
        SharedPreferences sp = getActivity().getSharedPreferences("MealMateSharedPreferences", Context.MODE_PRIVATE);
        sp.edit().putBoolean("isSignedIn", false).apply();
        startSplashActivity();
        showToast("Logged Out");
    }

    /**
     * Starts the splash activity to provide a smooth transition after logging out.
     */
    private void startSplashActivity() {
        Intent nextActivityIntent = new Intent(getActivity().getApplicationContext(), SplashActivity.class);
        getContext().startActivity(nextActivityIntent);
    }

    /**
     * Displays a short toast message with the specified text.
     *
     * @param message The text to be displayed in the toast.
     */
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
