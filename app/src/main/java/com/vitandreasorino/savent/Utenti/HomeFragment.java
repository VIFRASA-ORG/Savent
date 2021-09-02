package com.vitandreasorino.savent.Utenti;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.vitandreasorino.savent.R;
import com.vitandreasorino.savent.TechnicalInfoActivity;
import Helper.AnimationHelper;
import Helper.BluetoothLEHelper;


public class HomeFragment extends Fragment {

    private static final int ENABLE_BT_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;


    TextView textTechnicalInfo;

    //Definizione di tutti i componenti per i permessi e per il box relativo al bluetooth
    Button enableBluetoothButton;
    Button givePermissionButton;
    Button enableGeolocationButton;
    LinearLayout errorsBox;
    LinearLayout bluetoothDisabledBox;
    LinearLayout locationPermissionNotGivenBox;
    LinearLayout geolocationDisabledBox;
    View errorsBoxSpace;

    boolean isBluetoothEnabled = false;
    boolean isLocationGranted = false;
    boolean isGpsEnabled = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textTechnicalInfo=view.findViewById(R.id.stringTechnicalInfo);
        enableBluetoothButton = view.findViewById((R.id.enableBluetoothButton));
        givePermissionButton = view.findViewById(R.id.givePermissionButton);
        enableGeolocationButton = view.findViewById(R.id.enableGeolocationButton);

        errorsBox = view.findViewById(R.id.errorsBox);
        bluetoothDisabledBox = view.findViewById(R.id.bluetoothDisabledBox);
        locationPermissionNotGivenBox = view.findViewById(R.id.locationPermissionNotGivenBox);
        errorsBoxSpace = view.findViewById(R.id.errorsBoxSpace);
        geolocationDisabledBox = view.findViewById(R.id.geolocationDisabledBox);

        //Impostazione degli onClickListener
        textTechnicalInfo.setOnClickListener(v -> onClickTechnicalInfo());
        enableBluetoothButton.setOnClickListener( v -> onEnableBluetoothClick());
        givePermissionButton.setOnClickListener(v -> onGivePermissionButtonClick());
        enableGeolocationButton.setOnClickListener(v -> onEnableGeolocationClick());

        //Ottenimento degli stati del bluetooth e dei permessi
        isBluetoothEnabled = BluetoothLEHelper.isBluetoothEnabled();
        isLocationGranted = BluetoothLEHelper.isFineLocationGranted(getContext());
        isGpsEnabled = BluetoothLEHelper.isGpsEnabled(getContext());

        //Visione dei box degli errori
        setUpErrorsBox(!isBluetoothEnabled, !isLocationGranted, !isGpsEnabled,0);

        //Impostazione di un filtro per ricevere solo gli eventi di modifica dello stato del bluetooth.
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        getActivity().registerReceiver(bluetoothStateReceiver, filter);

