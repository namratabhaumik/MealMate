package com.groupeleven.mealmate.AccountManagement;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.groupeleven.mealmate.R;

/**
 * Represents the activity displaying the user's profile information.
 * This activity is responsible for showing user-specific details and preferences.
 */
public class YourProfileActivity extends AppCompatActivity {

    /**
     * Called when the activity is starting,
     * calling {@code setContentView(int)} to inflate the activity's UI,
     * using {@code findViewById} to programmatically interact with widgets in the UI,
     * and initializing data needed for the activity.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down, this Bundle contains the data most recently supplied
     *                           in {@link #onSaveInstanceState(Bundle)}, or null if this activity
     *                           is being started for the first time.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.your_profile_layout);
    }
}
