package com.vitandreasorino.savent.Utenti.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

            //Nel caso di risposta positiva nel dialog
            alertLogout.setPositiveButton(R.string.confirmPositive, (dialog, which) -> {
                //Effettua il logout e comunica che l'operazione è andata a buon fine
                AuthHelper.logOut(getApplicationContext());
                //Si gestiscono le shared preference nello shared preferences helper e si effettua il reset nell'auth helper
                //Viene poi richiamato qui il metodo per la gestione delle preferenze dopo la sua creazione

                Toast.makeText(SettingsActivity.this, R.string.stringLogoutDone, Toast.LENGTH_SHORT).show();
                //Si ritorna alla schermata iniziale
                Intent intentLogin = new Intent(SettingsActivity.this, LogSingInActivity.class);
                //Si rimuovono dall'attività tutte le attività precedenti.
                intentLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentLogin);
            });

            //Nel caso di risposta negativa nel dialog
            alertLogout.setNegativeButton(R.string.confirmNegative, null);
            //mostra il dialog
            alertLogout.show();

        });

        //Imposta come valore di default delle due shared preferences "true"
        switchBluetooth.setChecked(SharedPreferencesHelper.getBluetoothPreference(getApplicationContext()));
        switchProximitySensor.setChecked(SharedPreferencesHelper.getProximitySensorPreference(getApplicationContext()));

        switchBluetooth.setOnClickListener(v -> {
            if (switchBluetooth.isChecked()) {
                //Viene impostato a true il valore del bluetooth LE
               SharedPreferencesHelper.setBluetoothPreference(true, getApplicationContext());
                if(GattServerService.isRunning){
                    //Si invia il broadcast
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(GattServerService.RESTART_GATT_SERVER));
                }else{
                    //altrimenti si esegue il Gatt service
                    startService(new Intent(getBaseContext(), GattServerService.class));
                }
                //se la ricerca di dispositivi compatibili con BLE sta avvenendo
                if(GattServerCrawlerService.isRunning){
                    //si cerca di effettuare la connessione con tali dispositivi
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(GattServerCrawlerService.RESTART_GATT_CRAWLER));
                }else{
                    //altrimenti si cerca di rieseguire il Gatt service
                    startService(new Intent(getBaseContext(), GattServerCrawlerService.class));
                }
                //si setta il BLE come attivo
               switchBluetooth.setChecked(true);
            } else {
                //altrimenti si fa terminare il Gatt server service, se la ricerca non ha dato dispositivi compatibili e il BLE viene disattivato
                SharedPreferencesHelper.setBluetoothPreference(false, getApplicationContext());
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(GattServerService.STOP_GATT_SERVER));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(GattServerCrawlerService.STOP_GATT_CRAWLER));
                switchBluetooth.setChecked(false);
            }
        });

        //Si setta lo switch del sensore di prossimità, sulla base della preferenza messa dall'utente
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

    /**
     * Metodo che invoca l'intent per creare la schermata dedicata alla comunicazione dei tamponi
     * @param view: la nuova vista da richiamare
     */
    public void comunicationSwabOnClick(View view) {
        Intent i = new Intent(this, CommunicationSwabActivity.class);
        startActivity(i);
    }

    /**
     * Metodo che fa ritornare alla schermata precedente
     * @param view
     */
    public void onBackButtonPressed(View view) {
        super.onBackPressed();
        finish();
    }
}