package com.example.beerute_stepcounter02_java;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void navigateToStepCounterActivity(View v) {
        Intent mIntent = new Intent(this, StepsCounterActivity.class);
        startActivity(mIntent);
    }

    public void navigateToStepHistoryActivity(View v) {
        Intent mIntent = new Intent(this, StepsHistoryActivity.class);
        startActivity(mIntent);
    }

    public void navigateToCustomAlgoActivity(View v) {
        Intent mIntent = new Intent(this, CustomAlgoResultsActivity.class);
        startActivity(mIntent);

    }
}