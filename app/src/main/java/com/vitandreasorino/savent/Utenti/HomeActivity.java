package com.vitandreasorino.savent.Utenti;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vitandreasorino.savent.Utenti.Settings.SettingsActivity;
import com.vitandreasorino.savent.Utenti.AccountTab.AccountFragment;
import com.vitandreasorino.savent.Utenti.EventiTab.EventFragment;
import com.vitandreasorino.savent.Utenti.GruppiTab.GroupFragment;
import com.vitandreasorino.savent.R;
import com.vitandreasorino.savent.Utenti.Notification.NotificationActivity;
import Helper.AnimationHelper;
import Helper.LocalStorage.SQLiteHelper;
import Helper.LocalStorage.SharedPreferencesHelper;
import Model.DAO.Utenti;
import Services.BluetoothLEServices.GattServerCrawlerService;
import Services.BluetoothLEServices.GattServerService;


public class HomeActivity extends AppCompatActivity implements SensorEventListener {

    public boolean flag = true;

    HealthStatus actualHealthStatus = HealthStatus.NOT_DEFINED_YET;
    private SensorManager sensorManager;
    private Sensor sensorProximity;

    Toolbar topBar;
    LinearLayout imageAndStatusContainer;

    ImageView statusLogoSmall;
    ImageView statusLogoBig;
    TextView textStatusHomeBig;
    TextView textStatusHomeSmall;

    FrameLayout frameLayoutNotificationNumber;
    TextView textViewNotificationNumber;
    private static final int FROM_NOTIFICATION_RESULT = 10;

    Button notificationButton;
    Button buttonSetting;

    Class previousFragmentClass = HomeFragment.class;
    TopBarConfiguration previousConfiguration = TopBarConfiguration.BIG;

    ViewPager2 viewPager;

    //Dichiarazione e inizializzazione per la finestra di dialogo per l'attivazione/disattivazione del servizio
    private AlertDialog serviceDialog = null;

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorProximity, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Si ottiene la vista della barra di navigazione inferiore allegandone il listener
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        topBar = (Toolbar) findViewById(R.id.toolbarStatusHome);
        imageAndStatusContainer = (LinearLayout) findViewById(R.id.imageAndStatusLayoutContainer);

        statusLogoSmall = findViewById(R.id.logoStatusHomeSmall);
        statusLogoBig = findViewById(R.id.logoStatusHomeBig);
        textStatusHomeSmall = findViewById(R.id.textStatusHomeSmall);
        textStatusHomeBig = findViewById(R.id.textStatusHomeBig);
        notificationButton = findViewById(R.id.buttonNotification);
        buttonSetting = findViewById(R.id.buttonSetting);

        //Si registra il lister per il broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("updateStatusHealth"));

        frameLayoutNotificationNumber = findViewById(R.id.frameLayoutNotificationNumber);
        textViewNotificationNumber = findViewById(R.id.textViewNotificationNumber);
        setNotificationNumber();

        //Si registra il lister per il broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(updateNoficiationNumber, new IntentFilter("UpdateNotification"));

