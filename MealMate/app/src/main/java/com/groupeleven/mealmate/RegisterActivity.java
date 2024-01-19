package com.groupeleven.mealmate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class  RegisterActivity extends AppCompatActivity {

    RelativeLayout registerFragment;
    Button registerBtn;
    EditText nameET;
    EditText emailET;
    EditText passwordET;
    private static final int INI_POS_TOP = -1700;
    private static final int ANIM_DUR = 400;
    private static final int ZERO_DELAY = 0;
    private static final int MIN_PASS_LEN = 8;

    private FirebaseFirestore fbdb = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setUpStatusbarColor();

        registerFragment = findViewById(R.id.registerFragment);
        registerFragment.setY(INI_POS_TOP);
        registerFragment.animate().translationY(ZERO_DELAY).setDuration(ANIM_DUR).setStartDelay(ZERO_DELAY);

        setUpViews();
    }

    /**
     * Assign the views with their respective variables
     *
     * Why? Those variables is the only way the code will acknowledge the UI behaviour
     */
    private void setUpViews() {
        registerBtn = findViewById(R.id.registerBtn);
        nameET = findViewById(R.id.nameET);
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerPressed();
            }
        });
    }

    /**
     * Logic to handle the register button pressed
     *
     * Why? Check if the email entered follows the email format
     */
    private void registerPressed() {
        String nameEntered = nameET.getText().toString();
        String emailEntered = emailET.getText().toString();
        String passwordEntered = passwordET.getText().toString();

        if (!emailEntered.contains("@")) {
            Toast.makeText(this, "Email Invalid", Toast.LENGTH_SHORT).show();
        }
        else if (passwordEntered.length() < MIN_PASS_LEN) {
            Toast.makeText(this, "Password must be at least 8 characters long.", Toast.LENGTH_SHORT).show();
        } else {
            createUser(nameEntered, emailEntered, passwordEntered);
        }
    }

    /**
     * Logic to create the user on Firebase Authentication Instance
     *
     * Why? Required to store the logic that will be followed on the button press
     */
    private void createUser(String nameEntered, String emailEntered, String passwordEntered) {
        FirebaseAuth fbAuth = FirebaseAuth.getInstance();
        fbAuth.createUserWithEmailAndPassword(emailEntered, passwordEntered).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                registerSuccess(nameEntered, emailEntered, passwordEntered, fbAuth);
            } else {
                Toast.makeText(this, "Could not create user:( ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Logic to handle successful user creation
     *
     * Why? Required to let the user know that they have to verify their email before logging in
     */
    private void registerSuccess(String nameEntered, String emailEntered, String passwordEntered, FirebaseAuth fbAuth) {
        if (fbAuth.getCurrentUser() != null) {
            fbAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    Toast.makeText(this, "Registration complete. Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                    Intent loginActivity = new Intent(this, LoginActivity.class);
                    this.startActivity(loginActivity);
                } else {
                    Toast.makeText(this, "Verification email could not be sent :(", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Logic to change the status bar color of the activity to black
     *
     * Why? For UI consistency
     */
    private void setUpStatusbarColor() {
        Window appWindow = this.getWindow();
        appWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        appWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        appWindow.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
    }
}