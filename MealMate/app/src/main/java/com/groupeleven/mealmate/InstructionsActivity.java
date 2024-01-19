package com.groupeleven.mealmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.groupeleven.mealmate.Common.constants.EmailMessages;
import com.groupeleven.mealmate.Recipe.models.Recipe;
import com.groupeleven.mealmate.Recipe.utils.RecipeLoader;

import java.util.ArrayList;
import java.util.Map;

public class InstructionsActivity extends AppCompatActivity {
    Context context;
    RecipeLoader recipeLoader;

    /**
     * Method that initiates the activity
     *
     * Why? To assign a view to the activity and get its child components to be used in this activity
     *
     * @param savedInstanceState Required for activity initialization
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        context = this;

        ImageButton shareIconIB = findViewById(R.id.shareIconIB);
        ImageButton editRecipeIB = findViewById(R.id.editRecipeIB);
        ImageButton deleteRecipeBtn = findViewById(R.id.deleteRecipeBtn);
        setStatusBarColor();

        recipeLoader = new RecipeLoader(FirebaseFirestore.getInstance());
        InstructionsAdapter instructionsVPAdapter = setupInstructionsAdapter(this);
        shareIconIB.bringToFront();

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(instructionsVPAdapter);

        Recipe currentRecipeFromShared = SharedValues.getInstance().getCurrentRecipe();
        setIconVisibilities(editRecipeIB, deleteRecipeBtn, currentRecipeFromShared);

        shareIconIB.setOnClickListener(view -> {
            shareClicked(context);
        });

        editRecipeIB.setOnClickListener(view -> {
            editClicked(context);
        });

        deleteRecipeBtn.setOnClickListener(view -> {
            AlertDialog deleteDialog = new AlertDialog.Builder(context).create();
            deleteDialog.setTitle("Are you sure?");
            deleteDialog.setMessage("This will delete your selected recipe.");
            deleteDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                    (dialog, which) -> dialog.dismiss());
            deleteDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", (dialogInterface, i) -> {
                FirebaseFirestore fbdb = FirebaseFirestore.getInstance();
                Recipe currentRecipe = SharedValues.getInstance().getCurrentRecipe();
                fbdb.collection(FireBaseEntities.RECIPES).document(currentRecipe.getRecipeId())
                        .delete();
                Toast.makeText(this, "Recipe Deleted!", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
            });
            deleteDialog.show();
        });
    }

    /**
     * Displays an AlertDialog to confirm whether the user wants to edit a recipe.
     * If the user selects "YES," the method sets a flag indicating that the user has chosen to edit
     * a recipe using SharedValues.getInstance().setEditRecipeSelected(true). It then launches the
     * MainActivity which in turn opens the upload recipe fragment which has the values prepopulated
     * for the user's minimal input.
     *
     * @param context The context in which the AlertDialog should be displayed i.e. This activity.
     */
    private static void editClicked(Context context) {
        AlertDialog editDialog = new AlertDialog.Builder(context).create();
        editDialog.setTitle("Are you sure?");
        editDialog.setMessage("Do you want to edit this recipe of yours?");
        editDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                (dialog, which) -> dialog.dismiss());
        editDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", (dialogInterface, i) -> {
            SharedValues.getInstance().setEditRecipeSelected(true);
            Intent mainActivity = new Intent(context, MainActivity.class);
            context.startActivity(mainActivity);
        });
        editDialog.show();
    }

    /**
     * Displays an AlertDialog to prompt the user for input before initiating a "share" action.
     *
     * Why? Getting confirmation from the user as the latter  steps will require their engagement.
     *
     * If the user selects "OK," the method triggers the {@link #okClicked(EditText, DialogInterface, Context)}
     * method, passing the user input, the dialog, and the context as parameters.
     *
     * @param context The context in which the AlertDialog should be displayed.i.e This activity
     */
    private static void shareClicked(Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        final EditText input = setupAlertDialog(alertDialogBuilder, context);
        alertDialogBuilder.setPositiveButton("OK", (dialog, which) -> {
            okClicked(input, dialog, context);
        });
        alertDialogBuilder.setNegativeButton("Cancel",
                (dialog, which) -> dialog.cancel());
        alertDialogBuilder.show();
    }

    /**
     * Configures an AlertDialog with a title and an EditText input field for entering an email address.
     *
     * Why? Seperation of concern between the logic to show the dialog and the logic to structure it.
     *
     * @param alertDialogBuilder The AlertDialog.Builder instance to configure.
     * @param context The context in which the AlertDialog is being created.
     * @return The EditText instance that represents the input field for the email address.
     */
    @NonNull
    private static EditText setupAlertDialog(AlertDialog.Builder alertDialogBuilder, Context context) {
        alertDialogBuilder.setTitle("Please enter the email " +
                "of the person you want to share this recipe with:");
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                | InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
        alertDialogBuilder.setView(input);
        return input;
    }

    /**
     * Checking if the email entered is atleast of email format
     *
     * Why? An attempt to stop unguided use of the feaature
     *
     * @param input the edittext in which the user entered an email
     * @param dialog the dialog which contains the above edit text
     * @param context The context of he activity which is required to open the email sharing fragment.
     */
    private static void okClicked(EditText input, DialogInterface dialog, Context context) {
        String emailInput = String.valueOf(input.getText());
        if (emailInput.contains("@") & emailInput.contains(".")) {
            putEmailInSharedRecipesDocument(emailInput, context);

            openTheSendEmailFragment(dialog, emailInput, context);
        } else {
            Toast.makeText(context, "The email seems" +
                    " invalid :(", Toast.LENGTH_SHORT).show();
        }
        dialog.dismiss();
    }

    /**
     * Opening android's email service picker for the user to share their recipe
     *
     * Why? To open the user's mailing client to share the recipe selected as well as a way to access it.
     *
     * @param dialog The Dialog instance to be dismissed.
     * @param emailInput The email to be set as receiver.
     * @param context The context in which the AlertDialog was created.
     */
    private static void openTheSendEmailFragment(DialogInterface dialog, String emailInput, Context context) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailInput});
        intent.putExtra(Intent.EXTRA_SUBJECT, EmailMessages.RECIPE_SHARE_BODY);
        intent.putExtra(Intent.EXTRA_TEXT, EmailMessages.RECIPE_SHARE_SUBJECT);
        context.startActivity(Intent.createChooser(intent, "Choose an Email" +
                " client :"));
        dialog.dismiss();
    }

    /**
     * Task to add the entered email in our database that stores all the shared recipes
     *
     * Why? Seperating the DB tasks from the UI tasks.
     *
     * @param emailInput the email to be entered.
     * @param context The context of the activity which is required to open the email sharing fragment.
     */
    private static void putEmailInSharedRecipesDocument(String emailInput, Context context) {
        DocumentReference shareRecipesReference;
        FirebaseFirestore fbdb = FirebaseFirestore.getInstance();
        shareRecipesReference = fbdb.collection(
                FireBaseEntities.SHR_RECI_COL)
                .document(FireBaseEntities.SHR_REC_DOC);

        shareRecipesReference.get().addOnSuccessListener(documentSnapshot -> {
            addOrPutEmailInTheMap(emailInput, shareRecipesReference, documentSnapshot, context);
        }).addOnFailureListener(e -> {
            Toast.makeText(context,
                    "Something went wrong :(", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Logic to form a map containing all the recipes shared with all the recipients
     *
     * Why? this map will be used to decide weather the user is already added in the db or do we have to create a new entry
     *
     * @param emailInput the email user entered.
     * @param shareRecipesReference the document with all the shared emails
     * @param documentSnapshot snapshot returned by the calling method
     * @param context The context of the activity.
     */
    private static void addOrPutEmailInTheMap(String emailInput, DocumentReference shareRecipesReference, DocumentSnapshot documentSnapshot, Context context) {
        Map<String, Object> data = documentSnapshot.getData();
        assert data != null;
        ArrayList<String> keys = new ArrayList<>(data.keySet());
        if (keys.contains(emailInput)) {
            ArrayList<String> recipesShared =
                    (ArrayList<String>) data.get(emailInput);
            recipesShared.add(SharedValues.getInstance()
                    .getCurrentRecipe().getRecipeId());
            data.put(emailInput, recipesShared);
        } else {
            ArrayList<String> recipesShared = new ArrayList<>();
            recipesShared.add(SharedValues.getInstance()
                    .getCurrentRecipe().getRecipeId());
            data.put(emailInput, recipesShared);
        }

        uploadUpdatedValues(shareRecipesReference, data, context);
    }

    /**
     * Task to upload the updated values to shared recipes document
     *
     * Why? To seperate the upload task from other operations
     *
     * @param shareRecipesReference The AlertDialog.Builder instance to configure.
     * @param data The context in which the AlertDialog is being created.
     * @param context The context to show the toast msg.
     */
    private static void uploadUpdatedValues(DocumentReference shareRecipesReference, Map<String, Object> data, Context context) {
        Task<Void> uploadTask = shareRecipesReference.set(data);

        uploadTask.addOnSuccessListener(unused -> {
            Toast.makeText(context,
                    "Success!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(context,
                    "Something went wrong :(", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * UI task to set the visibilities of the respective views according to the authority
     *
     * Why? To only show the authors the option of editing recipes
     *
     * @param editRecipeIB Edit button reference
     * @param deleteRecipeBtn Delete button reference
     * @param currentRecipeFromShared Recipe opened object
     */
    private static void setIconVisibilities(ImageButton editRecipeIB, ImageButton deleteRecipeBtn, Recipe currentRecipeFromShared) {
        String owner = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (currentRecipeFromShared.getUserEmail().contentEquals(owner)) {
            deleteRecipeBtn.setVisibility(View.VISIBLE);
            editRecipeIB.setVisibility(View.VISIBLE);
            deleteRecipeBtn.bringToFront();
            editRecipeIB.bringToFront();
        } else {
            deleteRecipeBtn.setVisibility(View.GONE);
            editRecipeIB.setVisibility(View.GONE);
        }

        String instructionActivityCaller = SharedValues.getInstance().getInstructionsActivityCaller();
        if (instructionActivityCaller!= null && instructionActivityCaller.equals("MealPlannerFragment")){
            deleteRecipeBtn.setVisibility(View.GONE);
            editRecipeIB.setVisibility(View.GONE);
        }
    }

    /**
     * UI task to set up the instructions view adapter
     *
     * Why? To load the recipes in different views
     *
     * @param context Required to initiate the adapter
     * @return instructionsVPAdapter To be added ad adapter for its respective recycler view
     */
    @NonNull
    private static InstructionsAdapter setupInstructionsAdapter(Context context) {
        ArrayList<String> instructions = new ArrayList<>();
        instructions.add("");
        instructions.addAll(SharedValues.getInstance().getInstructions());
        InstructionsAdapter instructionsVPAdapter = new InstructionsAdapter(instructions, context);
        return instructionsVPAdapter;
    }

    /**
     * UI task to set up the color of the status bar
     *
     * Why? To keep the UI consistent
     */
    private void setStatusBarColor() {
        Window appWindow = this.getWindow();
        appWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        appWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        appWindow.setStatusBarColor(ContextCompat.getColor(this,R.color.black));
    }
}