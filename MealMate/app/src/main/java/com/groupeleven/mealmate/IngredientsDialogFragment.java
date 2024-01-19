package com.groupeleven.mealmate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Objects;

public class IngredientsDialogFragment extends DialogFragment {
    RecyclerView ingredientsRecyclerView;
    Button cancelBtn;
    String categorySelected;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_ingredients, container, false);
        ingredientsRecyclerView = v.findViewById(R.id.ingredientsRecyclerView);
        cancelBtn = v.findViewById(R.id.cancelBtn);

        cancelBtn.setOnClickListener(view -> Objects.requireNonNull(getDialog()).dismiss());

        IngredientsDialogAdapter adapter = new IngredientsDialogAdapter(categorySelected);
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ingredientsRecyclerView.setAdapter(adapter);

        return v;
    }

    public IngredientsDialogFragment(String categorySelected) {
        this.categorySelected = categorySelected;
    }
}
