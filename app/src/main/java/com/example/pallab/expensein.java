package com.example.pallab;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;
import java.util.List;

public class expensein extends AppCompatActivity {

    private Button expenseback;
    private Spinner spinnerMember;
    private EditText editTextExpense;
    private Button buttonSubmitExpense;
    private TextView textViewDate;  // TextView for the date

    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private String selectedDate = "";  // Variable to store selected date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expensein);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        expenseback = findViewById(R.id.expenseback);
        expenseback.setOnClickListener(v -> {
            Intent intent = new Intent(expensein.this, MainActivity.class);
            startActivity(intent);
        });

        // Initialize UI elements
        spinnerMember = findViewById(R.id.spinnerTables);
        editTextExpense = findViewById(R.id.editTextExpense);
        buttonSubmitExpense = findViewById(R.id.buttonSubmitExpense);
        textViewDate = findViewById(R.id.textViewDate);  // Initialize the TextView

        // Initialize database
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        // Populate the spinner with member (table) names
        List<String> tableNames = dbHelper.getAllMemberTableNames(db);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tableNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMember.setAdapter(adapter);

        // Add a default hint as the first item
        tableNames.add(0, "Select Name");

        // Set listener for Date selection
        textViewDate.setOnClickListener(v -> {
            // Get the current date
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Show DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(expensein.this, (view, year1, month1, dayOfMonth) -> {
                // Format the date as DD/MM/YYYY and update the TextView
                selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                textViewDate.setText(selectedDate);
            }, year, month, day);
            datePickerDialog.show();
        });

        // Set listener for Submit Expense button
        buttonSubmitExpense.setOnClickListener(v -> {
            // Get selected member (table name)
            String selectedMember = spinnerMember.getSelectedItem().toString();

            // Get expense input
            String expenseInput = editTextExpense.getText().toString().trim();

            if (expenseInput.isEmpty()) {
                Toast.makeText(this, "Please enter an expense amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            int expenseAmount = Integer.parseInt(expenseInput);

            // Call the updateExpense method
            updateExpense(selectedMember, expenseAmount);

            // Clear the input field
            editTextExpense.setText("");

            Toast.makeText(this, "Expense updated!", Toast.LENGTH_SHORT).show();
        });
    }

    // Add this method to handle the expense update
    private void updateExpense(String tableName, int expenseAmount) {
        // Insert the expense amount and selected date into the table
        ContentValues values = new ContentValues();
        values.put(tableName + "_EXPENSE", expenseAmount);
        values.put(tableName + "_DATE", selectedDate);  // Store the date
        db.insert(tableName, null, values);

        // Query to calculate the sum of all expenses
        String query = "SELECT SUM(" + tableName + "_EXPENSE) AS totalExpense FROM " + tableName;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            // Safely retrieve the column index of "totalExpense"
            int totalExpenseIndex = cursor.getColumnIndexOrThrow("totalExpense");

            // Get the total sum of expenses
            int totalExpense = cursor.getInt(totalExpenseIndex);

            // Update the _TOTAL_EXPENSE field in the first row (id = 1)
            ContentValues totalValues = new ContentValues();
            totalValues.put(tableName + "_TOTAL_EXPENSE", totalExpense);
            db.update(tableName, totalValues, "id = ?", new String[]{"1"});

            cursor.close();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }
}
