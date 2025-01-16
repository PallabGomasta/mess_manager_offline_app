package com.example.pallab;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class AddMember extends AppCompatActivity {
    private Button addmemback;
    Button addButton;
    TextInputEditText entername;
    DBHelper DB;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_member);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        entername = findViewById(R.id.addentername);
        addButton = findViewById(R.id.addmembutton);
        addmemback = findViewById(R.id.addmemback);
        DB = new DBHelper(this);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the user input for the table name
                String tableName = entername.getText().toString().trim();

                if (tableName.isEmpty()) {
                    // Show an error if the input is empty
                    Toast.makeText(AddMember.this, "Please add a member name", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a new database helper instance
                SQLiteDatabase db = DB.getWritableDatabase();

                // Check if the table already exists
                if (isTableExists(db, tableName)) {
                    Toast.makeText(AddMember.this, "Member already exists", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create the table dynamically for the new member
                DB.createUserTable(db, tableName);

                // Show a message confirming the table creation
                Toast.makeText(AddMember.this, "New member added: " + tableName, Toast.LENGTH_SHORT).show();

                // Close the database
                db.close();
            }
        });

        addmemback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the main activity
                Intent intent = new Intent(AddMember.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    // Method to check if a table already exists
    private boolean isTableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}
