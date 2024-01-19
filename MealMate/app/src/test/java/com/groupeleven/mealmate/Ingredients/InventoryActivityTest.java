package com.groupeleven.mealmate.Ingredients;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.groupeleven.mealmate.CategoryAdapter;
import com.groupeleven.mealmate.DBHelper;
import com.groupeleven.mealmate.Ingredients.IngredientsData;
import com.groupeleven.mealmate.Ingredients.InventoryActivity;
import com.groupeleven.mealmate.Ingredients.InventoryAdapter;
import com.groupeleven.mealmate.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

//@RunWith(RobolectricTestRunner.class)
public class InventoryActivityTest {

    private InventoryActivity inventoryActivity;
    private FirebaseFirestore mockedFirestore;
    private CollectionReference mockedCollectionRef;
    private RecyclerView mockedRecyclerView;
    private Button mockedSelectedIngredientButton;
    private Button mockedStartRecommendButton;
    private ImageView mockedImgView;
    private EditText mockedEditText;
    private DBHelper mockedDBHelper;
    private SQLiteDatabase mockedSQLiteDatabase;
    private Cursor mockedCursor;
    @Mock
    private Log mockLog;

//    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        inventoryActivity = new InventoryActivity();

        /// Mock dependencies
        mockedFirestore = mock(FirebaseFirestore.class);
        mockedCollectionRef = mock(CollectionReference.class);
        mockedRecyclerView = mock(RecyclerView.class);
        mockedSelectedIngredientButton = mock(Button.class);
        mockedStartRecommendButton = mock(Button.class);
        mockedImgView = mock(ImageView.class);
        mockedEditText = mock(EditText.class);
        mockedDBHelper = mock(DBHelper.class);
        mockedSQLiteDatabase = mock(SQLiteDatabase.class);
        mockedCursor = mock(Cursor.class);

        // Use reflection to set the private fields
        setPrivateField("db", mockedFirestore);
        setPrivateField("collectionRef", mockedCollectionRef);
        setPrivateField("recyclerView", mockedRecyclerView);
        setPrivateField("selectedIngredient", mockedSelectedIngredientButton);
        setPrivateField("startRecommendButton", mockedStartRecommendButton);
        setPrivateField("imgView", mockedImgView);
        setPrivateField("editText", mockedEditText);
        setPrivateField("dbHelper", mockedDBHelper);
        setPrivateField("sqlDBRead", mockedSQLiteDatabase);
    }

    // Helper method to set private fields using reflection
    private void setPrivateField(String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = InventoryActivity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(inventoryActivity, value);
    }

//    @After
    public void tearDown() {
        inventoryActivity.finish();
    }

//    @Test
    public void testPreconditions() {
        assertNotNull("inventoryActivity is null", inventoryActivity);
    }

//    @Test
    public void testPopulateCategories() throws NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // Mock the behavior of the Firestore task
        Task<QuerySnapshot> mockedTask = mock(Task.class);
        when(mockedCollectionRef.get()).thenReturn(mockedTask);

        // Mock the successful completion of the task
        when(mockedTask.isSuccessful()).thenReturn(true);

        // Mock the result of the query
        List<QueryDocumentSnapshot> documentSnapshots = new ArrayList<>();
        QueryDocumentSnapshot documentSnapshot = mock(QueryDocumentSnapshot.class);
        when(documentSnapshot.getId()).thenReturn("categoryId");
        Map<String, Object> data = mock(Map.class);
        when(data.get("Name")).thenReturn("CategoryName");
        when(data.get("Url")).thenReturn("CategoryUrl");
        when(documentSnapshot.getData()).thenReturn(data);
        documentSnapshots.add(documentSnapshot);

        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
        when(mockedTask.getResult()).thenReturn(querySnapshot);

        // Calling the private method using reflection
        Method populateCategoriesMethod = InventoryActivity.class.getDeclaredMethod("populateCategories");
        populateCategoriesMethod.setAccessible(true);
        populateCategoriesMethod.invoke(inventoryActivity);

        // Accessing the private field using reflection
        Field categoryAdapterField = InventoryActivity.class.getDeclaredField("categoryAdapter");
        categoryAdapterField.setAccessible(true);
        CategoryAdapter categoryAdapter = (CategoryAdapter) categoryAdapterField.get(inventoryActivity);

        // Verify that the categoryAdapter has been set with the expected data
        assertNotNull(categoryAdapter);
        assertEquals(1, categoryAdapter.getItemCount());

        // Access the private field using reflection
        Field recyclerViewField = InventoryActivity.class.getDeclaredField("recyclerView");
        recyclerViewField.setAccessible(true);
        RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(inventoryActivity);

        // Verifying that the RecyclerView has been set with the categoryAdapter
        assertNotNull(recyclerView.getAdapter());
        assertTrue(recyclerView.getAdapter() instanceof CategoryAdapter);
    }

