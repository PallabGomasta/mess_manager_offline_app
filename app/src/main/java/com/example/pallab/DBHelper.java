package com.example.pallab;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    // Constructor
    public DBHelper(Context context) {
        super(context, "Messdata.db", null, 1);
    }

    // Method to create a table for each member
    public void createUserTable(SQLiteDatabase db, String tableName) {
        String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                tableName + "_DATE TEXT, " +
                tableName + "_MEAL REAL, " +
                tableName + "_CASH INTEGER, " +
                tableName + "_EXPENSE INTEGER, " +
                tableName + "_TOTAL_MEAL REAL, " +
                tableName + "_TOTAL_CASH INTEGER, " +
                tableName + "_TOTAL_EXPENSE INTEGER)";
        db.execSQL(CREATE_TABLE_SQL);
    }

    // Method to get all member (table) names from the database
    public List<String> getAllMemberTableNames(SQLiteDatabase db) {
        List<String> tableNames = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name != 'android_metadata'", null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                tableNames.add(cursor.getString(0));
            }
            cursor.close();
        }

        return tableNames;
    }

    // Method to delete a member's table from the database
    public void deleteMemberTable(SQLiteDatabase db, String tableName) {
        if (tableName != null && !tableName.equals("Select Name")) {
            String query = "DROP TABLE IF EXISTS " + tableName;
            db.execSQL(query);
        }
    }

    // Method to calculate Grand Totals across all user tables
    public double[] calculateGrandTotals(SQLiteDatabase db) {
        double grandTotalMeal = 0.0;
        double grandTotalCash = 0.0;
        double grandTotalExpense = 0.0;

        List<String> tableNames = getAllMemberTableNames(db);

        for (String tableName : tableNames) {
            if (!tableName.equals("android_metadata") && !tableName.equals("sqlite_sequence")) {
                String query = "SELECT " +
                        "IFNULL(SUM(" + tableName + "_TOTAL_MEAL), 0) AS totalMeal, " +
                        "IFNULL(SUM(" + tableName + "_TOTAL_CASH), 0) AS totalCash, " +
                        "IFNULL(SUM(" + tableName + "_TOTAL_EXPENSE), 0) AS totalExpense " +
                        "FROM " + tableName;

                Cursor cursor = db.rawQuery(query, null);

                if (cursor != null && cursor.moveToFirst()) {
                    grandTotalMeal += cursor.getDouble(cursor.getColumnIndexOrThrow("totalMeal"));
                    grandTotalCash += cursor.getDouble(cursor.getColumnIndexOrThrow("totalCash"));
                    grandTotalExpense += cursor.getDouble(cursor.getColumnIndexOrThrow("totalExpense"));
                }

                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return new double[]{grandTotalMeal, grandTotalCash, grandTotalExpense};
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tables will be created as needed
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade logic if needed
    }
}
