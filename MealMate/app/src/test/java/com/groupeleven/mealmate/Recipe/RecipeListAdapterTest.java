package com.groupeleven.mealmate.Recipe;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.groupeleven.mealmate.R;
import com.groupeleven.mealmate.Recipe.models.Recipe;
import com.groupeleven.mealmate.Recipe.utils.RecipeListAdapter;
import com.groupeleven.mealmate.TestApplication;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestApplication.class)
public class RecipeListAdapterTest {
    @Mock
    private ViewGroup mockViewGroup;
    @Mock
    private LayoutInflater mockLayoutInflater;
    @Mock
    private View mockView;
    @Mock
    private Context mockContext;
    private RecipeListAdapter recipeListAdapter;
    private final ArrayList<Recipe> mockedRecipesList = new ArrayList<>();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Context appContext = RuntimeEnvironment.getApplication();

        Recipe mockedRecipe = getMockedRecipe();
        mockedRecipesList.add(mockedRecipe);

        recipeListAdapter = new RecipeListAdapter(mockedRecipesList,
                appContext, "RecipeListAdapterTest");

        LayoutInflater layoutInflater = LayoutInflater.from(appContext);

        FrameLayout mockFrameLayout = new FrameLayout(appContext);

        mockView = layoutInflater.inflate(R.layout.home_screen_card, mockFrameLayout, false);
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
    public void onCreateViewHolder_ReturnNonNullViewHolder() {
        when(mockViewGroup.getContext()).thenReturn(mockContext);
        when(mockContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .thenReturn(mockLayoutInflater);

        when(mockLayoutInflater.inflate(R.layout.home_screen_card, mockViewGroup, false))
                .thenReturn(mockView);

        RecipeListAdapter.RecipeViewHolder viewHolder = recipeListAdapter
                .onCreateViewHolder(mockViewGroup, 0);

        assertNotNull(viewHolder);
    }

    @Test
    public void onBindViewHolder_FirstPosition() {
        RecipeListAdapter.RecipeViewHolder mockViewHolder = new RecipeListAdapter
                .RecipeViewHolder(mockView);

        recipeListAdapter.onBindViewHolder(mockViewHolder, 0);
    }

    @Test
    public void onBindViewHolder_OtherPositionThanFirst() {
        RecipeListAdapter.RecipeViewHolder mockViewHolder = new RecipeListAdapter
                .RecipeViewHolder(mockView);

        recipeListAdapter.onBindViewHolder(mockViewHolder, 1);
    }
}