//    @Test
    public void testPopulateIngredientsOnCategoriesWithData() {
        String category = "TestCategory";
        List<IngredientsData> testData = new ArrayList<>();
        IngredientsData ingredientData = new IngredientsData();
        ingredientData.setIngredientImg("url1");
        ingredientData.setIngredientName("Ingredient1");
        ingredientData.setIngredientQty(1);
        ingredientData.setNoOfCook(0);
        ingredientData.setUnit("unit1");
        ingredientData.setIngredientCategory(category);
        testData.add(ingredientData);

        Task<QuerySnapshot> mockedTask = mock(Task.class);
        when(mockedCollectionRef.whereEqualTo("category", category)).thenReturn(mockedCollectionRef);
        when(mockedCollectionRef.get()).thenReturn(mockedTask);

        // Mocking the successful completion of the task
        when(mockedTask.isSuccessful()).thenReturn(true);

        Map<String, Object> documentData = new HashMap<>();
        documentData.put("imageUrl", "url1");
        documentData.put("name", "Ingredient1");
        documentData.put("quantity", 0);
        documentData.put("unit", "unit1");
        documentData.put("category", "TestCategory");

        // Mocking the result of the query
        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
        DocumentSnapshot mockedDocumentSnapshot = mock(DocumentSnapshot.class);
        when(mockedDocumentSnapshot.getData()).thenReturn(documentData);
        when(querySnapshot.getDocuments()).thenReturn(new ArrayList<>(Collections.singletonList(mockedDocumentSnapshot)));
        when(mockedTask.getResult()).thenReturn(querySnapshot);

        // Calling the method to test
        inventoryActivity.populateIngredientsOnCategories(category);

        // Verifying that the recyclerView has been set with the expected adapter
        verify(mockedRecyclerView).setAdapter(any(InventoryAdapter.class));
    }

//    @Test
    public void testPopulateIngredientsOnCategoriesWithoutData() {
        // Setting up test data
        String category = "TestCategory";
        List<IngredientsData> testData = new ArrayList<>();

        // Mocking the behavior of the Firestore task
        Task<QuerySnapshot> mockedTask = mock(Task.class);
        when(mockedCollectionRef.whereEqualTo("category", category)).thenReturn(mockedCollectionRef);
        when(mockedCollectionRef.get()).thenReturn(mockedTask);

        // Mocking the successful completion of the task with no results
        when(mockedTask.isSuccessful()).thenReturn(true);
        when(mockedTask.getResult()).thenReturn(mock(QuerySnapshot.class));

        // Calling the method to test
        inventoryActivity.populateIngredientsOnCategories(category);

        // Verifying that the recyclerView has been set with the expected adapter
        verify(mockedRecyclerView).setAdapter(any(InventoryAdapter.class));
    }

