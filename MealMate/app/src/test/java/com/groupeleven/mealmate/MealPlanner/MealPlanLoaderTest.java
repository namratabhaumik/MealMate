package com.groupeleven.mealmate.MealPlanner;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.groupeleven.mealmate.MealPlanner.models.Meal;
import com.groupeleven.mealmate.MealPlanner.models.MealPlan;
import com.groupeleven.mealmate.MealPlanner.utils.MealPlanLoader;
import com.groupeleven.mealmate.Recipe.models.Recipe;

@RunWith(RobolectricTestRunner.class)
public class MealPlanLoaderTest {
    private MealPlanLoader mealPlanLoader;

    @Mock
    private FirebaseFirestore mockDb;

    @Mock
    private Task<QuerySnapshot> mockQuerySnapshotTask;

    @Mock
    private CollectionReference mockCollectionReference;

    @Mock
    private QuerySnapshot mockQuerySnapshot;

    @Mock
    private DocumentSnapshot mockDocumentSnapshot;

    @Mock
    private DocumentReference mockDocumentReference;

    @Mock
    private Query mockQuery;

    @Mock
    private Task<DocumentReference> mockDocumentReferenceTask;
    @Mock
    private Task<MealPlan> mockMealPlanTask;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mealPlanLoader = new MealPlanLoader(mockDb);

        // For Mocking Firestore and its related classes
        when(mockDb.collection(anyString())).thenReturn(mockCollectionReference);
        when(mockCollectionReference.whereEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockCollectionReference
                .whereEqualTo(anyString(), any())
                .whereEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockCollectionReference.add(any())).thenReturn(mockDocumentReferenceTask);
        when(mockQuery.get()).thenReturn(mockQuerySnapshotTask);

