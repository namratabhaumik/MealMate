package com.groupeleven.mealmate.MealPlanner.utils;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.groupeleven.mealmate.InstructionsActivity;
import com.groupeleven.mealmate.R;

import com.groupeleven.mealmate.Recipe.models.Recipe;
import com.groupeleven.mealmate.SharedValues;

import java.util.List;

import android.content.Intent;
import android.view.View;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.groupeleven.mealmate.MealPlanner.models.Meal;
import com.groupeleven.mealmate.MealPlanner.models.MealRecipe;

/**
 * Adapter for displaying a list of meals within a meal plan in a RecyclerView.
 */
public class MealPlanAdapter extends RecyclerView.Adapter<MealPlanAdapter.MealViewHolder> {
    private List<Meal> meals;
    private AlertDialog alertDialog;
    private Context context;

    public MealPlanAdapter(Context context, List<Meal> meals) {
        this.context = context;
        this.meals = meals;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.meal_item, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = meals.get(position);
        holder.bind(meal);
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    public class MealViewHolder extends RecyclerView.ViewHolder {
        private TextView mealNameTextView;
        private LinearLayout recipesLayout;
        MealPlanLoader mealPlanLoader = new MealPlanLoader(FirebaseFirestore.getInstance());

        /**
         * ViewHolder class representing individual meal items in the RecyclerView.
         */
        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            mealNameTextView = itemView.findViewById(R.id.mealNameTextView);
            recipesLayout = itemView.findViewById(R.id.recipesLayout);
        }

        public void bind(Meal meal) {
            mealNameTextView.setText(meal.getMealType().getName());
            recipesLayout.removeAllViews();

            for (MealRecipe mealRecipe : meal.getMealRecipes()) {
                View recipeView = LayoutInflater.from(itemView.getContext()).inflate(
                        R.layout.recipe_item, recipesLayout, false);
                TextView recipeNameTextView = recipeView.findViewById(R.id.recipeNameTextView);
                TextView servingSizeTextView = recipeView.findViewById(R.id.servingSizeTextView);
                ImageView recipeImageView = recipeView.findViewById(R.id.recipeImageView);
                ImageButton editRecipeButton = recipeView.findViewById(R.id.editRecipeButton);

                mealPlanLoader.loadRecipe(mealRecipe.getRecipeId())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Recipe recipe = task.getResult();
                                if (recipe != null) {
                                    recipeNameTextView.setText(recipe.getRecipeName());
                                    servingSizeTextView.setText(
                                            "Serving Size: " + mealRecipe.getCustomServingSize()
                                    );

                                    String imagePath = recipe.getImageUrl();
                                    if (imagePath.length() > 20 || imagePath.contentEquals("")) {
                                        imagePath = "placeHolder.jpg";
                                    }

                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                    StorageReference storageRef = storage.getReference("uploads");
                                    StorageReference imageRef = storageRef.child(imagePath);
                                    imageRef.getBytes(5000000).addOnCompleteListener(imgTask -> {
                                        byte[] image = imgTask.getResult();
                                        Glide.with(recipeView.getContext()).load(image).into(recipeImageView);
                                    });

                                    // Opening "InstructionsActivity" when clicked on Meal Plan item
                                    recipeView.setOnClickListener(view -> {
                                        SharedValues.getInstance().setCurrentRecipe(recipe);
                                        SharedValues.getInstance().setInstructions(recipe.getInstructions());
                                        SharedValues.getInstance().setServingSize(mealRecipe.getCustomServingSize());
                                        SharedValues.getInstance().setInstructionsActivityCaller("MealPlannerFragment");
                                        Intent instructionActivity = new Intent(view.getContext(),
                                                InstructionsActivity.class);
                                        view.getContext().startActivity(instructionActivity);
                                    });

                                } else {
                                    System.out.println("Recipe not found for ID: "
                                            + mealRecipe.getRecipeId());
                                }
                            } else {
                                Exception exception = task.getException();
                                if (exception != null) {
                                    System.err.println("Error: " + exception.getMessage());
                                }
                            }
                        });

                editRecipeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View dialogView = LayoutInflater.from(context).inflate(
                                R.layout.popup_dialog, null);

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setView(dialogView);

                        EditText servingSizeEditText = dialogView.findViewById(
                                R.id.servingSizeEditText);
                        Button updateButton = dialogView.findViewById(R.id.updateButton);
                        Button deleteButton = dialogView.findViewById(R.id.deleteButton);
                        Button closeButton = dialogView.findViewById(R.id.closeButton);

                        updateButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String newServingSize = servingSizeEditText.getText().toString();
                                recipeUpdateListener.onUpdateRecipe(mealRecipe, newServingSize);
                                alertDialog.dismiss();
                            }
                        });
                        deleteButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                recipeUpdateListener.onDeleteRecipe(mealRecipe);
                                alertDialog.dismiss();
                            }
                        });
                        closeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (alertDialog != null) {
                                    alertDialog.dismiss();
                                }
                            }
                        });
                        alertDialog = builder.create();
                        alertDialog.show();
                    }
                });

                recipesLayout.addView(recipeView);
            }
        }
    }

    private OnRecipeUpdateListener recipeUpdateListener;

    public interface OnRecipeUpdateListener {
        void onUpdateRecipe(MealRecipe recipe, String newServingSize);
        void onDeleteRecipe(MealRecipe recipe);
    }

    public void setOnRecipeUpdateListener(OnRecipeUpdateListener listener) {
        this.recipeUpdateListener = listener;
    }
}