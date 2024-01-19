package com.groupeleven.mealmate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.groupeleven.mealmate.Common.utils.ToastUtils;
import com.groupeleven.mealmate.GroceryPlanner.utils.GroceryListAdapter;
import com.groupeleven.mealmate.InventoryManagement.utils.InventoryLoader;
import com.groupeleven.mealmate.Recipe.models.Recipe;

import java.util.AbstractMap;
import java.util.ArrayList;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

public class InstructionsAdapter extends RecyclerView.Adapter<InstructionsAdapter.InstructionsViewHolder> {
    List<String> instructions;
    Context context;

    private static final int POSITION_ONE = 0;
    private static final int FILE_NAME_LIMIT = 30;
    private static final int DOWNLOAD_SIZE_LIMIT = 5000000;

    InstructionsAdapter(List<String> instructions, Context context) {
        this.instructions = instructions;
        this.context = context;
    }
    @NonNull
    @Override
    public InstructionsAdapter.InstructionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View instructionView = LayoutInflater.from(parent.getContext()).inflate(R.layout.instructions_item, parent, false);
        return new InstructionsViewHolder(instructionView);
    }

    @Override
    public void onBindViewHolder(@NonNull InstructionsAdapter.InstructionsViewHolder holder, int position) {
        ImageView imageView = holder.itemView.findViewById(R.id.instruction_image);
        Button updateInventoryBtn = holder.itemView.findViewById(R.id.updateInventoryBtn);
        if (position == POSITION_ONE) {
            loadPositionZeroElement(holder, imageView);
            String imagePath = getImagePath();
            loadImageInImageView(imageView, imagePath);
        } else {
            setUpViewsForRecipes(holder, position, imageView);
        }

        setViewVisibilityAccordingToPosition(holder, position, updateInventoryBtn);
        setArrowVisibilityAccordingToPosition(holder, position);

        updateInventoryBtn.setOnClickListener(this::updateInventoryClicked);

    }

    private void setViewVisibilityAccordingToPosition(@NonNull InstructionsViewHolder holder, int position, Button updateInventoryBtn) {
        if (position < instructions.size() - 1) {
            holder.itemView.findViewById(R.id.rightAvailable).setVisibility(View.VISIBLE);
            updateInventoryBtn.setVisibility(View.GONE);
        } else {
            holder.itemView.findViewById(R.id.rightAvailable).setVisibility(View.GONE);
            String instructionActivityCaller = SharedValues.getInstance().getInstructionsActivityCaller();
            if (instructionActivityCaller != null && instructionActivityCaller.equals("MealPlannerFragment")){
                updateInventoryBtn.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateInventoryClicked(View view) {
        Recipe currentRecipe = SharedValues.getInstance().getCurrentRecipe();

        List<Map.Entry<String, Float>> currentRecipeIngredients = new ArrayList<>();
        loadCurrentIngredients(currentRecipe, currentRecipeIngredients);

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        LayoutInflater inflater = LayoutInflater.from(view.getContext());
        View dialogView = inflater.inflate(R.layout.ingredients_dialog, null);
        builder.setView(dialogView);

        ListView listView = dialogView.findViewById(R.id.ingredientsListView);
        GroceryListAdapter adapter = new GroceryListAdapter(view.getContext(), currentRecipeIngredients);
        listView.setAdapter(adapter);

        Button yesButton = dialogView.findViewById(R.id.yesButton);
        Button noButton = dialogView.findViewById(R.id.noButton);

        AlertDialog dialog = builder.create();
        yesButton.setOnClickListener(v -> {
            InventoryLoader inventoryLoader = new InventoryLoader(FirebaseFirestore.getInstance());
            String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            updateInventoryOnFirebase(currentRecipeIngredients, v, inventoryLoader, currentUserEmail);
            dialog.dismiss();
        });

        noButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateInventoryOnFirebase(List<Map.Entry<String, Float>> currentRecipeIngredients, View v, InventoryLoader inventoryLoader, String currentUserEmail) {
        inventoryLoader.updateInventoryQuantities(currentUserEmail, currentRecipeIngredients, false).addOnSuccessListener(success -> {
            successListenerForInventoryUpdate(v, success);
        }).addOnFailureListener(e -> {
            System.out.println("An error occurred: " + e.getMessage());
        });
    }

    private void successListenerForInventoryUpdate(View v, Boolean success) {
        if (success) {
            ToastUtils.showLongToast(v.getContext(),
                    "Inventory quantities updated successfully!");
            ((InstructionsActivity) context).finish();
        } else {
            ToastUtils.showLongToast(v.getContext(),
                    "Failed to update inventory quantities.");
        }
    }

    private static void loadCurrentIngredients(Recipe currentRecipe, List<Map.Entry<String, Float>> currentRecipeIngredients) {
        for (Map<String, Object> entry : currentRecipe.getIngredients()) {
            String ingredientName = (String) entry.get("name");
            Float quantity = Float.parseFloat(String.valueOf(entry.get("quantity")));
            currentRecipeIngredients.add(new AbstractMap
                    .SimpleEntry<>(ingredientName, quantity));
        }
    }

    private static void setArrowVisibilityAccordingToPosition(@NonNull InstructionsViewHolder holder, int position) {
        if (position > POSITION_ONE) {
            holder.itemView.findViewById(R.id.leftAvailable).setVisibility(View.VISIBLE);
        } else {
            holder.itemView.findViewById(R.id.leftAvailable).setVisibility(View.GONE);
        }
    }

    private void setUpViewsForRecipes(@NonNull InstructionsViewHolder holder, int position, ImageView imageView) {
        imageView.setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.instructionsRL).setBackgroundResource(R.drawable.card_background);
        TextView stepCount = holder.itemView.findViewById(R.id.stepCount);
        String text = "Step " + position;
        stepCount.setText(text);
        TextView instructionTV = holder.itemView.findViewById(R.id.instruction_text);
        TextView forNumberOfPeople = holder.itemView.findViewById(R.id.forNumberOfPeople);
        String servingSizeStatement = "This meal will be enough for " + SharedValues.getInstance().getServingSize() + " people.";
        forNumberOfPeople.setText(servingSizeStatement);
        instructionTV.setText(instructions.get(position));
    }

    private void loadImageInImageView(ImageView imageView, String imagePath) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference("uploads");
        StorageReference imageRef = storageRef.child(imagePath);
        imageRef.getBytes(DOWNLOAD_SIZE_LIMIT).addOnCompleteListener(task -> {
            byte[] image = task.getResult();
            imageView.setVisibility(View.VISIBLE);
            Glide.with(context).load(image).into(imageView);
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                imageView.setVisibility(View.GONE);
            }
        });
    }

    @NonNull
    private static String getImagePath() {
        String imagePath = SharedValues.getInstance().getCurrentRecipe().getImageUrl();
        if (imagePath.length() > FILE_NAME_LIMIT || imagePath.contentEquals("")) {
            imagePath = "placeHolder.jpg";
        }
        return imagePath;
    }

    private static void loadPositionZeroElement(@NonNull InstructionsViewHolder holder, ImageView imageView) {
        imageView.setVisibility(View.VISIBLE);
        holder.itemView.findViewById(R.id.instructionsRL).setBackgroundResource(R.drawable.card_background);
        TextView stepCount = holder.itemView.findViewById(R.id.stepCount);
        stepCount.setText(SharedValues.getInstance().getCurrentRecipe().getRecipeName());
        TextView instructionTV = holder.itemView.findViewById(R.id.instruction_text);
        TextView forNumberOfPeople = holder.itemView.findViewById(R.id.forNumberOfPeople);
        String servingSizeStatement = "This meal will be enough for " + SharedValues.getInstance().getServingSize() + " people.";
        forNumberOfPeople.setText(servingSizeStatement);
        instructionTV.setText(SharedValues.getInstance().getCurrentRecipe().getRecipeDescription());
    }

    @Override
    public int getItemCount() {
        return instructions.size();
    }

    public static class InstructionsViewHolder extends RecyclerView.ViewHolder {
        public InstructionsViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
