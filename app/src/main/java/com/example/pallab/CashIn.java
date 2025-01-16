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

public class CashIn extends AppCompatActivity {
    private Button cashinback;
    private Spinner spinnerMember;
    private EditText editTextCash;
    private Button buttonSubmitCash;
    private TextView textViewDate; // TextView for date selection

    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private String selectedDate = ""; // Variable to store the selected date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cashin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cashinback = findViewById(R.id.cashinback);
        cashinback.setOnClickListener(v -> {
            Intent intent = new Intent(CashIn.this, MainActivity.class);
            startActivity(intent);
        });

        // Initialize UI elements
        spinnerMember = findViewById(R.id.spinnerTables);
        editTextCash = findViewById(R.id.editTextCash);
        buttonSubmitCash = findViewById(R.id.buttonSubmitCash);
        textViewDate = findViewById(R.id.textViewDate); // Initialize the TextView for date

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
            DatePickerDialog datePickerDialog = new DatePickerDialog(CashIn.this, (view, selectedYear, selectedMonth, selectedDay) -> {
                // Format the date as DD/MM/YYYY
                selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                textViewDate.setText(selectedDate);
            }, year, month, day);

            // Show the dialog
            datePickerDialog.show();
        });

        // Set listener for Submit Cash button
        buttonSubmitCash.setOnClickListener(v -> {
            // Get selected member (table name)
            String selectedMember = spinnerMember.getSelectedItem().toString();

            // Get cash input
            String cashInput = editTextCash.getText().toString().trim();

            if (cashInput.isEmpty()) {
                Toast.makeText(this, "Please enter a cash amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            int cashAmount = Integer.parseInt(cashInput);

            // Call the updateCash method
            updateCash(selectedMember, cashAmount);

            // Clear the input field
            editTextCash.setText("");

            Toast.makeText(this, "Cash updated!", Toast.LENGTH_SHORT).show();
        });
    }

    // Add this method to handle the cash update
    private void updateCash(String tableName, int cashAmount) {
        // Insert the cash amount and selected date into the table
        ContentValues values = new ContentValues();
        values.put(tableName + "_CASH", cashAmount);
        values.put(tableName + "_DATE", selectedDate); // Store the date
        db.insert(tableName, null, values);

        // Query to calculate the sum of all cash values
        String query = "SELECT SUM(" + tableName + "_CASH) AS totalCash FROM " + tableName;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            // Safely retrieve the column index of "totalCash"
            int totalCashIndex = cursor.getColumnIndexOrThrow("totalCash");

            // Get the total sum of cash
            int totalCash = cursor.getInt(totalCashIndex);

            // Update the _TOTAL_CASH field in the first row (id = 1)
            ContentValues totalValues = new ContentValues();
            totalValues.put(tableName + "_TOTAL_CASH", totalCash);
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
