package com.groupeleven.mealmate.MealPlanner.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Utility class containing methods for handling date and time operations related to the calendar.
 */
public class CalendarUtils
{
    public static LocalDate selectedDate;

    /**
     * Formats the given date in the format "dd MMMM yyyy".
     *
     * @param date The date to format.
     * @return A formatted date string.
     */
    public static String formattedDate(LocalDate date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        return date.format(formatter);
    }

    /**
     * Formats the given time in the format "hh:mm:ss a".
     *
     * @param time The time to format.
     * @return A formatted time string.
     */
    public static String formattedTime(LocalTime time)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        return time.format(formatter);
    }

    /**
     * Retrieves the month and year from the given date and formats it as "MMMM yyyy".
     *
     * @param date The date to extract month and year from.
     * @return A formatted string representing the month and year.
     */
    public static String monthYearFromDate(LocalDate date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    /**
     * Generates an ArrayList of LocalDate objects representing days in the month of the given date.
     *
     * @param date The date from which to generate days.
     * @return An ArrayList of LocalDate objects representing days in the month.
     */
    public static ArrayList<LocalDate> daysInMonthArray(LocalDate date)
    {
        ArrayList<LocalDate> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = CalendarUtils.selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for(int i = 1; i <= 42; i++)
        {
            if(i <= dayOfWeek || i > daysInMonth + dayOfWeek)
                daysInMonthArray.add(null);
            else
                daysInMonthArray.add(LocalDate.of(selectedDate.getYear(),selectedDate.getMonth(),i - dayOfWeek));
        }
        return  daysInMonthArray;
    }

    /**
     * Generates an ArrayList of LocalDate objects representing days in a week starting from the given date.
     *
     * @param selectedDate The starting date of the week.
     * @return An ArrayList of LocalDate objects representing days in the week.
     */
    public static ArrayList<LocalDate> daysInWeekArray(LocalDate selectedDate)
    {
        ArrayList<LocalDate> days = new ArrayList<>();
        LocalDate current = sundayForDate(selectedDate);
        LocalDate endDate = current.plusWeeks(1);

        while (current.isBefore(endDate))
        {
            days.add(current);
            current = current.plusDays(1);
        }
        return days;
    }

    /**
     * Finds the Sunday date for the given date.
     *
     * @param current The date for which to find the Sunday.
     * @return The LocalDate representing the Sunday of that week.
     */
    private static LocalDate sundayForDate(LocalDate current)
    {
        LocalDate oneWeekAgo = current.minusWeeks(1);

        while (current.isAfter(oneWeekAgo))
        {
            if(current.getDayOfWeek() == DayOfWeek.SUNDAY)
                return current;

            current = current.minusDays(1);
        }

        return null;
    }
}