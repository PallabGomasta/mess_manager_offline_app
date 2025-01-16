package com.example.pallab;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class SheetActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private TableLayout sheetTableLayout;
    private TableLayout individualtable;
    private TableLayout grandTotalTableLayout;
    private Spinner spinnerName;
    private Button showButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet);

        dbHelper = new DBHelper(this);
        sheetTableLayout = findViewById(R.id.sheetTableLayout);
        grandTotalTableLayout = findViewById(R.id.grandTotalTableLayout);
        individualtable = findViewById(R.id.individualtable);
        spinnerName = findViewById(R.id.spinnerName);
        showButton = findViewById(R.id.showButton);

        displaySheetTable();
        displayGrandTotalTable();
        // Populate Spinner with table names from the database
        populateSpinner();
        // Set up the Show Button to display the selected table's data
        showButton.setOnClickListener(v -> {
            String selectedTable = (String) spinnerName.getSelectedItem();
            if (selectedTable != null) {
                displayTableData(selectedTable);
            }
        });
    }

    private void displaySheetTable() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> memberTables = dbHelper.getAllMemberTableNames(db);

        double mealRate = calculateMealRate(db, memberTables);

        // Add Header Row for Sheet Table
        TableRow headerRow = new TableRow(this);
        headerRow.addView(createBoldTextView("Member's Name "));
        headerRow.addView(createBoldTextView("Individual Meal"));
        headerRow.addView(createBoldTextView("Individual Cash"));
        headerRow.addView(createBoldTextView("Individual Expense"));
        headerRow.addView(createBoldTextView("Individual Cost"));
        headerRow.addView(createBoldTextView("Individual Balance"));

        sheetTableLayout.addView(headerRow);

        for (String tableName : memberTables) {
            Cursor cursor = db.rawQuery("SELECT SUM(" + tableName + "_TOTAL_MEAL) AS totalMeal, SUM(" + tableName + "_TOTAL_CASH) AS totalCash, SUM(" + tableName + "_TOTAL_EXPENSE) AS totalExpense FROM " + tableName, null);

            if (cursor != null && cursor.moveToFirst()) {
                double totalMeal = cursor.getDouble(cursor.getColumnIndexOrThrow("totalMeal"));
                double totalExpense = cursor.getDouble(cursor.getColumnIndexOrThrow("totalExpense"));
                double totalCash = cursor.getDouble(cursor.getColumnIndexOrThrow("totalCash"));
                double individualCost = mealRate * totalMeal;
                double individualBalance = totalCash - individualCost;

                TableRow row = new TableRow(this);
                row.addView(createTextView(tableName));
                row.addView(createTextView(String.format("%.2f", totalMeal)));
                row.addView(createTextView(String.format("%.2f", totalCash)));
                row.addView(createTextView(String.format("%.2f", totalExpense)));
                row.addView(createTextView(String.format("%.2f", individualCost)));
                row.addView(createTextView(String.format("%.2f", individualBalance)));
                sheetTableLayout.addView(row);
            }
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private double calculateMealRate(SQLiteDatabase db, List<String> memberTables) {
        double totalMeals = 0;
        double totalExpenses = 0;
        double totalCash = 0;

        for (String tableName : memberTables) {
            Cursor cursor = db.rawQuery("SELECT SUM(" + tableName + "_TOTAL_MEAL) AS totalMeal, SUM(" + tableName + "_TOTAL_CASH) AS totalCash, SUM(" + tableName + "_TOTAL_EXPENSE) AS totalExpense FROM " + tableName, null);

            if (cursor != null && cursor.moveToFirst()) {
                totalMeals += cursor.getDouble(cursor.getColumnIndexOrThrow("totalMeal"));
                totalCash += cursor.getDouble(cursor.getColumnIndexOrThrow("totalCash"));
                totalExpenses += cursor.getDouble(cursor.getColumnIndexOrThrow("totalExpense"));
            }
            if (cursor != null) {
                cursor.close();
            }
        }

        return totalMeals > 0 ? totalExpenses / totalMeals : 0;
    }

    private void displayGrandTotalTable() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> memberTables = dbHelper.getAllMemberTableNames(db);

        double grandTotalMeals = 0;
        double grandTotalCash = 0;
        double grandTotalExpenses = 0;
        double mealRate = calculateMealRate(db, memberTables);

        // Add Header Row for Grand Total Table
        TableRow headerRow = new TableRow(this);
        headerRow.addView(createBoldTextView("Meal Rate"));
        headerRow.addView(createBoldTextView("Grand Total Meals"));
        headerRow.addView(createBoldTextView("Grand Total Cash"));
        headerRow.addView(createBoldTextView("Grand Total Expense"));

        grandTotalTableLayout.addView(headerRow);

        for (String tableName : memberTables) {
            Cursor cursor = db.rawQuery("SELECT SUM(" + tableName + "_TOTAL_MEAL) AS totalMeal, SUM(" + tableName + "_TOTAL_CASH) AS totalCash, SUM(" + tableName + "_TOTAL_EXPENSE) AS totalExpense FROM " + tableName, null);

            if (cursor != null && cursor.moveToFirst()) {
                grandTotalMeals += cursor.getDouble(cursor.getColumnIndexOrThrow("totalMeal"));
                grandTotalCash += cursor.getDouble(cursor.getColumnIndexOrThrow("totalCash"));
                grandTotalExpenses += cursor.getDouble(cursor.getColumnIndexOrThrow("totalExpense"));
            }
            if (cursor != null) {
                cursor.close();
            }
        }

        TableRow row = new TableRow(this);
        row.addView(createTextView(String.format("%.2f", mealRate)));
        row.addView(createTextView(String.format("%.2f", grandTotalMeals)));
        row.addView(createTextView(String.format("%.2f", grandTotalCash)));
        row.addView(createTextView(String.format("%.2f", grandTotalExpenses)));

        grandTotalTableLayout.addView(row);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        return textView;
    }

    private TextView createBoldTextView(String text) {
        TextView textView = createTextView(text);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);
        return textView;
    }

    private void populateSpinner() {
        // Fetch all table names from the database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> memberTables = dbHelper.getAllMemberTableNames(db);

        // Create and set an ArrayAdapter for the Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, memberTables);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerName.setAdapter(adapter);
    }

    private void displayTableData(String tableName) {
        individualtable.removeAllViews();  // Clear previous data

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);

        // Check if the cursor is valid and move to the first record
        if (cursor != null && cursor.moveToFirst()) {
            // Add a header row to the table layout
            TableRow headerRow = new TableRow(this);
            headerRow.addView(createBoldTextView(tableName));
            headerRow.addView(createBoldTextView("DATE"));
            headerRow.addView(createBoldTextView("MEAL"));
            headerRow.addView(createBoldTextView("CASH"));
            headerRow.addView(createBoldTextView("EXPENSE"));

            individualtable.addView(headerRow);

            // Loop through the data and add rows to the table
            do {
                TableRow row = new TableRow(this);

                // Handle each column index carefully to avoid -1
                int idIndex = cursor.getColumnIndex("id");
                int dateIndex = cursor.getColumnIndex(tableName + "_DATE");
                int mealIndex = cursor.getColumnIndex(tableName + "_MEAL");
                int cashIndex = cursor.getColumnIndex(tableName + "_CASH");
                int expenseIndex = cursor.getColumnIndex(tableName + "_EXPENSE");

                String id = idIndex != -1 ? String.valueOf(cursor.getInt(idIndex)) : "N/A";
                String date = dateIndex != -1 ? cursor.getString(dateIndex) : "N/A";
                String meal = mealIndex != -1 ? String.format("%.2f", cursor.getDouble(mealIndex)) : "N/A";
                String cash = cashIndex != -1 ? String.format("%.2f", cursor.getDouble(cashIndex)) : "N/A";
                String expense = expenseIndex != -1 ? String.format("%.2f", cursor.getDouble(expenseIndex)) : "N/A";

                row.addView(createTextView(id));
                row.addView(createTextView(date));
                row.addView(createTextView(meal));
                row.addView(createTextView(cash));
                row.addView(createTextView(expense));

                // Set a long-click listener on the row for deletion
                row.setOnLongClickListener(v -> {
                    showDeleteConfirmationDialog(tableName, id);
                    return true;
                });

                individualtable.addView(row);
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            Toast.makeText(this, "No data found for the selected table", Toast.LENGTH_SHORT).show();
        }
    }
private void showDeleteConfirmationDialog(String tableName, String id) {
    new android.app.AlertDialog.Builder(this)
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete this entry?")
            .setPositiveButton("Yes", (dialog, which) -> {
                deleteTableRow(tableName, id);
                Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("No", null)
            .show();
}
private void deleteTableRow(String tableName, String id) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    int rowsAffected = db.delete(tableName, "id = ?", new String[]{id});

    if (rowsAffected > 0) {
        // Refresh the table layout after deletion
        displayTableData(tableName);
    } else {
        Toast.makeText(this, "Failed to delete entry", Toast.LENGTH_SHORT).show();
    }
}

}
