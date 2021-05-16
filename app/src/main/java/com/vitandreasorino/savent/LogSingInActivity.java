package com.vitandreasorino.savent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LogSingInActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logsignin);
    }


    public void onLoginClick(View view) {
        
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }



}