package com.groupeleven.mealmate;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.groupeleven.mealmate.Recipe.models.Recipe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class UploadRecipeFragment extends androidx.fragment.app.Fragment {
    Button selectIngredientsBtn;
    Button uploadBtn;
    Button chooseImageBtn;
    Switch vegeterianSwitch;
    EditText titleET;
    EditText descriptionET;
    EditText instructionsET;
    ImageButton add_btn;
    ImageView selectedImage;
    ImageButton subtract_btn;
    ImageButton subtract_btn_for_time;
    ImageButton add_btn_for_time;
    TextView servingSizeTV;
    TextView servingSizeTV_for_time;
    RelativeLayout loadingPB;
    RelativeLayout everythingElse;
    private CollectionReference categoriesColRef;
    private CollectionReference ingredientsColRef;
    private StorageReference storageReference;
    private DatabaseReference dbReference;
    private static final int TIME_INCR = 30;
    private static final int UPL_POS = 2;
    private static final float NEG_APLHA = 0.5F;
    FirebaseFirestore fbdb = FirebaseFirestore.getInstance();

    /**
     * Required to initialze the fragment
     *
     * Why? To show the fields the user has to enter in order to upload their recipe
     *
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_upload_recipe, container, false);
        SharedValues.getInstance().getNavigationView().getMenu().getItem(UPL_POS).setChecked(true);
        assignValues(v);

        UploadRecipeSingleton.getInstance().delete();

        if (SharedValues.getInstance().isEditRecipeSelected()) {
            prePopulateUploadRecipeScreen();
        }

        SharedValues.getInstance().setCategorywiseIngredients(new HashMap<>());
        SharedValues.getInstance().setIngredientsUnits(new HashMap<>());
        categoriesColRef.get().addOnCompleteListener(UploadRecipeFragment::onCompleteLogic);

        ingredientsColRef = fbdb.collection("Ingredients");
        ingredientsColRef.get().addOnCompleteListener(task -> {
            onCompleteLogicForIngredients(task);
        });
        setUpBehaviours();
        return v;
    }

    /**
     * Add the values returned from the Firebase to an Arraylist of ingredients
     *
     * Why? To get all the available options for the user to select their ingredients
     *
     * @param task Required to return its result which contains the information about all the ingredients
     */
    private void onCompleteLogicForIngredients(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            for (QueryDocumentSnapshot document : task.getResult()) {
                Map<String, Object> data = document.getData(); // Get the document data
                String name = data.get("name").toString();
                String category = data.get("category").toString();
                String unit = data.get("unit").toString();
                addIngredientToCategory(category, name);
                addUnitToIngredient(name, unit);
            }
        } else {
            Exception exception = task.getException();
            if (exception != null) {
                Log.e("Firebase Exception", exception.toString());
            }
        }
    }

    /**
     * Add the values returned from the Firebase to an Arraylist of categories
     *
     * Why? To get all the available options for the user to select their ingredients from their respective categories
     *
     * @param task Required to return its result which contains the information about all the categories
     */
    private static void onCompleteLogic(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            ArrayList<String> categories = new ArrayList<>();
            for (QueryDocumentSnapshot document : task.getResult()) {
                Map<String, Object> data = document.getData();
                String name = data.get("Name").toString();
                categories.add(name);
            }
            SharedValues.getInstance().setCategories(categories);
        } else {
            Exception exception = task.getException();
            if (exception != null) {
                Log.e("Firebase Exception", exception.toString());
            }
        }
    }

    /**
     * Add information from the existing recipe
     *
     * Why? So that the user can change the items they want to change and not re enter all the details again
     *
     */
    private void prePopulateUploadRecipeScreen() {
        Recipe currentRecipe = SharedValues.getInstance().getCurrentRecipe();
        String updatedName = currentRecipe.getRecipeName();
        titleET.setText(updatedName);
        descriptionET.setText(currentRecipe.getRecipeDescription());
        vegeterianSwitch.setChecked(currentRecipe.isVegetarian());
        vegeterianSwitch.setEnabled(false);
        vegeterianSwitch.setAlpha(NEG_APLHA);
        selectIngredientsBtn.setEnabled(false);
        add_btn.setEnabled(false);
        subtract_btn.setEnabled(false);
        selectIngredientsBtn.setAlpha(NEG_APLHA);
        add_btn.setAlpha(NEG_APLHA);
        subtract_btn.setAlpha(NEG_APLHA);
        String instructions = "";
        for (int i = 0; i < currentRecipe.getInstructions().size(); i++) {
            if (i == 0) {
                instructions = instructions + currentRecipe.getInstructions().get(i);
            } else {
                instructions = instructions + "\n" + currentRecipe.getInstructions().get(i);
            }

            if (i == currentRecipe.getInstructions().size() - 1) {
                instructionsET.setText(instructions);
            }
        }
        UploadRecipeSingleton.getInstance().setTimeTakenToPrepare(Integer.parseInt(currentRecipe.getTime().split(" ")[0]));
        UploadRecipeSingleton.getInstance().setIngredients(currentRecipe.getIngredients());
        UploadRecipeSingleton.getInstance().setVegeterian(currentRecipe.isVegetarian());
        UploadRecipeSingleton.getInstance().setServingSize(Integer.parseInt(currentRecipe.getServingSize()));
        servingSizeTV.setText(String.valueOf(currentRecipe.getServingSize()));
        servingSizeTV_for_time.setText(currentRecipe.getTime().split("\\s+")[0]);
        SharedValues.getInstance().setEditRecipeSelected(false);
    }

    /**
     * Assign the views to their respective variables
     *
     * Why? They will be used by the code logic to interact with the views and their status
     *
     * @param v Parent view which contains all the children views required by this fragment
     */
    private void assignValues(View v) {
        selectIngredientsBtn = v.findViewById(R.id.selectIngredientsBtn);
        uploadBtn = v.findViewById(R.id.uploadBtn);
        uploadBtn.setEnabled(true);
        uploadBtn.setAlpha(1.0F);
        vegeterianSwitch = v.findViewById(R.id.vegeterianSwitch);
        titleET = v.findViewById(R.id.titleET);
        descriptionET = v.findViewById(R.id.descriptionET);
        instructionsET = v.findViewById(R.id.instructionsET);
        add_btn = v.findViewById(R.id.add_btn);
        subtract_btn = v.findViewById(R.id.subtract_btn);
        subtract_btn_for_time = v.findViewById(R.id.subtract_btn_for_time);
        add_btn_for_time = v.findViewById(R.id.add_btn_for_time);
        servingSizeTV = v.findViewById(R.id.servingSizeTV);
        servingSizeTV_for_time = v.findViewById(R.id.servingSizeTV_for_time);
        chooseImageBtn = v.findViewById(R.id.chooseImageBtn);
        selectedImage = v.findViewById(R.id.selectedImage);
        loadingPB = v.findViewById(R.id.loadingPB);
        everythingElse = v.findViewById(R.id.everythingElse);
        loadingPB.setVisibility(View.GONE);
        everythingElse.setVisibility(View.VISIBLE);
        categoriesColRef = fbdb.collection("Categories");
        dbReference = FirebaseDatabase.getInstance().getReference("uploads");
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
    }

    /**
     * Add the values returned from the Firebase to an Arraylist of categories maps
     *
     * Why? To store the categories and ingredients in relation with each other
     *
     * @param category Category name
     * @param ingredient Respective ingredient name
     */
    void addIngredientToCategory(String category, String ingredient) {
        HashMap<String, ArrayList<String>> categoryIngredientsMap = SharedValues.getInstance().getCategorywiseIngredients();
        Set<String> keys = categoryIngredientsMap.keySet();
        if (keys.isEmpty() || !keys.contains(category)) {
            ArrayList<String> ingredients = new ArrayList<>();
            ingredients.add(ingredient);
            categoryIngredientsMap.put(category, ingredients);
        } else {
            ArrayList<String> ingredients = categoryIngredientsMap.get(category);
            ingredients.add(ingredient);
            categoryIngredientsMap.replace(category, ingredients);
        }
        SharedValues.getInstance().setCategorywiseIngredients(categoryIngredientsMap);
    }

    /**
     * Add the unit for the ingredient in discussion
     *
     * Why? Using this we will be able to keep the increment relative to the ingredient
     * e.g. Increment 1 Unit for eggs and increment 100 Gms for rice
     *
     * @param ingredient Ingredient name
     * @param unit Unit for the ingredient in discussion
     */
    public void addUnitToIngredient(String ingredient, String unit) {
        HashMap<String, String> ingredientsUnits = SharedValues.getInstance().getIngredientsUnits();
        ingredientsUnits.put(ingredient, unit);
        SharedValues.getInstance().setIngredientsUnits(ingredientsUnits);
    }

    /**
     * Setting up the brhavious of all the ui elements i.e. OnClick
     *
     * Why? Keeping the onClick logic of all the components in a designated place so it is easy to debug when in need
     *
     */
    private void setUpBehaviours() {
        selectIngredientsBtn.setOnClickListener(view -> {
            CategoriesDialog dialog = new CategoriesDialog();
            dialog.show(getFragmentManager(), "Categories Dialog");
        });
        vegeterianSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            UploadRecipeSingleton.getInstance().setVegeterian(b);
        });

        // We need to keep the below two complex conditional methods
        titleET.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH ||
                    i == EditorInfo.IME_ACTION_DONE ||
                    keyEvent != null &&
                    keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                    keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER
            ) {
                if (keyEvent == null || !keyEvent.isShiftPressed()) {
                    UploadRecipeSingleton singleton = UploadRecipeSingleton.getInstance();
                    singleton.setRecipeName(String.valueOf(titleET.getText()));
                    return true;
                }
            }
            return false;
        });

        descriptionET.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH ||
                i == EditorInfo.IME_ACTION_DONE ||
                keyEvent != null &&
                keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER
            ) {
                if (keyEvent == null || !keyEvent.isShiftPressed()) {
                    UploadRecipeSingleton singleton = UploadRecipeSingleton.getInstance();
                    singleton.setRecipeName(String.valueOf(descriptionET.getText()));
                    return true;
                }
            }
            return false;
        });

        add_btn.setOnClickListener(view -> {
            incrementServingSize();
        });

        subtract_btn.setOnClickListener(view -> {
            decrementServingSize();
        });

        add_btn_for_time.setOnClickListener(view -> {
            incrementTimeTaken();
        });

        subtract_btn_for_time.setOnClickListener(view -> {
            decrementTimeTaken();
        });

        chooseImageBtn.setOnClickListener(view -> {
            openFileExplorer();
        });

        uploadBtn.setOnClickListener(view -> {
            uploadRecipe();
        });
    }

    /**
     * Check and setup all the values entered by the user
     *
     * Why? We need all the fields to be entered before uploading
     *
     */
    private void uploadRecipe() {
        uploadBtn.setEnabled(false);
        uploadBtn.setAlpha(NEG_APLHA);
        everythingElse.setVisibility(View.GONE);
        loadingPB.setVisibility(View.VISIBLE);
        UploadRecipeSingleton.getInstance().setRecipeName(titleET.getText().toString());
        UploadRecipeSingleton.getInstance().setRecipeDesc(descriptionET.getText().toString());
        String instructions = instructionsET.getText().toString();
        String[] instructionsStrArr = instructions.split("\n");
        ArrayList<String> instructionsAL = new ArrayList<>(Arrays.asList(instructionsStrArr));
        UploadRecipeSingleton.getInstance().setInstructions(instructionsAL);
        FirebaseAuth fba = FirebaseAuth.getInstance();
        String userEmail = fba.getCurrentUser().getEmail();
        String timeRN = String.valueOf(System.currentTimeMillis());

        if (isEverythingEntered()) {
            checkForDuplicationAndUpload(userEmail, timeRN);
        } else {
            Toast.makeText(getContext(), "Please do not leave the mandatory fields empty!", Toast.LENGTH_SHORT).show();
        }
        everythingElse.setVisibility(View.VISIBLE);
        loadingPB.setVisibility(View.GONE);
    }

    /**
     * Making sure that the already existing recipes do not have the same name as entered by the user
     *
     * Why? Requirement by the client to not have two recipes with the same name
     *
     * @param userEmail Required to check if the user is the owner of the recipe he is about to upload
     * @param timeRN this variable is used to name the images uploaded
     */
    private void checkForDuplicationAndUpload(String userEmail, String timeRN) {
        if (checkCondition()) {
            Toast.makeText(getContext(), "This Recipe Name is already in use :(", Toast.LENGTH_SHORT).show();
        } else {
            deleteExistingRecipe();
            String imagePathForRecipe = "";
            imagePathForRecipe = uploadImageIfAny(timeRN, imagePathForRecipe);
            Map<String, Object> entry = new HashMap<>();//
            createMApForTheRecipe(userEmail, imagePathForRecipe, entry);
            uploadRecipeToFirebase(timeRN, entry);
        }
    }

    /**
     * Check if the user is the owner of the same recipe already existing on the server
     *
     * Why? We want the user to overwrite their own recipes if they want to
     *
     */
    private boolean checkCondition() {
        Recipe currentRecipe = SharedValues.getInstance().getCurrentRecipe();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        boolean isOwner = false;
        if (currentRecipe != null) {
            String ownerEmail = currentRecipe.getUserEmail();
            isOwner = ownerEmail.contentEquals(userEmail);
        }
        boolean isDuplicated = isNameDuplicated(titleET.getText().toString());
        return (isDuplicated && !isOwner);
    }

    /**
     * Delete the recipe with the same name from the dataase
     *
     * Why? To rewrite the recipe again, we need to delete the existing recipe
     *
     */
    private void deleteExistingRecipe() {
        Recipe currentRecipe = SharedValues.getInstance().getCurrentRecipe();
        if (currentRecipe != null) {
            String recipeID = currentRecipe.getRecipeId();
            String ownerEmail = SharedValues.getInstance().getCurrentRecipe().getUserEmail();
            String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if (ownerEmail.contentEquals(userEmail)) {
                CollectionReference recipeCollection = fbdb.collection(FireBaseEntities.RECIPES);
                DocumentReference recipeDocument = recipeCollection.document(recipeID);
                recipeDocument.delete();
            }
        }
    }

    /**
     * Task to upload the recipe to firebase database
     *
     * Why? To upload the user recipe for the other users to access
     *
     * @param timeRN Required to name the document which will store this recipe
     * @param entry Required as it will be uploaded to firebase
     */
    private void uploadRecipeToFirebase(String timeRN, Map<String, Object> entry) {
        Task<Void> uploadTask = fbdb.collection(FireBaseEntities.RECIPES).document(timeRN).set(entry);

        uploadTask.addOnSuccessListener(unused -> {
            if (UploadRecipeSingleton.getInstance().getImageUri() == null) {
                Toast.makeText(this.getContext(), "Upload Successful", Toast.LENGTH_SHORT).show();
                FragmentManager fm =  this.getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.frame, new HomeFragment());
                ft.commit();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this.getContext(), "Upload NOT Successful", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Preparing the recipe map that will be uploaded
     *
     * Why? We require a map to be uploaded to represent the recipe
     *
     * @param userEmail To make sure that the recipe has an owner
     * @param imagePathForRecipe For the recipe to know which image is associated with them
     * @param entry Map that will be uploaded
     */
    private static void createMApForTheRecipe(String userEmail, String imagePathForRecipe, Map<String, Object> entry) {
        entry.put(FireBaseEntities.R_ID, "R_" + UUID.randomUUID().toString());
        entry.put(FireBaseEntities.R_NAME, UploadRecipeSingleton.getInstance().getRecipeName());
        entry.put(FireBaseEntities.R_DESC, UploadRecipeSingleton.getInstance().getRecipeDesc());
        entry.put(FireBaseEntities.IS_VEG, UploadRecipeSingleton.getInstance().isVegeterian());
        entry.put(FireBaseEntities.INGR, UploadRecipeSingleton.getInstance().getIngredients());
        entry.put(FireBaseEntities.IMAGE_URL, imagePathForRecipe);
        entry.put(FireBaseEntities.R_SERV_SIZ, String.valueOf(UploadRecipeSingleton.getInstance().getServingSize()));
        entry.put(FireBaseEntities.R_OWNER_MAIL, userEmail);
        entry.put(FireBaseEntities.REC_INSTS, UploadRecipeSingleton.getInstance().getInstructions());
        entry.put(FireBaseEntities.R_TIME_TAKEN, UploadRecipeSingleton.getInstance().getTimeTakenToPrepare() + " Minutes");
    }

    /**
     * Logic to upload an image of the recipe if the user has selected an image for their recipe
     *
     * Why? To store it in out Firebase storage which will bw accessed by the other users of the application
     * when coming across this recipe
     *
     * @param timeRN Will be used to name the recipe
     * @param imagePathForRecipe will be used as image name
     */
    private String uploadImageIfAny(String timeRN, String imagePathForRecipe) {
        everythingElse.setVisibility(View.GONE);
        loadingPB.setVisibility(View.VISIBLE);
        if (UploadRecipeSingleton.getInstance().getImageUri() != null) {
            ContentResolver cr =  getActivity().getContentResolver();
            MimeTypeMap mtm = MimeTypeMap.getSingleton();
            UploadRecipeSingleton currentRecipe = UploadRecipeSingleton.getInstance();
            String extension = mtm.getExtensionFromMimeType(cr.getType(currentRecipe.getImageUri()));

            imagePathForRecipe = timeRN + "." + extension;
            StorageReference fileReference =  storageReference.child(imagePathForRecipe);

            CompletableFuture<Void> uploadImageTask = CompletableFuture.runAsync(() -> {
                try {
                    UploadRecipeSingleton urs = UploadRecipeSingleton.getInstance();
                    fileReference.putFile(urs.getImageUri()).addOnSuccessListener(this::imageUploadSuccess).addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Image Upload Failed!", Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Asynchronous task completed.");
            });
            try {
                uploadImageTask.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        everythingElse.setVisibility(View.VISIBLE);
        loadingPB.setVisibility(View.GONE);
        return imagePathForRecipe;
    }

    /**
     * Image upload success handler
     *
     * Why? To distribute the code
     *
     * @param taskSnapshot Required to get its metadata
     */
    private void imageUploadSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        String downloadString = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
        FirebaseUpload firebaseUpload = new FirebaseUpload(titleET.getText().toString(), downloadString);
        String uploadID = dbReference.push().getKey();
        assert uploadID != null;
        dbReference.child(uploadID).setValue(firebaseUpload);
        Toast.makeText(this.getContext(), "Upload Successful", Toast.LENGTH_SHORT).show();
        FragmentManager fm =  this.getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frame, new HomeFragment());
        ft.commit();
    }

    /**
     * Decrement the time taken by the recipe
     *
     * Why? User interaction with the respective button need to be acknowledged
     */
    private void decrementTimeTaken() {
        String countStr = servingSizeTV_for_time.getText().toString();
        int count = Integer.parseInt(countStr);
        if (count != 0) {
            count = count - TIME_INCR;
        }
        servingSizeTV_for_time.setText(String.valueOf(count));
        UploadRecipeSingleton.getInstance().setTimeTakenToPrepare(count);
    }

    /**
     * Increment the time taken by the recipe
     *
     * Why? User interaction with the respective button need to be acknowledged
     */
    private void incrementTimeTaken() {
        String countStr = servingSizeTV_for_time.getText().toString();
        int count = Integer.parseInt(countStr);
        count = count + TIME_INCR;
        servingSizeTV_for_time.setText(String.valueOf(count));
        UploadRecipeSingleton.getInstance().setTimeTakenToPrepare(count);
    }

    /**
     * Decrement the serving size of the recipe
     *
     * Why? User interaction with the respective button need to be acknowledged
     */
    private void decrementServingSize() {
        String countStr = servingSizeTV.getText().toString();
        int count = Integer.parseInt(countStr);
        if (count != 0) {
            count--;
        }
        servingSizeTV.setText(String.valueOf(count));
        UploadRecipeSingleton.getInstance().setServingSize(count);
    }

    /**
     * Increment the serving size of the recipe
     *
     * Why? User interaction with the respective button need to be acknowledged
     */
    private void incrementServingSize() {
        String countStr = servingSizeTV.getText().toString();
        int count = Integer.parseInt(countStr);
        count++;
        servingSizeTV.setText(String.valueOf(count));
        UploadRecipeSingleton.getInstance().setServingSize(count);
    }

    /**
     * Check whether any of the required fields are not kept empty
     *
     * Why? If not done, will lead to recipes with insufficient information which will the standard of the
     * recipes down for the application
     */
    // WE NEED TO KEEP THIS COMPLEX CONDITIONAL METHOD AND LONG STATEMENT
    // AS WE NEED TO CHECK ALL THESE PARAMETERS IN THIS METHOD
    private boolean isEverythingEntered() {
        if (UploadRecipeSingleton.getInstance().getRecipeName() != null &&
                UploadRecipeSingleton.getInstance().getRecipeDesc() != null &&
                UploadRecipeSingleton.getInstance().getIngredients() != null &&
                UploadRecipeSingleton.getInstance().getServingSize() != 0 &&
                UploadRecipeSingleton.getInstance().getTimeTakenToPrepare() != 0 &&
                !UploadRecipeSingleton.getInstance().getInstructions().isEmpty()
        ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if the name is duplicated or not
     *
     * Why? Client requirement, the clients do not want the recipe names to be duplicated
     */
    private boolean isNameDuplicated(String nameEntered) {
        ArrayList<Recipe> existingRecipes = SharedValues.getInstance().getExistingRecipies();
        for (Recipe recipe : existingRecipes) {
            if (recipe.getRecipeName().contentEquals(nameEntered)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logic to open the file explorer for the user to select the image for their recipe
     *
     * Why? To provide an option for the user to upload an image of their recipe
     */
    private void openFileExplorer() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    /**
     * Logic to handle the selected image by the user for their recipe
     *
     * Why? We get the details of the image selected in the onActivityResult method
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean codeStatus = requestCode == 1 && resultCode == -1;
        boolean dataStatus = data != null && data.getData() != null;
        if (codeStatus && dataStatus) {
            UploadRecipeSingleton.getInstance().setImageUri(data.getData());
            Picasso.get().load(data.getData()).into(selectedImage);
            selectedImage.setVisibility(View.VISIBLE);
        }
    }
}