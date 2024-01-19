package com.groupeleven.mealmate.recommendations;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.groupeleven.mealmate.R;

import java.util.Map;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Adapter for the RecyclerView in the recommendations feature.
 */
public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.RecommendViewHolder>{
    final int BLUR_RADIUS = 15;
    final int BLUR_SAMPLING = 1;
    final int MAX_IMAGE_SIZE_BYTES = 5000000;
    final int MIN_IMAGE_PATH_LENGTH = 20;

    public List<Map<String, Object>> recommendRecipeList;

    public RecommendAdapter(List<Map<String, Object>> recommendRecipeList){
        this.recommendRecipeList = recommendRecipeList;
    }
    private OnFavoriteClickListener onFavoriteClickListener;

    /**
     * Interface for handling favorite icon clicks.
     */
    public interface OnFavoriteClickListener {
        /**
         * Called when the favorite icon is clicked.
         *
         * @param position The position of the clicked item in the RecyclerView.
         */
        void onFavoriteClick(int position);
    }

    /**
     * Sets the listener for favorite icon clicks.
     *
     * @param listener The listener to set.
     */
    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.onFavoriteClickListener = listener;
    }

    /**
     * ViewHolder class for the RecyclerView items.
     */
    public class RecommendViewHolder extends RecyclerView.ViewHolder{

        ImageView imgView;

        TextView txtView;

        CardView cardView;
        ImageView favoriteIcon;

        /**
         * Constructs a new RecommendViewHolder.
         *
         * @param itemView The item view for the ViewHolder.
         */
        public RecommendViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.itemCard);
            imgView = cardView.findViewById(R.id.itemImageView);
            txtView = cardView.findViewById(R.id.recommendRecipeName);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
        }
    }

    /**
     * Creates a new ViewHolder.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder.
     */
    @NonNull
    @Override
    public RecommendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommending_recipe_card, parent, false);
        return new RecommendViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull RecommendAdapter.RecommendViewHolder holder, int position) {
        Map<String, Object> recipe = recommendRecipeList.get(position);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.transform(new MultiTransformation<>(new BlurTransformation(BLUR_RADIUS, BLUR_SAMPLING)));

        String imagePath = recipe.get("imageUrl").toString();
        if (imagePath.length() > MIN_IMAGE_PATH_LENGTH || imagePath.contentEquals("")) {
            imagePath = "placeHolder.jpg";
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference("uploads");
        StorageReference imageRef = storageRef.child(imagePath);
        imageRef.getBytes(MAX_IMAGE_SIZE_BYTES).addOnCompleteListener(task -> {
            byte[] image = task.getResult();
            Glide.with(holder.cardView)
                    .load(image)
                    .apply(requestOptions)
                    .into(holder.imgView);
        }).addOnFailureListener(e -> {
            Log.d("EXCEPTION :", e.getMessage());
        });

        holder.txtView.setText(recipe.get("recipeName").toString());

        final int FAVORITE_FILLED_ICON = R.drawable.favorite_filled;
        final int FAVORITE_EMPTY_ICON = R.drawable.favorite_empty;

        if ((boolean) recipe.get("isFavorite")) {
            holder.favoriteIcon.setImageResource(FAVORITE_FILLED_ICON);
        } else {
            holder.favoriteIcon.setImageResource(FAVORITE_EMPTY_ICON);
        }

        holder.favoriteIcon.setOnClickListener(v -> {
            if (onFavoriteClickListener != null) {
                onFavoriteClickListener.onFavoriteClick(holder.getAdapterPosition());
            }
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return recommendRecipeList.size();
    }

    /**
     * Sets new data for the adapter.
     *
     * @param newData The new data to set.
     */
    public void setData(List<Map<String, Object>> newData) {
        recommendRecipeList = newData;
    }
}
