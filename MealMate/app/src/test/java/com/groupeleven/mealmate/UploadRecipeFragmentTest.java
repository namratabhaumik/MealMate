package com.groupeleven.mealmate;

import android.os.Build;
import android.widget.Button;
import android.widget.Toast;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

//@RunWith(RobolectricTestRunner.class)
public class UploadRecipeFragmentTest {
    private UploadRecipeFragment uploadRecipefragment;
    private Button uploadBtn;

//    @Before
    public void setUp() {
        uploadRecipefragment = new UploadRecipeFragment();
        uploadRecipefragment.onCreate(null);
        uploadBtn = uploadRecipefragment.uploadBtn;
    }

//    @After
    public void tearDown() {
        // Clean up resources if needed
    }

//    @Test
    public void testListItemClick() {
        Toast mockToast = mock(Toast.class);
        uploadBtn.performClick();
        verify(mockToast).show();
    }
}