        //Aggiunta di un ricevitore per l'evento di modifica dello stato del GPS
        IntentFilter filter1 = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        filter1.addAction(Intent.ACTION_PROVIDER_CHANGED);
        getActivity().registerReceiver(geolocationStateChangeReceiver, filter1);
    }

    private final BroadcastReceiver geolocationStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isGpsEnabled = BluetoothLEHelper.isGpsEnabled(getContext());
            setUpErrorsBox(!isBluetoothEnabled, !isLocationGranted, !isGpsEnabled,0);
        }
    };

    /**
     * Si imposta il ricevitore di trasmissione per cambiare la scatola bluetooth
     * quando lo stato del bluetooth viene aggiornato dal sistema.
     */
    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (bluetoothState) {
                    case BluetoothAdapter.STATE_ON:
                        //Nel caso in cui il bluethooth è attivo
                        isBluetoothEnabled = true;
                        //Si rimuove la casella per la richiesta di abilitazione del bluetooth
                        setUpErrorsBox(false,!isLocationGranted, !isGpsEnabled,0);
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        isBluetoothEnabled = false;
                        //se il bluetooth non è attivo
                        // si lascia visibile la casella per la richiesta di abilitazione del bluetooth
                        setUpErrorsBox(true,!isLocationGranted, !isGpsEnabled,0);
                        break;
                }
            }
        }
    };

    /**
     * Viene mostrato o nascosto il box con la casella di abilitazione bluetooth e con la casella di
     * richiesta di autorizzazione per la posizione fine.
     * @param bluetooth true per mostrare la casella di abilitazione bluetooth, false per nasconderla.
     * @param fineLocation true per mostrare la casella di richiesta dell'autorizzazione per la posizione precisa, false per nasconderla.
     * @param animationDuration durata dell'animazione.
     */
    private void setUpErrorsBox(boolean bluetooth, boolean fineLocation, boolean gps, int animationDuration){
        //Viene mostrato il box di errore relativo al bluetooth
        if(bluetooth) AnimationHelper.fadeIn(bluetoothDisabledBox,animationDuration);
        else AnimationHelper.fadeOutWithGone(bluetoothDisabledBox,animationDuration);

        //Viene mostrato solo uno tra l'autorizzazione Posizione eccellente e la posizione GPS con priorità sull'autorizzazione Posizione eccellente
        if(fineLocation){
            AnimationHelper.fadeIn(locationPermissionNotGivenBox,animationDuration);
            AnimationHelper.fadeOutWithGone(geolocationDisabledBox,animationDuration);
        } else if(gps) {
            AnimationHelper.fadeOutWithGone(locationPermissionNotGivenBox,animationDuration);
            AnimationHelper.fadeIn(geolocationDisabledBox,animationDuration);
        }else{
            AnimationHelper.fadeOutWithGone(locationPermissionNotGivenBox,animationDuration);
            AnimationHelper.fadeOutWithGone(geolocationDisabledBox,animationDuration);
        }

        ///Vengono mostrati i box contenitori se almeno un box interno viene visualizzato
        if(bluetooth || fineLocation || gps) AnimationHelper.fadeIn(errorsBox,animationDuration);
        else AnimationHelper.fadeOutWithGone(errorsBox,animationDuration);

        //Viene mostrato uno spazio tra i due box solo se entrambi vengono mostrati
        if(bluetooth && (fineLocation || gps)) AnimationHelper.fadeIn(errorsBoxSpace,animationDuration);
        else AnimationHelper.fadeOutWithGone(errorsBoxSpace,animationDuration);

        //Viene avviato il gatt server
        if(bluetooth && !(gps || fineLocation)){

        }else if(bluetooth && gps && fineLocation){

        }
    }



    /**
     * Richiamato quando si vogliono visionare le informazioni tecniche relative al funzionamento dell'applicazione
     */
    private void onClickTechnicalInfo(){
        Intent viewTechnicalInfo = new Intent(getActivity(), TechnicalInfoActivity.class);
        startActivity(viewTechnicalInfo);
    }

    /**
     * Richiamato quando si fa clic sul pulsante "Abilita Bluetooth".
     */
    private void onEnableBluetoothClick(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_CODE);
            }
        }else{
            //Comunica all'utente che il dispositivo non supporta la tecnologia bluetooth
            Toast.makeText(getContext(),R.string.bluetoothNotSupported, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Metodo richiamato quando si fa clic sul pulsante di concessione dell'autorizzazione per la posizione ottimale.
     */
    private void onGivePermissionButtonClick(){
        //Richiedi il permesso Fine Location.
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Si richiama il layout relativo alle impostazioni per l'attivazione della geolocalizzazione
     */
    private void onEnableGeolocationClick(){
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    /**
     * Metodo che serve per richiedere i permessi all'utente in merito alla geolocalizzazione
     * @param requestCode :codice di richiesta passato ActivityCompat.requestPermissions(android.app.Activity, String[], int)
     * @param permissions :le autorizzazioni richieste di tipo String.
     * @param grantResults :i risultati della concessione per le autorizzazioni corrispondenti.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                Toast.makeText(getContext(),R.string.locationPermissionDeniedToast,Toast.LENGTH_SHORT).show();
                isLocationGranted = false;
            } else {
                Toast.makeText(getContext(),R.string.locationPermissionGrantedToast,Toast.LENGTH_SHORT).show();
                setUpErrorsBox(!BluetoothLEHelper.isBluetoothEnabled(), false,!isGpsEnabled,0);
                isLocationGranted = true;

                //Viene inoltrato un messaggio di broadcast al crawler
                Intent fineLocationGranted = new Intent("fineLocationGranted");
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(fineLocationGranted);
            }
        }
    }

    /**
     * Metodo che serve per la definizione delle attività rispetto alle scelte relative ai permessi effettuate dall'utente
     * @param requestCode :codice di richiesta intero originariamente fornito a startActivityForResult(),
     * che consente di identificare da chi proviene questo risultato
     * @param resultCode :codice risultato intero restituito dall'attività figlia tramite il suo setResult().
     * @param data :un intento che può restituire i dati dei risultati al chiamante.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            //nel caso in cui si decide di attivare il bluetooth ne viene comunicata la relativa attivazione
            case ENABLE_BT_REQUEST_CODE:
                if(resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getContext(),R.string.bluetoothEnabledToast,Toast.LENGTH_SHORT).show();

                    //Viene modificato il box
                    setUpErrorsBox(false, !isLocationGranted,!isGpsEnabled,0);
                    isBluetoothEnabled = true;
                    //in caso contrario, si comunica la non attivazione del bluetooth
                } else if(resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(getContext(),R.string.bluetoothNotEnabledToast,Toast.LENGTH_SHORT).show();
                    isBluetoothEnabled = false;
                }
                break;
        }
    }
}
