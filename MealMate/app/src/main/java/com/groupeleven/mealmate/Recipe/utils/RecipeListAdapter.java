package com.groupeleven.mealmate.Recipe.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.groupeleven.mealmate.Common.constants.CommonConstants;
import com.groupeleven.mealmate.InstructionsActivity;
import com.groupeleven.mealmate.R;
import com.groupeleven.mealmate.Recipe.models.Recipe;
import com.groupeleven.mealmate.SharedValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the RecyclerView adapter for displaying a list of recipes.
 */
public class RecipeListAdapter extends RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder> {
    ArrayList<Recipe> recipes;
    Context activityContext;
    String adapterCaller;

    /**
     * Map that stores text values based on adapterCaller for the first item in the list.
     * <p>
     * This map is used to set specific text values for firstText, secondText, and thirdText
     * based on different adapterCaller values when binding the first item in the RecyclerView.
     * </p>
     * <p>
     * Key: String representing the adapterCaller value.
     * Value: List of Strings containing text values for firstText, secondText, and thirdText
     * respectively.
     * </p>
     */
    private static final Map<String, List<String>> TEXT_VALUES_MAP = new HashMap<>();

    static {
        TEXT_VALUES_MAP.put("SharedRecipesActivity",
                Arrays.asList("Recipes ", "Shared ", " With you!"));
        TEXT_VALUES_MAP.put("HomeFragment",
                Arrays.asList("Meal", "Mate", "\uD83C\uDF55"));
        TEXT_VALUES_MAP.put("MyRecipesActivity",
                Arrays.asList("My ", "Recipes ", "\uD83C\uDF72"));
    }

    /**
     * Represents a single item view for a recipe within the RecyclerView.
     */
    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImageView;
        TextView recipeName;
        TextView recipeDescription;
        RelativeLayout titleRL;
        TextView firstText;
        TextView secondText;
        TextView thirdText;
        RelativeLayout parentRL;
        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImageView = itemView.findViewById(R.id.itemImageView);
            recipeName = itemView.findViewById(R.id.recipeName);
            recipeDescription = itemView.findViewById(R.id.recipeDescription);
            titleRL = itemView.findViewById(R.id.titleRL);
            parentRL = itemView.findViewById(R.id.parentRL);
            firstText = itemView.findViewById(R.id.firstText);
            secondText = itemView.findViewById(R.id.secondText);
            thirdText = itemView.findViewById(R.id.thirdText);
        }
    }

    /**
     * Constructs a RecipeListAdapter.
     *
     * @param recipes         The list of recipes to display.
     * @param activityContext The context of the activity using the adapter.
     * @param adapterCaller   Describes the source/context of the adapter initiation.
     */
    public RecipeListAdapter(ArrayList<Recipe> recipes, Context activityContext,
                             String adapterCaller) {
        this.recipes = recipes;
        this.activityContext = activityContext;
        this.adapterCaller = adapterCaller;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.home_screen_card, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        // "position = 0" holds the logic for how to display the first item in the list.
        if (position == 0) {
            List<String> textValues = TEXT_VALUES_MAP.getOrDefault(adapterCaller,
                    Arrays.asList("", "", ""));
            if (textValues != null){
                holder.firstText.setText(textValues.get(0));
                holder.secondText.setText(textValues.get(1));
                holder.thirdText.setText(textValues.get(2));
            }
            holder.recipeName.setVisibility(View.GONE);
            holder.recipeDescription.setVisibility(View.GONE);
            holder.recipeImageView.setVisibility(View.GONE);
            holder.titleRL.setVisibility(View.VISIBLE);
            holder.parentRL.setBackground(null);
        } else {
            holder.recipeName.setVisibility(View.VISIBLE);
            holder.recipeDescription.setVisibility(View.VISIBLE);
            holder.recipeImageView.setVisibility(View.VISIBLE);
            holder.titleRL.setVisibility(View.GONE);
            holder.parentRL.setBackground(
                    activityContext.getApplicationContext().getDrawable(
                            R.drawable.home_card_background));

            String imagePath = recipes.get(position - 1).getImageUrl();
            if (imagePath.length() > CommonConstants.IMAGE_PATH_MAX_LENGTH
                    || imagePath.contentEquals("")) {
                imagePath = "placeHolder.jpg";
            }

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference("uploads");
            StorageReference imageRef = storageRef.child(imagePath);
            imageRef.getBytes(CommonConstants.MAX_FIREBASE_IMAGE_DOWNLOAD_SIZE_BYTES)
                    .addOnCompleteListener(task -> {
                byte[] image = task.getResult();
                Glide.with(activityContext).load(image).into(holder.recipeImageView);
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("EXCEPTION :" , e.getMessage());
                }
            });

            Recipe currentRecipe = recipes.get(position-1);
            holder.recipeName.setText(currentRecipe.getRecipeName());
            holder.recipeDescription.setText(currentRecipe.getRecipeDescription());
        }

        holder.parentRL.setOnClickListener(view -> {
            if (position != 0) {
                Recipe currentRecipe = recipes.get(position-1);
                SharedValues.getInstance().setInstructions(currentRecipe.getInstructions());
                SharedValues.getInstance().setServingSize(Integer.parseInt(
                        currentRecipe.getServingSize()));
                SharedValues.getInstance().setCurrentRecipe(currentRecipe);
                Intent instructionActivity = new Intent(this.activityContext,
                        InstructionsActivity.class);
                ((Activity) activityContext).startActivityForResult(
                        instructionActivity, CommonConstants.INSTRUCTIONS_REQUEST_CODE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size() + 1;
    }
}
