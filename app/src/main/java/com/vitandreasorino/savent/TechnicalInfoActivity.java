package com.vitandreasorino.savent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class TechnicalInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technical_info);
    }

    /**
     * Metodo che serve per tornare alla schermata precedente
     * @param view : la vista della pagina precedente
     */
    public void onBackButtonPressed(View view) {
        super.onBackPressed();
        finish();
    }

}