package com.example.pallab;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class RemoveMember extends AppCompatActivity {

    private DBHelper dbHelper;
    private Button removememback;
    private Spinner tableSpinner;
    private Button removeButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_remove_member);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        removememback = findViewById(R.id.removememback);
        tableSpinner = findViewById(R.id.spinnerTables);
        removeButton = findViewById(R.id.buttonRemove);

        dbHelper = new DBHelper(this);

        // Open the database in writable mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Fetch all table names (i.e., member tables)
        List<String> tableNames = dbHelper.getAllMemberTableNames(db);

        // Add a default hint as the first item
        tableNames.add(0, "Select Name");

        // Close the database
        db.close();



        // Populate the spinner with table names
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tableNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tableSpinner.setAdapter(adapter);

        // Set the default selection to the hint (position 0)
        tableSpinner.setSelection(0);

        // Set up the "Remove" button click listener
        removeButton.setOnClickListener(v -> {
            // Get the selected table name
            String selectedTable = tableSpinner.getSelectedItem().toString();

            // Check if a valid selection has been made
            if (selectedTable == null || selectedTable.isEmpty() || selectedTable.equals("Select Name")) {
                Toast.makeText(RemoveMember.this, "Select a member to remove", Toast.LENGTH_SHORT).show();
                return;
            }

            // Open the database in writable mode for deletion
            SQLiteDatabase dbForDeletion = dbHelper.getWritableDatabase();

            // Delete the selected table
            dbHelper.deleteMemberTable(dbForDeletion, selectedTable);

            // Close the database after deletion
            dbForDeletion.close();


            // Show a toast confirming the deletion
            Toast.makeText(RemoveMember.this, "Member '" + selectedTable + "' has been removed", Toast.LENGTH_SHORT).show();

            // Optionally, update the spinner or perform other UI updates (reload table list after removal)
            // Re-fetch the table names and update the spinner if necessary
        });

        // Set up the "Back" button click listener
        removememback.setOnClickListener(v -> {
            Intent intent = new Intent(RemoveMember.this, MainActivity.class);
            startActivity(intent);
        });
    }
}
