package com.groupeleven.mealmate;

public interface FireBaseEntities {
    String USERS = "Users";
    String EMAIL_ID = "email";
    String PASS = "password";
    String NAME = "name";

    ////////////////// Recipe Cloud Variables //////////////////
    String RECIPES = "Recipes";
    String IMAGE_URL = "imageUrl";
    String REC_INSTS = "instructions";
    String INGR = "ingredients";
    String INGR_NAME = "name";
    String INGR_QUAN = "quantity";
    String IS_VEG = "isVegetarian";
    String R_DESC = "recipeDescription";
    String R_NAME = "recipeName";
    String R_ID = "recipeId";
    String R_SERV_SIZ = "servingSize";
    String R_OWNER_MAIL = "userEmail";
    String R_TIME_TAKEN = "time";

    ////////////////////////////////// SHARE RECIPE //////////////////////////
    String SHR_RECI_COL = "SharedRecipes";
    String SHR_REC_DOC = "SharedRecipes";

    ////////////////////////////////// MEAL PLAN //////////////////////////
    String MEAL_PLAN_COLLECTION_NAME = "MealPlans";
    String MEAL_PLAN_ID = "mealPlanId";
    String MEAL_PLAN_USER_EMAIL = "userEmail";
    String MEAL_DATE = "mealDate";
}
