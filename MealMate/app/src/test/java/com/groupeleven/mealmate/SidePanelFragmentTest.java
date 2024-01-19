package com.groupeleven.mealmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.ListView;
import android.widget.Toast;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.groupeleven.mealmate.AccountManagement.SidePanelFragment;

//@RunWith(RobolectricTestRunner.class)
public class SidePanelFragmentTest {

    private SidePanelFragment fragment;
    private ListView listView;

//    @Before
    public void setUp() throws Exception {
        // Create a new SidePanelFragment
        fragment = new SidePanelFragment();
        fragment.onCreate(null);

        // Access the ListView from the fragment
        listView = fragment.getView().findViewById(R.id.list_view);
    }

//    @After
    public void tearDown() throws Exception {
        // Clean up resources if needed
    }

//    @Test
    public void testListViewNotNull() {
        // Check if the ListView is not null
        assertNotNull(listView);
    }

//    @Test
    public void testListViewItemCount() {
        // Check if the ListView is populated with items
        int itemCount = listView.getAdapter().getCount();
        assertTrue(itemCount > 0);
    }

//    @Test
    public void testListItemClick() {
        // Mock the Intent and Toast for item click testing
        Intent mockIntent = mock(Intent.class);
        Toast mockToast = mock(Toast.class);

        // Create a click event on the first item in the ListView
        listView.performItemClick(listView.getAdapter().getView(0, null, null), 0, listView.getAdapter().getItemId(0));

        // Verify that the correct intent is started
        verify(fragment.getActivity()).startActivity(mockIntent);
    }

//    @Test
    public void testLogoutItemClick() {
        // Mock SharedPreferences, Intent, and Toast for logout item click testing
        SharedPreferences mockSharedPreferences = mock(SharedPreferences.class);
        Intent mockIntent = mock(Intent.class);
        Toast mockToast = mock(Toast.class);

        // Set up the SharedPreferences to return false when isSignedIn is queried
        when(mockSharedPreferences.getBoolean(eq("isSignedIn"), anyBoolean())).thenReturn(false);

        // Create a click event on the "Logout" item in the ListView
        listView.performItemClick(listView.getAdapter().getView(5, null, null), 5, listView.getAdapter().getItemId(5));

        // Verify that the SharedPreferences are updated and the correct intent is started
        verify(mockSharedPreferences.edit()).putBoolean(eq("isSignedIn"), eq(false));
        verify(fragment.getActivity()).startActivity(mockIntent);

        // Verify that a "Logged Out" toast message is shown
        verify(mockToast).show();
    }
}
