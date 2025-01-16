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

public class MealCount extends AppCompatActivity {
    private Button mealcountback;
    private Spinner spinnerMember;
    private EditText editTextMeal;
    private Button buttonSubmitMeal;
    private TextView textViewDate;

    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mealcount);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mealcountback = findViewById(R.id.mealcountback);
        mealcountback.setOnClickListener(v -> {
            Intent intent = new Intent(MealCount.this, MainActivity.class);
            startActivity(intent);
        });

        spinnerMember = findViewById(R.id.spinnerTables);
        editTextMeal = findViewById(R.id.editTextMeal);
        buttonSubmitMeal = findViewById(R.id.buttonSubmitMeal);
        textViewDate = findViewById(R.id.textViewDate);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        List<String> tableNames = dbHelper.getAllMemberTableNames(db);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tableNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMember.setAdapter(adapter);

        tableNames.add(0, "Select Name");

        textViewDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(MealCount.this, (view, selectedYear, selectedMonth, selectedDay) -> {
                selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                textViewDate.setText(selectedDate);
            }, year, month, day);

            datePickerDialog.show();
        });

        buttonSubmitMeal.setOnClickListener(v -> {
            String selectedMember = spinnerMember.getSelectedItem().toString();
            String mealInput = editTextMeal.getText().toString().trim();

            if (mealInput.isEmpty()) {
                Toast.makeText(this, "Please enter a meal count", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            double mealCount = Double.parseDouble(mealInput);
            updateMealCount(selectedMember, mealCount);

            editTextMeal.setText("");
            Toast.makeText(this, "Meal count updated!", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateMealCount(String tableName, double mealCount) {
        ContentValues values = new ContentValues();
        values.put(tableName + "_MEAL", mealCount);
        values.put(tableName + "_DATE", selectedDate);
        db.insert(tableName, null, values);

        String query = "SELECT SUM(" + tableName + "_MEAL) AS totalMeal FROM " + tableName;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            double totalMeal = cursor.getDouble(cursor.getColumnIndexOrThrow("totalMeal"));

            ContentValues totalValues = new ContentValues();
            totalValues.put(tableName + "_TOTAL_MEAL", totalMeal);
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
