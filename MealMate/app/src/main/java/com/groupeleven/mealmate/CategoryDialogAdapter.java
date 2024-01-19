package com.groupeleven.mealmate;

        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.RelativeLayout;
        import android.widget.TextView;

        import androidx.annotation.NonNull;
        import androidx.fragment.app.FragmentManager;
        import androidx.recyclerview.widget.RecyclerView;

public class CategoryDialogAdapter extends RecyclerView.Adapter<CategoryDialogAdapter.CategoryViewHolder> {
    public CategoryDialogAdapter(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }
    FragmentManager fragmentManager;

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        RelativeLayout card;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            textView = itemView.findViewById(R.id.textView);
        }
    }
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.categories_dialog_card, parent, false);
        return new CategoryDialogAdapter.CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder,int position) {
        String categoryName = SharedValues.getInstance().getCategories().get(position);
        holder.textView.setText(categoryName);
        holder.card.setOnClickListener(view -> {
            IngredientsDialogFragment dialog = new IngredientsDialogFragment(categoryName);
            dialog.show(fragmentManager, "Categories Dialog");
        });
    }

    @Override
    public int getItemCount() {
        return SharedValues.getInstance().getCategories().size();
    }
}
