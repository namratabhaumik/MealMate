package com.groupeleven.mealmate;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.load.MultiTransformation;
import jp.wasabeef.glide.transformations.BlurTransformation;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<CategoryData> category;
    Context activityContext;
    public CategoryAdapter(List<CategoryData> category) {
        this.category = category;
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        CardView cardView;
        ImageView categoryImageView;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.category_card_view);
            textView = cardView.findViewById(R.id.category_name);
            categoryImageView = cardView.findViewById(R.id.category_image);

        }
    }
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.categories, parent, false);

        return new CategoryAdapter.CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder,int position) {
        CategoryData ctgry = category.get(position);
        String categoryName = ctgry.getCategoryName();
        holder.textView.setText(categoryName+"");

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.transform(new MultiTransformation<>(new BlurTransformation(25, 3)));

        Glide.with(holder.cardView)
                .load(ctgry.getCategoryUrl())  // Replace with the actual image resource or URL
                .transform(new BlurTransformation(25, 1))  // Adjust the blur radius as needed
                .into(holder.categoryImageView);
    }

    @Override
    public int getItemCount() {
        return category.size();
    }
}
