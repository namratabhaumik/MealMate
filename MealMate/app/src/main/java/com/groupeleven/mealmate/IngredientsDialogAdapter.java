package com.groupeleven.mealmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngredientsDialogAdapter extends RecyclerView.Adapter<IngredientsDialogAdapter.IngredientsViewHolder> {
    private static final int INCREMENT_VALUE_FOR_GMS = 100;
    private static final int INCREMENT_VALUE_FOR_ML = 10;
    private static final int INCREMENT_VALUE_FOR_UNIT = 1;

    public IngredientsDialogAdapter(String category) {
        this.category = category;
    }
    private String category;

    public static class IngredientsViewHolder extends RecyclerView.ViewHolder {
        TextView ingredientName;
        TextView ingredientCount;
        TextView unit;
        ImageButton decrementBtn;
        ImageButton incrementBtn;

        public IngredientsViewHolder(View itemView) {
            super(itemView);
            ingredientName = itemView.findViewById(R.id.ingredientName);
            ingredientCount = itemView.findViewById(R.id.ingredientCount);
            unit = itemView.findViewById(R.id.unit);
            decrementBtn = itemView.findViewById(R.id.decrementBtn);
            incrementBtn = itemView.findViewById(R.id.incrementBtn);
        }
    }
    @NonNull
    @Override
    public IngredientsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ingredients_dialog_card, parent, false);
        return new IngredientsDialogAdapter.IngredientsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientsViewHolder holder,int position) {
        ArrayList<String> ingredients = SharedValues.getInstance().getCategorywiseIngredients().get(category);
        String ingredient = ingredients.get(position);
        String unit = SharedValues.getInstance().getIngredientsUnits().get(ingredient);
        holder.ingredientName.setText(ingredient);
        holder.unit.setText(unit);
        int incrementValue;
        incrementValue = getIncrementValue(unit);

        // DECREMENT
        holder.decrementBtn.setOnClickListener(view -> {
            decrementClicked(holder, ingredient, incrementValue);
        });

        // INCREMENT
        holder.incrementBtn.setOnClickListener(view -> {
            incrementClicked(holder, ingredient, incrementValue);
        });
    }

    private static void incrementClicked(@NonNull IngredientsViewHolder holder, String ingredient, int incrementValue) {
        String countStr = holder.ingredientCount.getText().toString();
        int count = Integer.parseInt(countStr);
        count += incrementValue;
        holder.ingredientCount.setText(String.valueOf(count));

        List<Map<String, Object>> ingredientsArrayList = UploadRecipeSingleton.getInstance().getIngredients();

        if (ingredientsArrayList.isEmpty()) {
            HashMap<String, Object> newMap = new HashMap<>();
            newMap.put(FireBaseEntities.INGR_NAME, ingredient);
            newMap.put(FireBaseEntities.INGR_QUAN, count);
            ingredientsArrayList.add(newMap);
        } else {
            for (int i = 0; i < ingredientsArrayList.size(); i++) {
                if (updateIngredientsA(ingredient, count, ingredientsArrayList, i)) break;
            }
        }
        UploadRecipeSingleton.getInstance().setIngredients(ingredientsArrayList);
    }

    private static boolean updateIngredientsA(String ingredient, int count, List<Map<String, Object>> ingredientsArrayList, int i) {
        Map<String, Object> map = ingredientsArrayList.get(i);
        if (map.get(FireBaseEntities.INGR_NAME).equals(ingredient)) {
            map.replace(FireBaseEntities.INGR_QUAN, count);
            return true;
        }
        if (i == ingredientsArrayList.size() - 1) {
            HashMap<String, Object> newMap = new HashMap<>();
            newMap.put(FireBaseEntities.INGR_NAME, ingredient);
            newMap.put(FireBaseEntities.INGR_QUAN, count);
            ingredientsArrayList.add(newMap);
        }
        return false;
    }

    private static void decrementClicked(@NonNull IngredientsViewHolder holder, String ingredient, int incrementValue) {
        String countStr = holder.ingredientCount.getText().toString();
        int count = Integer.parseInt(countStr);
        if (count != 0) {
            count -= incrementValue;
            holder.ingredientCount.setText(String.valueOf(count));
        }
        List<Map<String, Object>> ingredientsArrayList = UploadRecipeSingleton.getInstance().getIngredients();

        if (ingredientsArrayList.isEmpty()) {
            HashMap<String, Object> newMap = new HashMap<>();
            newMap.put(FireBaseEntities.INGR_NAME, ingredient);
            newMap.put(FireBaseEntities.INGR_QUAN, count);
            ingredientsArrayList.add(newMap);
        } else {
            updateIngredientsArraylist(ingredient, count, ingredientsArrayList);
        }
        UploadRecipeSingleton.getInstance().setIngredients(ingredientsArrayList);
    }

    private static int getIncrementValue(String unit) {
        int incrementValue;
        if (unit.toLowerCase().contentEquals("ml")) {
            incrementValue = INCREMENT_VALUE_FOR_ML;
        } else if (unit.toLowerCase().contentEquals("gms")) {
            incrementValue = INCREMENT_VALUE_FOR_GMS;
        } else {
            incrementValue = INCREMENT_VALUE_FOR_UNIT;
        }
        return incrementValue;
    }

    private static void updateIngredientsArraylist(String ingredient, int count, List<Map<String, Object>> ingredientsArrayList) {
        for (int i = 0; i < ingredientsArrayList.size(); i++) {
            Map<String, Object> map = ingredientsArrayList.get(i);
            if (map.get(FireBaseEntities.INGR_NAME).equals(ingredient)) {
                map.replace(FireBaseEntities.INGR_QUAN, count);
                break;
            }
            if (i == ingredientsArrayList.size() - 1) {
                HashMap<String, Object> newMap = new HashMap<>();
                newMap.put(FireBaseEntities.INGR_NAME, ingredient);
                newMap.put(FireBaseEntities.INGR_QUAN, count);
                ingredientsArrayList.add(newMap);
            }
        }
    }

    @Override
    public int getItemCount() {
        ArrayList<String> a = SharedValues.getInstance().getCategorywiseIngredients().get(category);
        return a.size();
    }
}
