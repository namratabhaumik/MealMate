package com.groupeleven.mealmate.Ingredients;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.groupeleven.mealmate.R;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * The InventoryAdapter class serves as the adapter for the RecyclerView in the InventoryActivity.
 * It is responsible for creating and managing view holders, binding data to the views,
 * and determining the number of items in the RecyclerView. Each item in the RecyclerView represents an ingredient.
 *
 * This adapter uses the IngredientsViewHolder class to define and manage the individual views
 * within the card_item.xml layout for each ingredient card.
 *
 * The adapter includes methods for creating view holders, binding data to views, and determining
 * the total number of items in the RecyclerView.
 */
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.IngredientsViewHolder> {
    private static final int BLUR_RADIUS = 15;
    private static final int BLUR_SAMPLING = 1;
    private List<IngredientsData> ingredientsDataList;

    /**
     * Constructs an InventoryAdapter with the provided list of IngredientsData objects.
     *
     * @param ingredientsDataList The list of IngredientsData objects to be displayed in the RecyclerView.
     */
    public InventoryAdapter(List<IngredientsData> ingredientsDataList) {
        this.ingredientsDataList = ingredientsDataList;
    }

    /**
     * The IngredientsViewHolder class represents the view holder for individual items in the RecyclerView.
     * It holds references to the views within the card_item.xml layout, allowing efficient access and modification.
     */
    public class IngredientsViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView1;
        TextView textView;
        CardView cardView;

        TextView unitView;

        EditText editText;

        /**
         * Constructs an IngredientsViewHolder and initializes the views within the card_item.xml layout.
         *
         * @param itemView The view representing an item in the RecyclerView.
         */
        public IngredientsViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);

            textView = cardView.findViewById(R.id.ingt_name);

            imgView1 = cardView.findViewById(R.id.ingt_img_id);

            unitView = cardView.findViewById(R.id.ingt_qty_unit);

            editText = cardView.findViewById(R.id.ingt_qty_num);
        }
    }

    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new IngredientsViewHolder that holds the view for an item.
     */
    @NonNull
    @Override
    public IngredientsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new IngredientsViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder   The ViewHolder that should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull IngredientsViewHolder holder, int position) {
        IngredientsData ingredientsData = ingredientsDataList.get(position);
        holder.textView.setText(ingredientsData.getIngredientName().toUpperCase());
        Log.d("imgurl", ingredientsData.getIngredientImg());

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.transform(new MultiTransformation<>(new BlurTransformation(BLUR_RADIUS, BLUR_SAMPLING)));

        Glide.with(holder.cardView)
                .load(ingredientsData.getIngredientImg())
                .apply(requestOptions)
                .into(holder.imgView1);
        holder.unitView.setText(ingredientsData.getUnit());
        holder.editText.setText(String.valueOf(ingredientsData.getIngredientQty()));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in the RecyclerView.
     */
    @Override
    public int getItemCount() {
        return ingredientsDataList.size();
    }
}
