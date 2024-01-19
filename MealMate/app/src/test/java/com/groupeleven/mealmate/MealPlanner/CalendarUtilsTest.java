package com.groupeleven.mealmate.MealPlanner;

import com.groupeleven.mealmate.MealPlanner.utils.CalendarUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class CalendarUtilsTest {

    @Test
    public void testFormattedDate() {
        LocalDate date = LocalDate.of(2023, 11, 25);
        String formatted = CalendarUtils.formattedDate(date);
        assertEquals("25 November 2023", formatted);
    }

    @Test
    public void testFormattedTime() {
        LocalTime time = LocalTime.of(13, 30);
        String formatted = CalendarUtils.formattedTime(time);
        assertEquals("01:30:00 PM", formatted);
    }

    @Test
    public void testMonthYearFromDate() {
        LocalDate date = LocalDate.of(2023, 11, 25);
        String monthYear = CalendarUtils.monthYearFromDate(date);
        assertEquals("November 2023", monthYear);
    }

    @Test
    public void testDaysInMonthArray() {
        LocalDate date = LocalDate.of(2023, 12, 1);
        CalendarUtils.selectedDate = date;
        ArrayList<LocalDate> daysInMonthArray = CalendarUtils.daysInMonthArray(date);
        assertEquals(42, daysInMonthArray.size());
        assertNull(daysInMonthArray.get(0));
    }

    @Test
    public void testDaysInWeekArray() {
        LocalDate selectedDate = LocalDate.of(2023, 12, 1);
        ArrayList<LocalDate> daysInWeekArray = CalendarUtils.daysInWeekArray(selectedDate);

        assertEquals(7, daysInWeekArray.size());
        assertEquals(DayOfWeek.SUNDAY, daysInWeekArray.get(0).getDayOfWeek());
        assertEquals(DayOfWeek.SATURDAY, daysInWeekArray.get(6).getDayOfWeek());
    }
}