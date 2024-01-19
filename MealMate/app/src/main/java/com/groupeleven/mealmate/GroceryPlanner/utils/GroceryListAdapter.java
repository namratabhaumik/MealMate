package com.groupeleven.mealmate.GroceryPlanner.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.groupeleven.mealmate.R;

import java.util.List;
import java.util.Map;

/**
 * ArrayAdapter for displaying grocery items in a ListView.
 */
public class GroceryListAdapter extends ArrayAdapter<Map.Entry<String, Float>> {

    private final Context context;

    /**
     * Constructor for the GroceryListAdapter.
     *
     * @param context         The context in which the adapter will be used
     * @param groceryEntries  List of grocery items as Map entries (String - Item name, Float - Quantity)
     */
    public GroceryListAdapter(Context context, List<Map.Entry<String, Float>> groceryEntries) {
        super(context, 0, groceryEntries);
        this.context = context;
    }

    /**
     * Provides the View for each grocery list item in the ListView.
     *
     * @param position     The position of the item within the adapter's data set
     * @param convertView  The old view to reuse, if possible
     * @param parent       The parent that this view will eventually be attached to
     * @return             The View corresponding to the data at the specified position
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Map.Entry<String, Float> groceryItem = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.grocery_list_item, parent, false);
        }

        TextView ingredientNameTextView = convertView.findViewById(R.id.ingredientNameTextView);
        TextView quantityTextView = convertView.findViewById(R.id.quantityTextView);

        ingredientNameTextView.setText(groceryItem.getKey());
        quantityTextView.setText(String.valueOf(groceryItem.getValue()));

        return convertView;
    }
}