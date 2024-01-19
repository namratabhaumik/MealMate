package com.groupeleven.mealmate;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

public class SplashActivity extends AppCompatActivity {

    LottieAnimationView splashAnimation;
    RelativeLayout appName;
    private static final int SLEEP_INTERVAL = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splashAnimation = findViewById(R.id.animation);
        appName = findViewById(R.id.appNameMealMate);

        Window appWindow = this.getWindow();
        appWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        appWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        appWindow.setStatusBarColor(ContextCompat.getColor(this,R.color.black));

        SharedPreferences sp = getSharedPreferences("MealMateSharedPreferences", MODE_PRIVATE);
        boolean isSignedIn = sp.getBoolean("isSignedIn", false);

        Intent nextActivityIntent;
        if (isSignedIn) {
            nextActivityIntent = new Intent(this, MainActivity.class);
        } else {
            nextActivityIntent = new Intent(this, LoginActivity.class);
        }
        Thread thread = new Thread() {
            public void run() {
                try {
                    sleep(SLEEP_INTERVAL);
                    startActivity(nextActivityIntent);
                    finish();
                } catch (InterruptedException e) {
                    Log.d("ERROR: ", e.toString());
                }
            }
        };
        thread.start();
    }
}