//    @Test
    public void testCheckAndGetCategoryWithData() {
        // Setting up test data
        String category = "TestCategory";
        List<IngredientsData> testData = new ArrayList<>();
        IngredientsData ingredientData = new IngredientsData();
        ingredientData.setIngredientImg("url1");
        ingredientData.setIngredientName("Ingredient1");
        ingredientData.setIngredientQty(1);
        ingredientData.setNoOfCook(0);
        ingredientData.setUnit("unit1");
        ingredientData.setIngredientCategory(category);
        testData.add(ingredientData);

        // Mocking the behavior of the SQLiteDatabase query method
        when(mockedSQLiteDatabase.query(
                eq("ingredients"),
                isNull(),
                eq("category = ?"),
                eq(new String[]{category}),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(mockedCursor);

        // Mocking the successful retrieval of data from the cursor
        when(mockedCursor.getCount()).thenReturn(1);
        when(mockedCursor.moveToNext()).thenReturn(true).thenReturn(false);
        when(mockedCursor.getString(3)).thenReturn("url1");
        when(mockedCursor.getString(1)).thenReturn("Ingredient1");
        when(mockedCursor.getInt(2)).thenReturn(0);
        when(mockedCursor.getString(5)).thenReturn("unit1");
        when(mockedCursor.getString(4)).thenReturn(category);

        // Calling the method to test
        List<IngredientsData> result = inventoryActivity.checkAndGetCategory(category);

        // Verifying the expected behavior
        assertEquals(testData, result);
    }

//    @Test
    public void testCheckAndGetCategoryWithoutData() {
        // Setting up test data
        String category = "TestCategory";

        // Mocking the behavior of the SQLiteDatabase query method
        when(mockedSQLiteDatabase.query(
                eq("ingredients"),
                isNull(),
                eq("category = ?"),
                eq(new String[]{category}),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(mockedCursor);

        // Mocking the absence of data in the cursor
        when(mockedCursor.getCount()).thenReturn(0);

        // Calling the method to test
        List<IngredientsData> result = inventoryActivity.checkAndGetCategory(category);

        // Verifying the expected behavior
        assertEquals(new ArrayList<>(), result);
    }

//    @Test
    public void testOnAddButtonClick() {
        TextView quantityTextView = inventoryActivity.findViewById(R.id.ingt_qty_num);
        assertEquals("2", quantityTextView.getText().toString());
    }

//    @Test
    public void testOnSubtractButtonClick() {
        TextView quantityTextView = inventoryActivity.findViewById(R.id.ingt_qty_num);
        assertEquals("0", quantityTextView.getText().toString()); // Adjust the expected value accordingly
    }

//    @Test
    public void testOnEditQuantity() throws NoSuchFieldException, IllegalAccessException {
        Field field = InventoryActivity.class.getDeclaredField("ingredientsDataList");
        field.setAccessible(true);
        List<IngredientsData> ingredientsDataList = (List<IngredientsData>) field.get(inventoryActivity);

        int pos = 0;
        IngredientsData ingredient = ingredientsDataList.get(pos);
        assertEquals(5, ingredient.getIngredientQty());
    }

//    @Test
    public void testUpdateEditedQuantity() throws NoSuchFieldException, IllegalAccessException {
        Field field = InventoryActivity.class.getDeclaredField("ingredientsDataList");
        field.setAccessible(true);
        List<IngredientsData> ingredientsDataList = (List<IngredientsData>) field.get(inventoryActivity);
        int pos = 0;
        IngredientsData ingredient = ingredientsDataList.get(pos);
        assertEquals(5, ingredient.getIngredientQty());
    }

//    @Test
    public void testUpdateQtyToSql() {
        // Setting up the test environment
        String ingtName = "testIngredient";
        String quantity = "10";

        // Mocking ContentValues
        ContentValues mockContentValues = mock(ContentValues.class);

        // Performing the method call
        inventoryActivity.updateQtyToSql(ingtName, quantity);

        // Verifying that the ContentValues were set correctly
        verify(mockContentValues).put("quantity", quantity);

        // Verifying that the SQLiteDatabase update method was called with the correct parameters
        verify(mockedSQLiteDatabase).update(eq("ingredients"), eq(mockContentValues), eq("name = ?"), eq(new String[]{ingtName}));
    }

//    @Test
    public void testUpdateIngredientQty() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        InventoryActivity inventoryActivity = new InventoryActivity();
        Method updateMethod = InventoryActivity.class.getDeclaredMethod("updateIngredientQty", int.class, IngredientsData.class);
        updateMethod.setAccessible(true);

        int previousQty = 5;
        IngredientsData mockData = mock(IngredientsData.class);
        when(mockData.getIngredientName()).thenReturn("testIngredient");
        when(mockData.getIngredientQty()).thenReturn(3);

        updateMethod.invoke(inventoryActivity, previousQty, mockData);

        // Verifying that the ContentValues were set correctly
        ContentValues expectedValues = new ContentValues();
        expectedValues.put("quantity", previousQty + 3);
        verify(mockedSQLiteDatabase).update(
                eq("user_ingredients"),
                eq(expectedValues),
                eq("name = ?"),
                eq(new String[]{"testIngredient"})
        );

        // Verifying that the Log.d method was called
        verify(mockLog).d(eq("Update Error"), eq(String.valueOf(anyInt())));
    }

//    @Test
    public void testGetExistIngredientQty() {
        // Setting up the test environment
        String checkIngredient = "testIngredient";

        when(mockedDBHelper.getReadableDatabase()).thenReturn(mockedSQLiteDatabase);
        when(mockedSQLiteDatabase.query(
                eq("user_ingredients"),
                any(String[].class),
                eq("name = ?"),
                eq(new String[]{checkIngredient}),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(mockedCursor);
        when(mockedCursor.moveToFirst()).thenReturn(true);
        when(mockedCursor.getColumnIndex("quantity")).thenReturn(1);  // assuming "quantity" is at index 1
        when(mockedCursor.getInt(1)).thenReturn(42);  // assuming the quantity is 42

        // Performing the method call
        int result = inventoryActivity.getExistIngredientQty(checkIngredient);

        // Verifying that the database-related methods were called correctly
        verify(mockedDBHelper).getReadableDatabase();
        verify(mockedSQLiteDatabase).query(
                eq("user_ingredients"),
                any(String[].class),
                eq("name = ?"),
                eq(new String[]{checkIngredient}),
                isNull(),
                isNull(),
                isNull()
        );
        // Verifying the result
        assertEquals(42, result);
    }
}

