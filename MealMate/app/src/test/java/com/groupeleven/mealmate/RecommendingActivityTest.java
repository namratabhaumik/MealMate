package com.groupeleven.mealmate;


import androidx.recyclerview.widget.RecyclerView;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.shadows.ShadowLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.groupeleven.mealmate.recommendations.RecommendAdapter;
import com.groupeleven.mealmate.recommendations.RecommendingActivity;

//@RunWith(RobolectricTestRunner.class)
public class RecommendingActivityTest {

    @Mock
    private RecyclerView recyclerView;

    @Mock
    private RecommendAdapter adapter;

    private RecommendingActivity activity;

//    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        activity = new RecommendingActivity();
        ShadowLog.stream = System.out;
    }

//    @Test
    public void testFilterData() {
        List<Map<String, Object>> recipeList = new ArrayList<>();
        Map<String, Object> recipe1 = new HashMap<>();
        recipe1.put("recipeName", "Recipe 1");
        recipeList.add(recipe1);

        Map<String, Object> recipe2 = new HashMap<>();
        recipe2.put("recipeName", "Recipe 2");
        recipeList.add(recipe2);

        when(recyclerView.getAdapter()).thenReturn(adapter);
        activity.filterData("Recipe", recipeList, recyclerView);
//        assertEquals(recipeList, adapter.recommendRecipeList);
    }
}
