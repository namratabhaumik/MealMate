package com.groupeleven.mealmate.MealPlanner.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.groupeleven.mealmate.Common.constants.CommonConstants;
import com.groupeleven.mealmate.MealPlanner.RecipeSearchActivity;
import com.groupeleven.mealmate.Recipe.models.Recipe;
import com.groupeleven.mealmate.R;

import java.util.List;

/**
 * Adapter class for displaying recipe search results in a grid or list view.
 */
public class RecipeSearchAdapter extends BaseAdapter {

    private List<Recipe> recipeList;
    private Context context;

    /**
     * Constructs the RecipeSearchAdapter.
     *
     * @param context    The context of the adapter.
     * @param recipeList The list of recipes to display.
     */
    public RecipeSearchAdapter(Context context, List<Recipe> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
    }

    @Override
    public int getCount() {
        return recipeList.size();
    }

    @Override
    public Object getItem(int position) {
        return recipeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.recipe_card_item,
                    parent, false);
        }

        Recipe recipe = recipeList.get(position);

        ImageView imageView = convertView.findViewById(R.id.recipeImageView);
        TextView titleTextView = convertView.findViewById(R.id.recipeName);
        TextView servingTextView = convertView.findViewById(R.id.servingSize);
        Button addBtn = convertView.findViewById(R.id.addBtnRecipeCard);

        String imagePath = recipe.getImageUrl();
        if (imagePath.length() > CommonConstants.IMAGE_PATH_MAX_LENGTH
                || imagePath.contentEquals("")) {
            imagePath = "placeHolder.jpg";
        }

        // Loading recipe image using FirebaseStorage and Glide
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference("uploads");
        StorageReference imageRef = storageRef.child(imagePath);
        imageRef.getBytes(CommonConstants.MAX_FIREBASE_IMAGE_DOWNLOAD_SIZE_BYTES)
                .addOnCompleteListener(task -> {
            byte[] image = task.getResult();
            Glide.with(this.context).load(image).into(imageView);
        });

        titleTextView.setText(recipe.getRecipeName());
        servingTextView.setText(String.valueOf(recipe.getServingSize()));

        addBtn.setOnClickListener(v -> {
            if (context instanceof RecipeSearchActivity) {
                ((RecipeSearchActivity) context).handleRecipeSelect(recipe);
            }
        });

        return convertView;
    }
}
