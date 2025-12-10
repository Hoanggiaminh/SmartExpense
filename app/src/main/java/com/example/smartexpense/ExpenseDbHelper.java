// java
package com.example.smartexpense;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ExpenseDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "expenses.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE = "expenses";
    public static final String COL_ID = "_id";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_DATE = "date";
    public static final String COL_CATEGORY = "category";
    public static final String COL_NOTE = "note";
    public static final String COL_TYPE = "type";

    public ExpenseDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_AMOUNT + " REAL, "
                + COL_DATE + " TEXT, "
                + COL_CATEGORY + " TEXT, "
                + COL_NOTE + " TEXT, "
                + COL_TYPE + " TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public long insertExpense(double amount, String date, String category, String note, String type) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_AMOUNT, amount);
        cv.put(COL_DATE, date);
        cv.put(COL_CATEGORY, category);
        cv.put(COL_NOTE, note);
        cv.put(COL_TYPE, type);
        long id = db.insert(TABLE, null, cv);
        db.close();
        return id;
    }
}
