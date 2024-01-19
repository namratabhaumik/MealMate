package com.groupeleven.mealmate.AccountManagement;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.groupeleven.mealmate.R;

/**
 * DownloadsActivity class represents the activity for managing downloaded content in the MealMate application.
 * It extends the AppCompatActivity class and is responsible for handling the UI and functionality related to downloads.
 * The activity sets its content view to the downloads_layout resource, providing the user with the relevant interface.
 */
public class DownloadsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloads_layout);
    }
}
