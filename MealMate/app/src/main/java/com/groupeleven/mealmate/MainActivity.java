package com.groupeleven.mealmate;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.groupeleven.mealmate.AccountManagement.SidePanelFragment;
import com.groupeleven.mealmate.Ingredients.IngredientsFragment;
import com.groupeleven.mealmate.MealPlanner.MealPlannerFragment;


public class MainActivity extends AppCompatActivity {
    BottomNavigationView navigationView;
    FloatingActionButton floatingAddBtn;
    private static final int UPLOAD_ICON_POSITION = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        setStatusBarColor();

        navigationView = findViewById(R.id.navigationView);
        navigationView.setBackground(null);
        SharedValues.getInstance().setNavigationView(navigationView);
        setUpNavigationBar();

        floatingAddBtn = findViewById(R.id.floatingAddBtn);
        floatingAddBtn.setOnClickListener(v -> loadFragment(new UploadRecipeFragment()));

        if (SharedValues.getInstance().isEditRecipeSelected()) {
            loadFragment(new UploadRecipeFragment());
        } else {
            loadFragment(new HomeFragment());
        }
    }

    private void setUpNavigationBar() {
        navigationView.getMenu().getItem(UPLOAD_ICON_POSITION).setEnabled(false);
        navigationView.setOnItemSelectedListener(item -> {
            CharSequence title = item.getTitle();
            assert title != null;
            if (title.equals("Home")) {
                loadFragment(new HomeFragment());
            } else if (title.equals("Search")) {
                loadFragment(new IngredientsFragment());
            } else if(title.equals("Upload Recipe")){
                loadFragment(new UploadRecipeFragment());
            } else if (title.equals("Meal Planner")) {
                loadFragment(new MealPlannerFragment());
            } else if (title.equals("Account")) {
                loadFragment(new SidePanelFragment());
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm =  getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frame, fragment);
        ft.commit();
    }

    private void setStatusBarColor() {
        Window appWindow = this.getWindow();
        appWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        appWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        appWindow.setStatusBarColor(ContextCompat.getColor(this,R.color.black));
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            boolean returnToHome = data.getBooleanExtra("RETURN_TO_HOME", false);
            if (returnToHome) {
                loadFragment(new HomeFragment());
            }
        }
    }


}