        // Adding at least one "DocumentSnapshot" to get the first index result, i-e: "get(0)"
        List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
        documentSnapshots.add(mockDocumentSnapshot);
        when(mockQuerySnapshot.getDocuments()).thenReturn(documentSnapshots);
    }

    private Recipe getMockedRecipe(){
        Map<String, Object> ingredient1 = new HashMap<>();
        ingredient1.put("name", "Ingredient 1");
        ingredient1.put("quantity", "100g");

        Map<String, Object> ingredient2 = new HashMap<>();
        ingredient2.put("name", "Ingredient 2");
        ingredient2.put("quantity", "200g");

        List<Map<String, Object>> ingredients = new ArrayList<>();
        ingredients.add(ingredient1);
        ingredients.add(ingredient2);

        ArrayList<String> instructions = new ArrayList<>();
        instructions.add("Step 1: Do something");
        instructions.add("Step 2: Do something else");

        Recipe recipe = new Recipe("Sample Recipe",
                "4", "This is a sample recipe");
        recipe.setImageUrl("https://example.com/image.jpg");
        recipe.setIngredients(ingredients);
        recipe.setInstructions(instructions);
        recipe.setUserEmail("sample@example.com");
        recipe.setVegetarian(true);
        recipe.setTime("30 minutes");
        return recipe;
    }

    @Test
    public void deleteMealPlan_TaskUnsuccessful_ReturnsFalse(){
        when(mockQuerySnapshotTask.continueWithTask(any())).thenAnswer(invocation -> {
            Continuation<QuerySnapshot, Task<Boolean>> continuation =
                    invocation.getArgument(0);
            return continuation.then(mockQuerySnapshotTask);
        });
        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(false);

        Task<Boolean> taskResult = mealPlanLoader.deleteMealPlan("mealPlanId");

        assertFalse(taskResult.getResult());
    }

    @Test
    public void deleteMealPlan_TaskSuccessful_ReturnsTrue(){
        when(mockQuerySnapshotTask.continueWithTask(any())).thenAnswer(invocation -> {
            Continuation<QuerySnapshot, Task<Boolean>> continuation =
                    invocation.getArgument(0);
            return continuation.then(mockQuerySnapshotTask);
        });
        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(true);
        when(mockQuerySnapshotTask.getResult()).thenReturn(mockQuerySnapshot);
        when(mockQuerySnapshot.isEmpty()).thenReturn(false);
        when(mockDocumentSnapshot.getReference()).thenReturn(mockDocumentReference);

        Task<Boolean> taskResult = mealPlanLoader.deleteMealPlan("mealPlanId");

        verify(mockDocumentReference, times(1)).delete();
        assertTrue(taskResult.getResult());
    }

    @Test
    public void loadMealPlanWithMatchingUserAndDate_TaskUnsuccessful_ReturnsNull(){
        when(mockQuerySnapshotTask.continueWith(any())).thenAnswer(invocation -> {
            Continuation<QuerySnapshot, Task<MealPlan>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockQuerySnapshotTask);
            return Tasks.forResult(null);
        });
        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(false);

        Task<MealPlan> taskResult = mealPlanLoader.loadMealPlanWithMatchingUserAndDate(
                "test@example.com", "11-12-2023");

        assertNull(taskResult.getResult());
    }

    @Test
    public void loadMealPlanWithMatchingUserAndDate_TaskSuccessful_ReturnsMealPlan(){
        MealPlan mockedMealPlan = new MealPlan("11-12-2023", new ArrayList<>(),
                "test@example.com");
        when(mockQuerySnapshotTask.continueWith(any())).thenAnswer(invocation -> {
            Continuation<QuerySnapshot, Task<MealPlan>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockQuerySnapshotTask);
            return Tasks.forResult(mockedMealPlan);
        });
        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(true);
        when(mockQuerySnapshotTask.getResult()).thenReturn(mockQuerySnapshot);
        when(mockDocumentSnapshot.toObject(MealPlan.class)).thenReturn(mockedMealPlan);

        Task<MealPlan> taskResult = mealPlanLoader.loadMealPlanWithMatchingUserAndDate(
                "test@example.com", "11-12-2023");

        assertNotNull(taskResult.getResult());
        assertEquals(taskResult.getResult().getMealPlanId(), mockedMealPlan.getMealPlanId());
    }

    @Test
    public void loadRecipe_TaskUnsuccessful_ReturnsNull(){
        when(mockQuerySnapshotTask.continueWith(any())).thenAnswer(invocation -> {
            Continuation<QuerySnapshot, Task<Recipe>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockQuerySnapshotTask);
            return Tasks.forResult(null);
        });
        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(false);

        Task<Recipe> taskResult = mealPlanLoader.loadRecipe("recipeId");

        assertNull(taskResult.getResult());
    }

    @Test
    public void loadRecipe_TaskSuccessful_ReturnsRecipe(){
        Recipe mockedRecipe = getMockedRecipe();
        when(mockQuerySnapshotTask.continueWith(any())).thenAnswer(invocation -> {
            Continuation<QuerySnapshot, Task<Recipe>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockQuerySnapshotTask);
            return Tasks.forResult(mockedRecipe);
        });
        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(true);
        when(mockQuerySnapshotTask.getResult()).thenReturn(mockQuerySnapshot);
        when(mockDocumentSnapshot.toObject(Recipe.class)).thenReturn(mockedRecipe);

        Task<Recipe> taskResult = mealPlanLoader.loadRecipe("recipeId");

        assertNotNull(taskResult.getResult());
        assertEquals(mockedRecipe.getRecipeId(), taskResult.getResult().getRecipeId());
    }

    @Test
    public void uploadMealPlan_TaskUnsuccessful_ReturnsFalse() {
        MealPlan mockedMealPlan = new MealPlan("11-12-2023", new ArrayList<>(),
                "test@example.com");

        MealPlanLoader spyMealPlanLoader = spy(mealPlanLoader);

        when(spyMealPlanLoader.loadMealPlanWithMatchingUserAndDate(anyString(), anyString()))
                .thenReturn(mockMealPlanTask);

        when(mockMealPlanTask.continueWithTask(any())).thenAnswer(invocation -> {
            Continuation<MealPlan, Task<Boolean>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockMealPlanTask);
            return Tasks.forResult(false);
        });
        when(mockMealPlanTask.isSuccessful()).thenReturn(false);

        Task<Boolean> taskResult = spyMealPlanLoader.uploadMealPlan(mockedMealPlan);

        taskResult.addOnCompleteListener(task -> assertFalse(taskResult.getResult()));
    }

    @Test
    public void uploadMealPlan_NoExistingMealPlan_AddsNewMealPlanAndReturnTrue() {
        MealPlan mockedMealPlan = new MealPlan("11-12-2023", new ArrayList<>(),
                "test@example.com");

        MealPlanLoader spyMealPlanLoader = spy(mealPlanLoader);

        when(spyMealPlanLoader.loadMealPlanWithMatchingUserAndDate(anyString(), anyString()))
                .thenReturn(mockMealPlanTask);

        when(mockMealPlanTask.continueWithTask(any())).thenAnswer(invocation -> {
            Continuation<MealPlan, Task<Boolean>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockMealPlanTask);
            return Tasks.forResult(true);
        });
        when(mockMealPlanTask.isSuccessful()).thenReturn(true);
        when(mockMealPlanTask.getResult()).thenReturn(null);

        when(mockDocumentReferenceTask.continueWith(any())).thenAnswer(invocation -> {
            Continuation<DocumentReference, Task<Boolean>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockDocumentReferenceTask);
            return Tasks.forResult(true);
        });
        when(mockDocumentReferenceTask.isSuccessful()).thenReturn(true);

        Task<Boolean> taskResult = spyMealPlanLoader.uploadMealPlan(mockedMealPlan);

        taskResult.addOnCompleteListener(task -> assertTrue(taskResult.getResult()));
    }

    @Test
    public void uploadMealPlan_ExistingMealPlan_UpdatesMealPlanAndReturnTrue() {
        Meal mockedMeal = new Meal(Meal.MealType.LUNCH, new ArrayList<>());
        List<Meal> mockedMealList = new ArrayList<>();
        mockedMealList.add(mockedMeal);
        MealPlan mockedMealPlan = new MealPlan("11-12-2023", mockedMealList,
                "test@example.com");

        MealPlanLoader spyMealPlanLoader = spy(mealPlanLoader);

        when(spyMealPlanLoader.loadMealPlanWithMatchingUserAndDate(anyString(), anyString()))
                .thenReturn(mockMealPlanTask);

        when(mockMealPlanTask.continueWithTask(any())).thenAnswer(invocation -> {
            Continuation<MealPlan, Task<Boolean>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockMealPlanTask);
            return Tasks.forResult(true);
        });
        when(mockMealPlanTask.isSuccessful()).thenReturn(true);
        when(mockMealPlanTask.getResult()).thenReturn(mockedMealPlan);

        when(mockQuerySnapshotTask.continueWithTask(any())).thenAnswer(invocation -> {
            Continuation<QuerySnapshot, Task<Boolean>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockQuerySnapshotTask);
            return Tasks.forResult(true);
        });
        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(true);
        when(mockQuerySnapshotTask.getResult()).thenReturn(mockQuerySnapshot);
        when(mockDocumentSnapshot.getReference()).thenReturn(mockDocumentReference);

        Task<Boolean> taskResult = spyMealPlanLoader.uploadMealPlan(mockedMealPlan);

        taskResult.addOnCompleteListener(task -> assertTrue(taskResult.getResult()));
    }

    @Test
    public void uploadMealPlan_ExistingMealPlanAndDifferentType_UpdatesMealPlanAndReturnTrue() {
        Meal mockedMeal = new Meal(Meal.MealType.LUNCH, new ArrayList<>());
        List<Meal> mockedMealList = new ArrayList<>();
        mockedMealList.add(mockedMeal);
        MealPlan mockedMealPlan = new MealPlan("11-12-2023", mockedMealList,
                "test@example.com");

        MealPlanLoader spyMealPlanLoader = spy(mealPlanLoader);

        when(spyMealPlanLoader.loadMealPlanWithMatchingUserAndDate(anyString(), anyString()))
                .thenReturn(mockMealPlanTask);

        when(mockMealPlanTask.continueWithTask(any())).thenAnswer(invocation -> {
            Continuation<MealPlan, Task<Boolean>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockMealPlanTask);
            return Tasks.forResult(true);
        });
        when(mockMealPlanTask.isSuccessful()).thenReturn(true);
        when(mockMealPlanTask.getResult()).thenReturn(mockedMealPlan);

        when(mockQuerySnapshotTask.continueWithTask(any())).thenAnswer(invocation -> {
            Continuation<QuerySnapshot, Task<Boolean>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockQuerySnapshotTask);
            return Tasks.forResult(true);
        });
        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(true);
        when(mockQuerySnapshotTask.getResult()).thenReturn(mockQuerySnapshot);
        when(mockDocumentSnapshot.getReference()).thenReturn(mockDocumentReference);

        Meal meal = new Meal(Meal.MealType.BREAKFAST, new ArrayList<>());
        List<Meal> mealList = new ArrayList<>();
        mealList.add(meal);
        MealPlan mealPlan = new MealPlan("11-12-2023", mealList,
                "test@example.com");

        Task<Boolean> taskResult = spyMealPlanLoader.uploadMealPlan(mealPlan);

        taskResult.addOnCompleteListener(task -> assertTrue(taskResult.getResult()));
    }

    @Test
    public void updateExistingMealPlan_TaskUnsuccessful_ReturnsFalse() {
        Meal mockedMeal = new Meal(Meal.MealType.LUNCH, new ArrayList<>());
        List<Meal> mockedMealList = new ArrayList<>();
        mockedMealList.add(mockedMeal);
        MealPlan mockedMealPlan = new MealPlan("11-12-2023", mockedMealList,
                "test@example.com");

        when(mockQuerySnapshotTask.continueWithTask(any())).thenAnswer(invocation -> {
            Continuation<QuerySnapshot, Task<Boolean>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockQuerySnapshotTask);
            return Tasks.forResult(false);
        });
        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(false);

        Task<Boolean> taskResult = mealPlanLoader.updateExistingMealPlan(mockedMealPlan);

        taskResult.addOnCompleteListener(task -> assertFalse(taskResult.getResult()));
    }

    @Test
    public void updateExistingMealPlan_TaskSuccessful_ReturnsTrue() {
        Meal mockedMeal = new Meal(Meal.MealType.LUNCH, new ArrayList<>());
        List<Meal> mockedMealList = new ArrayList<>();
        mockedMealList.add(mockedMeal);
        MealPlan mockedMealPlan = new MealPlan("11-12-2023", mockedMealList,
                "test@example.com");

        when(mockQuerySnapshotTask.continueWithTask(any())).thenAnswer(invocation -> {
            Continuation<QuerySnapshot, Task<Boolean>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockQuerySnapshotTask);
            return Tasks.forResult(true);
        });
        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(true);
        when(mockQuerySnapshotTask.getResult()).thenReturn(mockQuerySnapshot);
        when(mockDocumentSnapshot.getReference()).thenReturn(mockDocumentReference);

        Task<Boolean> taskResult = mealPlanLoader.updateExistingMealPlan(mockedMealPlan);

        taskResult.addOnCompleteListener(task -> assertTrue(taskResult.getResult()));
    }
}