        viewPager = findViewById(R.id.viewPager);
        viewPager.setSaveEnabled(false);
        viewPager.setAdapter(new HomeFragmentAdapter(this));
        viewPager.setUserInputEnabled(false);

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            /**
             * Metodo chiamato quando viene selezionato un elemento nel menu di navigazione.
             * @param item: componente di tipo menu da utilizzare
             * @return true per visualizzare l'elemento come elemento selezionato
             */
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    //Nel caso in cui si clicchi sulla sezione home viene visualizzato l'home fragment
                    case R.id.nav_home:
                        moveTopBar(TopBarConfiguration.BIG,HomeFragment.class);
                        viewPager.setCurrentItem(0,false);
                        previousFragmentClass = HomeFragment.class;
                        return true;
                    //Nel caso in cui si clicchi sulla sezione gruppi viene visualizzato il group fragment
                    case R.id.nav_group:
                        moveTopBar(TopBarConfiguration.SMALL,GroupFragment.class);
                        viewPager.setCurrentItem(1,false);
                        previousFragmentClass = GroupFragment.class;
                        return true;
                    //Nel caso in cui si clicchi sulla sezione eventi viene visualizzato l'event fragment
                    case R.id.nav_event:
                        moveTopBar(TopBarConfiguration.SMALL,EventFragment.class);
                        viewPager.setCurrentItem(2,false);
                        previousFragmentClass = EventFragment.class;
                        return true;
                    //Nel caso in cui si clicchi sulla sezione della visualizzazione del proprio account, si accede all'account fragment
                    case R.id.nav_account:
                        moveTopBar(TopBarConfiguration.SMALL,AccountFragment.class);
                        viewPager.setCurrentItem(3,false);
                        previousFragmentClass = AccountFragment.class;
                        return true;
                }
                return false;
            }
        });

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) statusLogoSmall.setAlpha(0f);

        //In caso di cambio di orientamento, si ripristina lo stato precedente
        if(savedInstanceState != null){
            boolean prevS = savedInstanceState.getBoolean("previousConfiguration_BIG");
            if(prevS == true) previousConfiguration = TopBarConfiguration.BIG;
            else previousConfiguration = TopBarConfiguration.SMALL;
            reloadConfiguration();

            String prevClass = savedInstanceState.getString("previousFragmentClass");
            switch (prevClass){
                case "HomeFragment":
                    viewPager.setCurrentItem(0);
                    navigation.setSelectedItemId(R.id.nav_home);
                    break;
                case "GroupFragment":
                    viewPager.setCurrentItem(1);
                    navigation.setSelectedItemId(R.id.nav_group);
                    break;
                case "EventFragment":
                    viewPager.setCurrentItem(2);
                    navigation.setSelectedItemId(R.id.nav_event);
                    break;
                case "AccountFragment":
                    viewPager.setCurrentItem(3);
                    navigation.setSelectedItemId(R.id.nav_account);
                    break;
            }
        }

        //Si setta il listener etting the listener all'utente che si è loggato
        Utenti.addDocumentListener(this, newUser -> {
            setHealthStatusInView(newUser.getStatusSanitario());
        });

    }

    /**
     * Ricevitore di messaggio broadcast per aggiornare il numero
     * di notifiche quando se ne riceve un altra
     */
    private BroadcastReceiver updateNoficiationNumber = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setNotificationNumber();
        }
    };

    /**
     * Scarica il numero di notifiche presenti nel database
     * e le visualizza sull'icona delle notifiche.
     */
    private void setNotificationNumber(){
        SQLiteHelper database = new SQLiteHelper(this);
        int i = database.getNumberOfUnreadNotification();

        if (i == 0){
            frameLayoutNotificationNumber.setVisibility(View.INVISIBLE);
        }else{
            frameLayoutNotificationNumber.setVisibility(View.VISIBLE);
            textViewNotificationNumber.setText(""+i);
        }
    }

    /**
     * Settaggio dello status sanitario dell'utente loggato
     * **/
    private void setHealthStatus() {
        Utenti.addDocumentListener(this, newUser -> {
            setHealthStatusInView(newUser.getStatusSanitario());
        });
    }


    /**
     * Ricevitore di messaggio broadcast per aggiornare lo status
     * sanitario dell'utente loggato una volta che comunica l'esito del
     * tampone
     */
    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean isUpdatedStatus = intent.getBooleanExtra("updatedStatusHealth", false);
            if(isUpdatedStatus){
                setHealthStatus();
            }
        }
    };

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("previousConfiguration_BIG",previousConfiguration == TopBarConfiguration.BIG);
        outState.putString("previousFragmentClass",previousFragmentClass.getSimpleName());
    }

    /**
     * Settaggio dell'interfaccia utente in base allo stato di salute dell'utente connesso.
     *
     * @param healthStatus lo stato di salute dell'utente connesso.
     */
    @SuppressLint("ResourceType")
    private void setHealthStatusInView(int healthStatus){
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            if(healthStatus <= 33 && actualHealthStatus != HealthStatus.GREEN) {
                //Si dichiara la negatività impostando l'icona Savent verde
                if(previousConfiguration == TopBarConfiguration.SMALL) AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.green_status_icon);
                else statusLogoSmall.setImageResource(R.drawable.green_status_icon);

                AnimationHelper.switchImageWithFadeAnimations(statusLogoBig,R.drawable.green_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeBig,R.string.greenStatusText, null);
                actualHealthStatus = HealthStatus.GREEN;
            }else if(healthStatus > 33 && healthStatus <= 66 && actualHealthStatus != HealthStatus.YELLOW){
                //Si dichiara l'imminenza di un cambio di stato impostando l'icona Savent gialla
                if(previousConfiguration == TopBarConfiguration.SMALL) AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.yellow_status_icon);
                else statusLogoSmall.setImageResource(R.drawable.yellow_status_icon);

                AnimationHelper.switchImageWithFadeAnimations(statusLogoBig,R.drawable.yellow_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeBig,R.string.yelloStatusText,null);
                actualHealthStatus = HealthStatus.YELLOW;
            }else if(healthStatus > 66 && healthStatus <= 100 && actualHealthStatus != HealthStatus.RED){
                //Si dichiara la positività impostando l'icona Savent rossa
                if(previousConfiguration == TopBarConfiguration.SMALL) AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.red_status_icon);
                else statusLogoSmall.setImageResource(R.drawable.red_status_icon);

                AnimationHelper.switchImageWithFadeAnimations(statusLogoBig,R.drawable.red_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeBig,R.string.redStatusText,null);
                actualHealthStatus = HealthStatus.RED;
            }
        }else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            //Se si rientra fino al 33% si è negativi, e quindi si imposta l'icona verde
            if(healthStatus <= 33 && actualHealthStatus != HealthStatus.GREEN) {
                AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.green_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeSmall,R.string.greenStatusText,null);
                actualHealthStatus = HealthStatus.GREEN;
                //Se si rientra fino dal 33% al 66% si è a rischio contagio, e quindi si imposta l'icona gialla
            }else if(healthStatus > 33 && healthStatus <= 66 && actualHealthStatus != HealthStatus.YELLOW){
                AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.yellow_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeSmall,R.string.yelloStatusText,null);
                actualHealthStatus = HealthStatus.YELLOW;
                //Se si rientra fino dal 66% al 100% si è positivi, e quindi si imposta l'icona rossa
            }else if(healthStatus > 66 && healthStatus <= 100 && actualHealthStatus != HealthStatus.RED){
                AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.red_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeSmall,R.string.redStatusText,null);
                actualHealthStatus = HealthStatus.RED;
            }
        }
    }

    /**
     * Richiamato quando si verifica una modifica alla configurazione, ovvero quando il bundle non è null.
     */
    private void reloadConfiguration(){
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            if(previousConfiguration == TopBarConfiguration.BIG){
                AnimationHelper.slideView(topBar,topBar.getHeight(),AnimationHelper.dpToPx(200f,this),0);
                AnimationHelper.fadeIn(imageAndStatusContainer,0);
                AnimationHelper.fadeOut(statusLogoSmall,0);
            }else if(previousConfiguration == TopBarConfiguration.SMALL){
                AnimationHelper.slideView(topBar,topBar.getHeight(),AnimationHelper.dpToPx(55f,this),0);
                AnimationHelper.fadeOut(imageAndStatusContainer,0);
                AnimationHelper.fadeIn(statusLogoSmall,0);
            }
        }
    }

    /**
     * Viene eseguita l'animazione tra la BIG top bar e la SMALL top bar
     *
     * @param configuration la nuova configurazione da settare
     * @param selectedFragmentClass il nuovo fragment che rappresenta la destinazione. Viene utilizzato
     * per evitare di ricaricare il fragment quando viene premuto il pulsante del fragment mostrato.
     */
    private void moveTopBar(TopBarConfiguration configuration,Class selectedFragmentClass){
        //Eseguilo solo in modalità portrait
        if( getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && previousFragmentClass != selectedFragmentClass && previousConfiguration != configuration){
            switch (configuration){
                case BIG:
                    AnimationHelper.slideView(topBar,topBar.getHeight(),AnimationHelper.dpToPx(200f,this),400);
                    AnimationHelper.fadeIn(imageAndStatusContainer,400);
                    AnimationHelper.fadeOut(statusLogoSmall,400);
                    previousConfiguration = TopBarConfiguration.BIG;
                    break;
                case SMALL:
                    AnimationHelper.slideView(topBar,topBar.getHeight(),AnimationHelper.dpToPx(55f,this),400);
                    AnimationHelper.fadeOut(imageAndStatusContainer,200);
                    AnimationHelper.fadeIn(statusLogoSmall,200);
                    previousConfiguration = TopBarConfiguration.SMALL;
                    break;
            }
        }

        //se il landscape aggiorna solo la configurazione precedente
        if( getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            switch (configuration){
                case BIG:
                    previousConfiguration = TopBarConfiguration.BIG;
                    break;
                case SMALL:
                    previousConfiguration = TopBarConfiguration.SMALL;
                    break;
            }
        }
    }

    /**
     * Metodo che legge le preferenze impostate nel setting, e gestisce l'abilitazione e la disattivazione dei servizi di tracciamento in base al
     * passaggio della mano sul sensore di prossimità.
     * La variabile di flag ci serve per entrare una sola volta nel blocco ad ogni passaggio di mano.
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        //Se la preferenza di proximity sensor e di Ble sono attive, allora disattiva i servizi di tracciamento del BLe
        if(SharedPreferencesHelper.getProximitySensorPreference(this) && SharedPreferencesHelper.getBluetoothPreference(this)) {

            //si attiva nel caso l'utente ha passato la mano sul sensore
            if(event.values[0] == 0.0 && flag == true) {

                //lancio del popUp e invio dei msg di broadcast per killare i processi dei servizi di BLE
                lanchedPopUp(R.layout.disable_tracking_dialog);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(GattServerService.STOP_GATT_SERVER_INTENT));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(GattServerCrawlerService.STOP_GATT_CRAWLER_INTENT));

                //si disattiva la preferenza di BLE una volta killato i suoi processi e flag di disattivazione entrata nel blocco
                SharedPreferencesHelper.setBluetoothPreference(false,this);
                flag = false;
            }

        }

        //Se invece la preferenza di proximity sensor è attiva ma quella di Ble no, allora attiva i servizi di tracciamento del BLe
        if(SharedPreferencesHelper.getProximitySensorPreference(this) && SharedPreferencesHelper.getBluetoothPreference(this) == false) {

            //si attiva nel caso l'utente ha passato la mano sul sensore
            if(event.values[0] == 0.0 && flag == true) {

                if(GattServerService.isServiceRunning){
                    //Si invia il broadcast
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(GattServerService.RESTART_GATT_SERVER_INTENT));
                }else{
                    startService(new Intent(getBaseContext(), GattServerService.class));
                }

                if(GattServerCrawlerService.isServiceRunning){
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(GattServerCrawlerService.RESTART_GATT_CRAWLER_INTENT));
                }else{
                    startService(new Intent(getBaseContext(), GattServerCrawlerService.class));
                }

                //si lancia il popUp, avvio dei servizi
                lanchedPopUp(R.layout.activate_tracking_dialog);

                //si attiva la preferenza di BLE una volta avviato i servizi e si imposta il flag di disattivazione entrata nel blocco
                SharedPreferencesHelper.setBluetoothPreference(true,this);
                flag = false;
            }
        }
    }

    //Si implementa il popUp personalizzato
    private void lanchedPopUp(int dialog) {
        AlertDialog.Builder alertSensorProximity = new AlertDialog.Builder(this);
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
        View view = layoutInflaterAndroid.inflate(dialog, null);
        alertSensorProximity.setView(view);
        alertSensorProximity.setCancelable(false);
        serviceDialog = alertSensorProximity.create();
        serviceDialog.show();

    }

    /**
     * Metodo che si verifica se si clicca sul button ok presente all'interno del popUp personalizzato che
     * riporta il flag a true per rientrare nel blocco e procede con la chiusura del popUp
     * @param view
     */
    public void onConfermePopUp(View view) {
        flag = true;
        serviceDialog.dismiss();
    }

    /**
     * Metodo che richiama la schermata delle notifiche creando un intent apposito
     * @param view : la nuova vista raffigurante le notifiche
     */
    public void onClickNotificationButton(View view){
        Intent schermataNotification = new Intent(getApplicationContext(), NotificationActivity.class);
        startActivityForResult(schermataNotification,FROM_NOTIFICATION_RESULT);
    }

    /**
     * Metodo che serve per la definizione delle attività rispetto alle scelte relative al request Code .
     * @param requestCode :codice di richiesta intero originariamente fornito a startActivityForResult(),
     * che consente di identificare da chi proviene questo risultato
     * @param resultCode :codice risultato intero restituito dall'attività figlia tramite il suo setResult().
     * @param data :un intento che può restituire i dati della modifica al chiamante.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == FROM_NOTIFICATION_RESULT){
            setNotificationNumber();
        }
    }

    /**
     * Metodo che richiama l'intent creato per avviare la schermata delle impostazioni dalla home
     * @param view
     */
    public void onClickSettingsButton(View view){
        Intent schermataSettings = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(schermataSettings);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Definizione dell'oggetto di tipo enum per esplicitare lo stato dell'interfaccia utente in base allo stato di salute dell'utente.
     */
    private enum HealthStatus{
        GREEN,
        YELLOW,
        RED,
        NOT_DEFINED_YET;
    }

    /**
     * Definizione dell'oggetto di tipo enum per lo stato dell'interfaccia utente in base alla configurazione della barra superiore.
     */
    private enum TopBarConfiguration{
        BIG,
        SMALL
    }
    
}



/**
 * Adapter creato appositamente per mostrare i 4 fragment disponibili all'interno della HomeActivity
 */
class HomeFragmentAdapter extends FragmentStateAdapter {

    public HomeFragmentAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position){
            case 0:
                return new HomeFragment();
            case 1:
                return new GroupFragment();
            case 2:
                return new EventFragment();
            case 3:
                return new AccountFragment();
            default:
                return null;
        }
    }

    /**
     * Metodo che restituisce il numero dei fragment creati per la home activity
     * @return il numero di fragment creati
     */
    @Override
    public int getItemCount() {
        return 4;
    }
}