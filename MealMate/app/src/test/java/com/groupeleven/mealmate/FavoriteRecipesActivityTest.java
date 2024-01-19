package com.groupeleven.mealmate;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.groupeleven.mealmate.AccountManagement.FavoriteRecipesActivity;

public class FavoriteRecipesActivityTest {

    @Mock
    private DBHelper mockDbHelper;

    @Mock
    private SQLiteDatabase mockDb;

    @Mock
    private Cursor mockCursor;

    private FavoriteRecipesActivity activity;

//    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(mockDbHelper.getReadableDatabase()).thenReturn(mockDb);

        activity = new FavoriteRecipesActivity();

        when(mockDb.query(any(String.class), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockCursor);
        when(mockCursor.getCount()).thenReturn(1);
        when(mockCursor.moveToNext()).thenReturn(true);
        when(mockCursor.getString(1)).thenReturn("MockedRecipe");
    }

//    @Test
    public void testGetFavoriteRecipesFromDB() {
        ArrayList<String> recipeList = new ArrayList<>();
    }

//    @AfterClass
    public static void tearDownClass() {
    }
}
