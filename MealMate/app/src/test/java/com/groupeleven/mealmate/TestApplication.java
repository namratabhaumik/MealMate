package com.groupeleven.mealmate;

import android.app.Application;
import com.google.firebase.FirebaseApp;

/**
 * Custom Application class used for testing purposes.
 * Initializes Firebase for testing environments.
 */
public class TestApplication extends Application {

    /**
     * Called when the application is starting. Initializes Firebase.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}