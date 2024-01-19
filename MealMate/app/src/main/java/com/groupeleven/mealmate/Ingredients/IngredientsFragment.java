package com.groupeleven.mealmate.Ingredients;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.groupeleven.mealmate.Ingredients.InventoryActivity;
import com.groupeleven.mealmate.R;

/**
 * A {@link Fragment} that launches the {@link InventoryActivity} for ingredient management.
 */
public class IngredientsFragment extends Fragment {
    private static final int INVENTORY_ACTIVITY_REQ_CODE = 123;

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to. The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a
     *                           previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.inventory_mgmt, container, false);
        Intent inventoryActivity = new Intent(this.getContext(), InventoryActivity.class);
        startActivityForResult(inventoryActivity, INVENTORY_ACTIVITY_REQ_CODE);

        return view;
    }
}