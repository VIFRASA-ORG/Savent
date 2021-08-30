package com.vitandreasorino.savent.Utenti.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.vitandreasorino.savent.LogSingInActivity;
import com.vitandreasorino.savent.R;

import Helper.AuthHelper;
import Helper.LocalStorage.SharedPreferencesHelper;
import Services.BluetoothLEServices.GattServerCrawlerService;
import Services.BluetoothLEServices.GattServerService;

public class SettingsActivity extends AppCompatActivity{


    Switch switchBluetooth;
    Switch switchProximitySensor;
    Button buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        buttonLogout = (Button) findViewById(R.id.buttonLogout);
        switchBluetooth = (Switch) findViewById(R.id.switchBluetooth);
        switchProximitySensor = (Switch) findViewById(R.id.switchSensor);

        buttonLogout.setOnClickListener(v -> {
            AlertDialog.Builder alertLogout = new AlertDialog.Builder(SettingsActivity.this);
            alertLogout.setTitle(R.string.stringConfirmLogout);
            alertLogout.setMessage(R.string.stringDialogLogout);

            // Nel caso di risposta positiva nel dialog
            alertLogout.setPositiveButton(R.string.confirmPositive, (dialog, which) -> {
                //Effettua il logout e comunica che l'operazione è andata a buon fine
                AuthHelper.logOut(getApplicationContext());
                //Gestire le shared preference nello shared preferences helper e fare il reset nell'auth helper
                //Richiama qui il metodo per la gestione delle preferenze dopo la sua creazione

                Toast.makeText(SettingsActivity.this, R.string.stringLogoutDone, Toast.LENGTH_SHORT).show();
                //Ritorna alla schermata iniziale
                Intent intentLogin = new Intent(SettingsActivity.this, LogSingInActivity.class);
                intentLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);    //Removing from the task all the previous Activity.
                startActivity(intentLogin);
            });

            // Nel caso di risposta negativa nel dialog
            alertLogout.setNegativeButton(R.string.confirmNegative, null);
            //mostra il dialog
            alertLogout.show();

        });

        //Imposta come valore di default delle due shared preferences "true"
        switchBluetooth.setChecked(SharedPreferencesHelper.getBluetoothPreference(getApplicationContext()));
        switchProximitySensor.setChecked(SharedPreferencesHelper.getProximitySensorPreference(getApplicationContext()));

        switchBluetooth.setOnClickListener(v -> {
            if (switchBluetooth.isChecked()) {
               SharedPreferencesHelper.setBluetoothPreference(true, getApplicationContext());
                if(GattServerService.isServiceRunning){
                    //Inviare broadcast
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(GattServerService.RESTART_GATT_SERVER_INTENT));
                }else{
                    startService(new Intent(getBaseContext(), GattServerService.class));
                }

                if(GattServerCrawlerService.isServiceRunning){
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(GattServerCrawlerService.RESTART_GATT_CRAWLER_INTENT));
                }else{
                    startService(new Intent(getBaseContext(), GattServerCrawlerService.class));
                }
               switchBluetooth.setChecked(true);
            } else {
                SharedPreferencesHelper.setBluetoothPreference(false, getApplicationContext());
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(GattServerService.STOP_GATT_SERVER_INTENT));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(GattServerCrawlerService.STOP_GATT_CRAWLER_INTENT));
                switchBluetooth.setChecked(false);
            }
        });

        switchProximitySensor.setOnClickListener(v -> {
            if (switchProximitySensor.isChecked()) {
                SharedPreferencesHelper.setProximitySensorPreference(true, getApplicationContext());
                switchProximitySensor.setChecked(true);
            } else {
                SharedPreferencesHelper.setProximitySensorPreference(false, getApplicationContext());
                switchProximitySensor.setChecked(false);
            }
        });
    }

    public void comunicationSwabOnClick(View view) {
        Intent i = new Intent(this, CommunicationSwabActivity.class);
        startActivity(i);
    }

    public void onBackButtonPressed(View view) {
        super.onBackPressed();
        finish();
    }
}