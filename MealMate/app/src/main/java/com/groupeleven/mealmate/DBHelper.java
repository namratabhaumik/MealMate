package com.groupeleven.mealmate;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
    private static final String dbName = "Ingredients.db";
    private static final int DATABASE_VERSION = 1;
    private static final String tableName = "ingredients";
    private static final String colId = "id";
    private static final String colName = "name";
    private static final String colQty = "quantity";
    private static final String colCategory = "category";

    private static final String imgUrl = "imgUrl";
    private static final String colUnit = "unit";




    // Create the table
    private static final String CREATE_TABLE = "CREATE TABLE " +  tableName + " (" +
            colId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            colName + " TEXT," + colQty + " INTEGER," + imgUrl + " TEXT, " +    colCategory + " TEXT," + colUnit + " TEXT );";

    public DBHelper(Context context) {
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
