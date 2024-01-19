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

public class CategoriesDialog extends DialogFragment {
    RecyclerView categoriesRecyclerView;
    Button cancelBtn;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_categories, container, false);
        categoriesRecyclerView = v.findViewById(R.id.categoriesRecyclerView);
        cancelBtn = v.findViewById(R.id.cancelBtn);

        cancelBtn.setOnClickListener(view -> Objects.requireNonNull(getDialog()).dismiss());


        CategoryDialogAdapter adapter = new CategoryDialogAdapter(getParentFragmentManager());
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        categoriesRecyclerView.setAdapter(adapter);

        return v;
    }
}
