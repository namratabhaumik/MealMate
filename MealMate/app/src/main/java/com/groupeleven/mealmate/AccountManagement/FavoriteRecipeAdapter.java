package com.groupeleven.mealmate.AccountManagement;

import android.content.Intent;
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
import com.groupeleven.mealmate.InstructionsActivity;
import com.groupeleven.mealmate.R;
import com.groupeleven.mealmate.SharedValues;
import com.groupeleven.mealmate.Recipe.models.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Adapter class for displaying favorite recipes in a RecyclerView.
 */
public class FavoriteRecipeAdapter extends RecyclerView.Adapter<FavoriteRecipeAdapter.FavoriteRecipeViewHolder>{
    private static final int BLUR_RADIUS = 15;
    private static final int BLUR_SAMPLING = 1;
    private static final int MAX_IMAGE_SIZE_BYTES = 5000000;
    private static final int MIN_IMAGE_PATH_LENGTH = 20;
    List<Map<String, Object>> favRecipeList;

    /**
     * Constructs a FavoriteRecipeAdapter with the provided list of favorite recipes.
     *
     * @param favRecipeList List of favorite recipes to be displayed.
     */
    public FavoriteRecipeAdapter(List<Map<String, Object>> favRecipeList){
        this.favRecipeList = favRecipeList;
    }

    /**
     * ViewHolder class for holding the views of individual favorite recipes.
     */
    public class FavoriteRecipeViewHolder extends RecyclerView.ViewHolder{

        ImageView imgView;
        TextView textView;
        CardView cardView;

        public FavoriteRecipeViewHolder(View itemView){
            super(itemView);
            cardView = itemView.findViewById(R.id.itemCard);
            textView = itemView.findViewById(R.id.favoriteRecipeName);
            imgView = itemView.findViewById(R.id.itemImageView);
        }
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent
     * an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public FavoriteRecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_recipes_card, parent, false);
        return new FavoriteRecipeViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method
     * updates the contents of the itemView to reflect the item at the given position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull FavoriteRecipeViewHolder holder, int position) {
        Map<String, Object> recipe = favRecipeList.get(position);

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
            Log.d("EXCEPTION:", e.getMessage());
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Recipe currentRecipe = getCurrentRecipe(recipe);
                SharedValues.getInstance().setInstructions((ArrayList<String>) recipe.get("instructions"));
                SharedValues.getInstance().setServingSize(Integer.parseInt((String) recipe.get("servingSize")));
                SharedValues.getInstance().setCurrentRecipe(currentRecipe);
                Intent instructionActivity = new Intent(v.getContext(), InstructionsActivity.class);
                v.getContext().startActivity(instructionActivity);
            }
        });

        holder.textView.setText(recipe.get("recipeName").toString());
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    public Recipe getCurrentRecipe(Map<String, Object> tappedRecipe){
        Recipe recipe = new Recipe();
        recipe.setUserEmail(tappedRecipe.get("userEmail").toString());
        recipe.setImageUrl(tappedRecipe.get("imageUrl").toString());
        recipe.setIngredients((ArrayList<Map<String, Object>>) tappedRecipe.get("ingredients"));
        recipe.setVegetarian(Boolean.getBoolean(tappedRecipe.get("isVegetarian").toString()));
        recipe.setTime(tappedRecipe.get("time").toString());
        recipe.setInstructions((ArrayList<String>) tappedRecipe.get("instructions"));
        recipe.setRecipeDescription(tappedRecipe.get("recipeDescription").toString());
        recipe.setServingSize(tappedRecipe.get("servingSize").toString());
        return recipe;
    }

    @Override
    public int getItemCount() {
        return favRecipeList.size();
    }
}
