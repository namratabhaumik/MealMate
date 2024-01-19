package com.groupeleven.mealmate.MealPlanner.utils;

import android.view.View;
import java.time.LocalDate;
import java.util.ArrayList;
import android.widget.TextView;
import com.groupeleven.mealmate.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ViewHolder for individual cells (dates) in the calendar RecyclerView.
 * Handles the representation of a single date cell and its interaction.
 */
public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    private final ArrayList<LocalDate> days;
    public final View parentView;
    public final TextView dayOfMonth;
    private final CalendarAdapter.OnItemListener onItemListener;

    /**
     * Constructor for the ViewHolder, initializes the views and sets click listener.
     *
     * @param itemView       The view item for each cell.
     * @param onItemListener Listener to handle click events on each cell.
     * @param days           ArrayList containing the dates to be displayed.
     */
    public CalendarViewHolder(@NonNull View itemView, CalendarAdapter.OnItemListener onItemListener, ArrayList<LocalDate> days)
    {
        super(itemView);
        parentView = itemView.findViewById(R.id.parentView);
        dayOfMonth = itemView.findViewById(R.id.cellDayText);
        this.onItemListener = onItemListener;
        itemView.setOnClickListener(this);
        this.days = days;
    }

    /**
     * Handles click events on the cell and triggers the associated onItemClick method.
     *
     * @param view The view that was clicked.
     */
    @Override
    public void onClick(View view)
    {
        onItemListener.onItemClick(getAdapterPosition(), days.get(getAdapterPosition()));
    }
}
