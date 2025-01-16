package com.example.pallab;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



public class MainActivity extends AppCompatActivity {

private ImageButton mealcountbutton;
private ImageButton cashin ;
private ImageButton expensein;
private ImageButton addmembut;
private ImageButton removemembut;
private ImageButton sheetbutton;
private ImageButton infoButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find the ImageButton by its ID
        sheetbutton = findViewById(R.id.btn_open_sheet);
        sheetbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SheetActivity.class);
                startActivity(intent);
            }
        });

        mealcountbutton = findViewById(R.id.mealcountbutton);
        mealcountbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MealCount.class);
                startActivity(intent);
            }
        });

        cashin = findViewById(R.id.cashin);
        cashin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CashIn.class);
                startActivity(intent);
            }
        });

        expensein = findViewById(R.id.expensein);
        expensein.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, expensein.class);
                startActivity(intent);
            }
        });

        addmembut = findViewById(R.id.addmembut);
        addmembut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddMember.class);
                startActivity(intent);
            }
        });
        removemembut = findViewById(R.id.removemembut);
        removemembut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RemoveMember.class);
                startActivity(intent);
            }
        });
        infoButton = findViewById(R.id.info_button);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoDialog();
            }
        });
    }
        private void showInfoDialog() {
            // Create dialog
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_info);
            dialog.setCancelable(true);



            // Show the dialog
            dialog.show();
        }
    }

