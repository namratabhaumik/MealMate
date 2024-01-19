package com.groupeleven.mealmate.Recipe;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;


import android.app.Activity;
import android.content.Intent;
import android.view.View;

import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.groupeleven.mealmate.Common.constants.CommonConstants;
import com.groupeleven.mealmate.Recipe.models.Recipe;
import com.groupeleven.mealmate.Recipe.utils.RecipeLoader;
import com.groupeleven.mealmate.SharedValues;
import com.groupeleven.mealmate.TestApplication;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestApplication.class)
public class MyRecipesActivityTest {
    private MockedStatic<FirebaseFirestore> mockStaticFirestore;
    private MockedStatic<FirebaseAuth> mockStaticAuth;
    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private FirebaseAuth mockAuth;
    @Mock
    private FirebaseUser mockUser;
    @Mock
    private Task<List<Recipe>> mockedTask;
    RecipeLoader recipeLoader;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mocking FirebaseFirestore Factory
        mockStaticFirestore = mockStatic(FirebaseFirestore.class);
        mockStaticFirestore.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);

        // Mocking FirebaseAuth Factory
        mockStaticAuth = mockStatic(FirebaseAuth.class);
        mockStaticAuth.when(FirebaseAuth::getInstance).thenReturn(mockAuth);
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);

        // Spy on the real RecipeLoader to retain original behavior
        recipeLoader = spy(new RecipeLoader(mockFirestore));
    }

    @After
    public void tearDown() {
        mockStaticFirestore.close();
        mockStaticAuth.close();
    }

    @Test
    public void loadUserRecipes_SuccessfulTask_LoadingUserRecipesList(){
        when(mockUser.getEmail()).thenReturn("test@email.com");

        doReturn(mockedTask).when(recipeLoader).loadUserRecipes(anyString());

        // Mocking Callback Invocation
        List<Recipe> userRecipes = new ArrayList<>();
        doAnswer(invocation -> {
            OnCompleteListener<List<Recipe>> listener = invocation.getArgument(0);
            listener.onComplete(Tasks.forResult(userRecipes));
            return null;
        }).when(mockedTask).addOnCompleteListener(any());

        // Building and Setting up MyRecipesActivity
        try (ActivityController<MyRecipesActivity> controller =
                     Robolectric.buildActivity(MyRecipesActivity.class)){
            MyRecipesActivity activity = controller.get();
            // Setting recipeLoader with Spy recipeLoader
            activity.recipeLoader = recipeLoader;
            controller.create().start().resume();

            assertNotNull(activity.userRecipes);
            assertEquals(activity.userRecipes.size(), userRecipes.size());
            assertEquals(SharedValues.getInstance().getInstructionsActivityCaller(),
                    "MyRecipesActivity");
            assertEquals(View.GONE, activity.loadingPB.getVisibility());
        }
    }

    @Test
    public void loadUserRecipes_UnsuccessfulTask_NotLoadingUserRecipesList(){
        when(mockUser.getEmail()).thenReturn("test@email.com");

        doReturn(mockedTask).when(recipeLoader).loadUserRecipes(anyString());

        doAnswer(invocation -> {
            OnCompleteListener<List<Recipe>> listener = invocation.getArgument(0);
            listener.onComplete(Tasks.forException(new Exception("Recipe loading failed.")));
            return null;
        }).when(mockedTask).addOnCompleteListener(any());

        try (ActivityController<MyRecipesActivity> controller =
                     Robolectric.buildActivity(MyRecipesActivity.class)){
            MyRecipesActivity activity = controller.get();
            activity.recipeLoader = recipeLoader;
            controller.create().start().resume();

            assertEquals(View.GONE, activity.loadingPB.getVisibility());
        }
    }

    @Test
    public void onActivityResult_loadUserRecipesCalled() {
        when(mockAuth.getCurrentUser()).thenReturn(null);

        try (ActivityController<MyRecipesActivity> controller =
                     Robolectric.buildActivity(MyRecipesActivity.class)){
            MyRecipesActivity activity = controller.get();
            activity.onActivityResult(CommonConstants.INSTRUCTIONS_REQUEST_CODE
                    , Activity.RESULT_OK, new Intent());
        }
    }
}