package com.groupeleven.mealmate.AccountManagement;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavoriteRecipeDBHelper extends SQLiteOpenHelper {

    private static final String dbName = "FavoriteRecipe.db";
    private static final int DATABASE_VERSION = 1;
    private static final String tableName = "FavoriteRecipe";

    private static final String colId = "id";
    private static final String colName = "name";

    private static final String CREATE_TABLE = "CREATE TABLE " +  tableName + " (" +
            colId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            colName + " TEXT );";

    public FavoriteRecipeDBHelper(Context context) {
        super(context, dbName, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(db);
    }
}
