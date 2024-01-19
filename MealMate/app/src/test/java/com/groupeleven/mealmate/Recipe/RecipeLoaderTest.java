package com.groupeleven.mealmate.Recipe;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.mockito.MockedStatic;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.groupeleven.mealmate.Recipe.models.Recipe;
import com.groupeleven.mealmate.Recipe.utils.RecipeLoader;

@RunWith(RobolectricTestRunner.class)
public class RecipeLoaderTest {
    private RecipeLoader recipeLoader;
    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private Task<List<Task<?>>> mockTaskForListOfTasks;
    @Mock
    private Task<QuerySnapshot> mockQuerySnapshotTask;
    @Mock
    private CollectionReference mockCollectionReference;
    @Mock
    private QuerySnapshot mockQuerySnapshot;
    @Mock
    private DocumentSnapshot mockDocumentSnapshot;
    @Mock
    private Query mockQuery;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        recipeLoader = new RecipeLoader(mockFirestore);

        // For Mocking Firestore and its related classes / methods
        when(mockFirestore.collection(anyString())).thenReturn(mockCollectionReference);
        when(mockCollectionReference.whereEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockQuerySnapshotTask);

        // Adding at least one "DocumentSnapshot" to get the first index result, i-e: "get(0)"
        List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
        documentSnapshots.add(mockDocumentSnapshot);
        when(mockQuerySnapshot.getDocuments()).thenReturn(documentSnapshots);

        when(mockQuerySnapshotTask.getResult()).thenReturn(mockQuerySnapshot);
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
    public void loadRecipes_TaskUnsuccessful_ReturnsNull(){
        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(false);
        // For Mocking "continueWith" passed callback
        doAnswer(invocation -> {
            Continuation<QuerySnapshot, Task<List<Recipe>>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockQuerySnapshotTask);
            return Tasks.forResult(null);
        }).when(mockQuerySnapshotTask).continueWith(any());

        Task<List<Recipe>> recipesListTask = recipeLoader.loadRecipes(
                        "tomato curry", false);

        assertNull(recipesListTask.getResult());
    }

    @Test
    public void loadRecipes_NoMatchingRecipe_ReturnsEmptyList(){
        List<Recipe> mockedRecipesList = new ArrayList<>();

        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(true);
        doAnswer(invocation -> {
            Continuation<QuerySnapshot, Task<List<Recipe>>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockQuerySnapshotTask);
            return Tasks.forResult(mockedRecipesList);
        }).when(mockQuerySnapshotTask).continueWith(any());

        when(mockDocumentSnapshot.toObject(Recipe.class)).thenReturn(null);

        Task<List<Recipe>> recipesListTask = recipeLoader.loadRecipes(
                "NonExistingRecipe", true);

        assertTrue(recipesListTask.getResult().isEmpty());
    }

    @Test
    public void loadRecipes_MatchingRecipe_ReturnsMatchingRecipeList(){
        List<Recipe> mockedRecipesList = new ArrayList<>();
        Recipe mockedRecipe = getMockedRecipe();
        mockedRecipesList.add(mockedRecipe);

        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(true);
        doAnswer(invocation -> {
            Continuation<QuerySnapshot, Task<List<Recipe>>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockQuerySnapshotTask);
            return Tasks.forResult(mockedRecipesList);
        }).when(mockQuerySnapshotTask).continueWith(any());

        when(mockDocumentSnapshot.toObject(Recipe.class)).thenReturn(mockedRecipe);

        Task<List<Recipe>> recipesListTask = recipeLoader.loadRecipes(
                "Sample Recipe", true);

        assertEquals(recipesListTask.getResult().size(), mockedRecipesList.size());
        assertEquals(recipesListTask.getResult().get(0).getRecipeName(),
                mockedRecipesList.get(0).getRecipeName());
    }

    @Test
    public void loadUserRecipes_TaskUnsuccessful_ReturnsNull(){
        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(false);
        doAnswer(invocation -> {
            Continuation<QuerySnapshot, Task<List<Recipe>>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockQuerySnapshotTask);
            return Tasks.forResult(null);
        }).when(mockQuerySnapshotTask).continueWith(any());

        Task<List<Recipe>> recipesListTask = recipeLoader.loadUserRecipes(
                "sample@example.com");

        assertNull(recipesListTask.getResult());
    }

    @Test
    public void loadUserRecipes_TaskSuccessful_ReturnsRecipeList(){
        List<Recipe> mockedRecipesList = new ArrayList<>();
        Recipe mockedRecipe = getMockedRecipe();
        mockedRecipesList.add(mockedRecipe);

        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(true);
        doAnswer(invocation -> {
            Continuation<QuerySnapshot, Task<List<Recipe>>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockQuerySnapshotTask);
            return Tasks.forResult(mockedRecipesList);
        }).when(mockQuerySnapshotTask).continueWith(any());

        when(mockDocumentSnapshot.toObject(Recipe.class)).thenReturn(mockedRecipe);

        Task<List<Recipe>> recipesListTask = recipeLoader.loadUserRecipes(
                "sample@example.com");

        assertEquals(recipesListTask.getResult().size(), mockedRecipesList.size());
        assertEquals(recipesListTask.getResult().get(0).getUserEmail(),
                mockedRecipesList.get(0).getUserEmail());
    }

    @Test
    public void fetchRecipesWithMatchingIds_TaskSuccessful_ReturnsRecipeList() {
        List<Recipe> mockedRecipesList = new ArrayList<>();
        Recipe mockedRecipe = getMockedRecipe();
        mockedRecipesList.add(mockedRecipe);

        List<String> recipeIds = new ArrayList<>();
        recipeIds.add(mockedRecipe.getRecipeId());

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        tasks.add(mockQuerySnapshotTask);

        MockedStatic<Tasks> mockTasks = mockStatic(Tasks.class);
        mockTasks.when(() -> Tasks.whenAllComplete(tasks)).thenReturn(mockTaskForListOfTasks);

        doAnswer(invocation -> {
            Continuation<List<Task<?>>, Task<List<Recipe>>> continuation =
                    invocation.getArgument(0);
            continuation.then(mockTaskForListOfTasks);
            // Closing static mock bound to Tasks.class
            mockTasks.close();
            return Tasks.forResult(mockedRecipesList);
        }).when(mockTaskForListOfTasks).continueWith(any());

        when(mockQuerySnapshot.toObjects(Recipe.class)).thenReturn(mockedRecipesList);

        Task<List<Recipe>> matchingRecipesListTask = recipeLoader
                .fetchRecipesWithMatchingIds(recipeIds);

        assertEquals(matchingRecipesListTask.getResult().size(), mockedRecipesList.size());
        assertEquals(matchingRecipesListTask.getResult().get(0).getRecipeId(),
                mockedRecipesList.get(0).getRecipeId());
    }
}