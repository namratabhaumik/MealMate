package com.groupeleven.mealmate.MealPlanner.utils;

import com.groupeleven.mealmate.R;

import java.time.LocalDate;
import java.util.ArrayList;
import android.view.View;
import android.graphics.Color;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Adapter for displaying a calendar in a RecyclerView.
 */
public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder>
{
    private final ArrayList<LocalDate> days;
    private final OnItemListener onItemListener;

    /**
     * Constructor for CalendarAdapter.
     *
     * @param days           List of LocalDate objects representing days in the calendar.
     * @param onItemListener Listener for item click events in the calendar.
     */
    public CalendarAdapter(ArrayList<LocalDate> days, OnItemListener onItemListener)
    {
        this.days = days;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) parent.getHeight();
        return new CalendarViewHolder(view, onItemListener, days);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position)
    {
        final LocalDate date = days.get(position);
        if(date == null)
            holder.dayOfMonth.setText("");
        else
        {
            holder.dayOfMonth.setText(String.valueOf(date.getDayOfMonth()));
            if(date.equals(CalendarUtils.selectedDate))
                holder.parentView.setBackgroundColor(Color.GRAY);
        }
    }

    @Override
    public int getItemCount()
    {
        return days.size();
    }

    /**
     * Interface for handling item click events in the calendar.
     */
    public interface  OnItemListener
    {
        void onItemClick(int position, LocalDate date);
    }
}