package com.groupeleven.mealmate.Ingredients;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.groupeleven.mealmate.R;

import java.util.List;

/**
 * This class serves as the adapter for the RecyclerView in the IngredientsActivity.
 * It is responsible for binding the data of IngredientsData objects to the corresponding views in the RecyclerView.
 * The adapter extends RecyclerView.Adapter and uses the IngredientViewHolder class to hold references to individual item views.
 */
public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder> {

    private List<IngredientsData> ingredientsData;

    /**
     * Constructor for the IngredientsAdapter class.
     *
     * @param ingredientsData List of IngredientsData objects to be displayed.
     */
    public IngredientsAdapter(List<IngredientsData> ingredientsData) {
        this.ingredientsData = ingredientsData;
    }

    /**
     * ViewHolder class representing individual item views in the RecyclerView.
     * It holds references to the various UI elements inside the card view for each ingredient.
     */
    public class IngredientViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        CardView cardView;
        EditText editText_qty_num;
        TextView editText_qty_unit;

        /**
         * Constructor for the IngredientViewHolder class.
         *
         * @param itemView The root view of the individual item.
         */
        public IngredientViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            textView = cardView.findViewById(R.id.ingt_name);
            editText_qty_num = cardView.findViewById(R.id.ingt_qty_num);
            editText_qty_unit = cardView.findViewById(R.id.ingt_qty_unit);
        }
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selected_ingredients, parent, false);
        return new IngredientViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        IngredientsData ingredientsData1 = ingredientsData.get(position);
        String ingredientName = ingredientsData1.getIngredientName();
        holder.textView.setText(ingredientName.toUpperCase()+"");
        holder.editText_qty_num.setText(ingredientsData1.getIngredientQty()+"");
        holder.editText_qty_unit.setText(ingredientsData1.getUnit()+"");
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in the data set.
     */
    @Override
    public int getItemCount() {
        return ingredientsData.size();
    }
}
