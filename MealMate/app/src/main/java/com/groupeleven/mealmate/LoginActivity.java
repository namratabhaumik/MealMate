package com.groupeleven.mealmate;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    RelativeLayout loginFragment;
    TextView registerTV;
    TextView forgotPasswordTV;
    Button loginBtn;
    EditText emailET;
    EditText passwordET;

    private static final int START_POS_LOGIN = -1700;
    private static final int START_POS_REGISTER = 3000;
    private static final int DELAY_LOGIN = 400;
    private static final int DELAY_REGISTER = 1100;
    private static final int ZERO_DELAY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setUpStatusbarColor();
        setUpViews();
        startAnimate();
    }

    /**
     * Logic to animate the views
     *
     * Why? An attempt to make the UI modern
     */
    private void startAnimate() {
        loginFragment.setY(START_POS_LOGIN);
        loginFragment.animate().translationY(ZERO_DELAY).setDuration(DELAY_LOGIN).setStartDelay(ZERO_DELAY);

        registerTV.setY(START_POS_REGISTER);
        registerTV.animate().translationY(ZERO_DELAY).setDuration(DELAY_REGISTER).setStartDelay(ZERO_DELAY);
    }

    /**
     * Assign views to their respective variables
     *
     * Why? These variables will be used to access and modify those views
     */
    private void setUpViews() {
        registerTV = findViewById(R.id.register_TV);
        loginBtn = findViewById(R.id.loginBtn);
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        loginFragment = findViewById(R.id.loginFragment);
        forgotPasswordTV = findViewById(R.id.forgotPasswordTV);

        loginBtn.setOnClickListener(v -> loginPressed());

        registerTV.setOnClickListener(v -> registerClicked());
        forgotPasswordTV.setOnClickListener(view -> {
            forgotPasswordPressed();
        });
    }

    /**
     * Logic to handle forgot password
     *
     * Why? To Allow the users to change their passwords and have a way to sign in even if they forget their password
     */
    private void forgotPasswordPressed() {
        String userEnteredMail = emailET.getText().toString();
        FirebaseAuth fbAuth = FirebaseAuth.getInstance();
        if (userEnteredMail.length() != 0) {
            fbAuth.sendPasswordResetEmail(userEnteredMail).addOnSuccessListener(unused -> {
                Toast.makeText(this, "Password reset mail sent!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "User not found :(", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Logic to handle the login button pressed
     *
     * Why? Required to store the logic that will be followed on the button press
     */
    private void loginPressed() {
        String userEnteredMail = emailET.getText().toString();
        String userEnteredPassWord = passwordET.getText().toString();

        FirebaseAuth fbAuth = FirebaseAuth.getInstance();
        fbAuth.signInWithEmailAndPassword(userEnteredMail, userEnteredPassWord).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (fbAuth.getCurrentUser() != null) {
                    if (fbAuth.getCurrentUser().isEmailVerified()) {
                        SharedPreferences sp = getSharedPreferences("MealMateSharedPreferences", MODE_PRIVATE);
                        sp.edit().putBoolean("isSignedIn", true).apply();
                        Toast.makeText(this, "LOGIN SUCCESSFUL!", Toast.LENGTH_SHORT).show();
                        sp.edit().putString("email",userEnteredMail).apply();
                        Intent mainActivityIntent = new Intent(this, MainActivity.class);
                        LoginActivity.this.startActivity(mainActivityIntent);
                    } else {
                        Toast.makeText(this, "Registration/Email verification pending.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Oops..incorrect password :(", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Logic to handle the register button pressed
     *
     * Why? Required to store the logic that will be followed on the button press
     */
    private void registerClicked() {
        Intent registerActivityIntent = new Intent(this, RegisterActivity.class);
        this.startActivity(registerActivityIntent);
    }

    /**
     * Logic to keep the status bar color as black
     *
     * Why? To maintain the UI consistency
     */
    private void setUpStatusbarColor() {
        Window appWindow = this.getWindow();
        appWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        appWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        appWindow.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loginFragment != null) {
            startAnimate();
        }
    }

    /**
     * Logic to handle the back pressed on Login screen
     *
     * Why? To avoid accidental closing of the application
     */
    @Override
    public void onBackPressed() {
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle("Leaving so soon?");
        alertDialog.setMessage("Are you sure you want to exit?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                (dialog, which) -> this.finishAffinity());
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                (dialog, which) -> alertDialog.dismiss());
        alertDialog.setCancelable(true);
        alertDialog.show();
    }
}