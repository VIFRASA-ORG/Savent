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
import android.util.Log;
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
import Model.DB.Utenti;
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

    //Dialog for the activate\disable dialog service
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

        //getting bottom navigation view and attaching the listener
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

        //registrazione del lister per il broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("updateStatusHealth"));

        frameLayoutNotificationNumber = findViewById(R.id.frameLayoutNotificationNumber);
        textViewNotificationNumber = findViewById(R.id.textViewNotificationNumber);
        setNotificationNumber();

        //registrazione del lister per il broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(updateNoficiationNumber, new IntentFilter("UpdateNotification"));

        viewPager = findViewById(R.id.viewPager);
        viewPager.setSaveEnabled(false);
        viewPager.setAdapter(new HomeFragmentAdapter(this));
        viewPager.setUserInputEnabled(false);

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        moveTopBar(TopBarConfiguration.BIG,HomeFragment.class);
                        viewPager.setCurrentItem(0,false);
                        previousFragmentClass = HomeFragment.class;
                        return true;
                    case R.id.nav_group:
                        moveTopBar(TopBarConfiguration.SMALL,GroupFragment.class);
                        viewPager.setCurrentItem(1,false);
                        previousFragmentClass = GroupFragment.class;
                        return true;
                    case R.id.nav_event:
                        moveTopBar(TopBarConfiguration.SMALL,EventFragment.class);
                        viewPager.setCurrentItem(2,false);
                        previousFragmentClass = EventFragment.class;
                        return true;
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

        // Restore the previous state, in case a orientation change append
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

        //Setting the listener to the logged in user.
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

    /* Metodo per settare lo status sanitario dell'utente loggato */
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
     * Set the user interface based on the logged in user health status.
     *
     * @param healthStatus the logged in user halth status.
     */
    @SuppressLint("ResourceType")
    private void setHealthStatusInView(int healthStatus){
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            if(healthStatus <= 33 && actualHealthStatus != HealthStatus.GREEN) {
                //Green
                if(previousConfiguration == TopBarConfiguration.SMALL) AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.green_status_icon);
                else statusLogoSmall.setImageResource(R.drawable.green_status_icon);

                AnimationHelper.switchImageWithFadeAnimations(statusLogoBig,R.drawable.green_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeBig,R.string.greenStatusText, null);
                actualHealthStatus = HealthStatus.GREEN;
            }else if(healthStatus > 33 && healthStatus <= 66 && actualHealthStatus != HealthStatus.YELLOW){
                //Yellow
                if(previousConfiguration == TopBarConfiguration.SMALL) AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.yellow_status_icon);
                else statusLogoSmall.setImageResource(R.drawable.yellow_status_icon);

                AnimationHelper.switchImageWithFadeAnimations(statusLogoBig,R.drawable.yellow_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeBig,R.string.yelloStatusText,null);
                actualHealthStatus = HealthStatus.YELLOW;
            }else if(healthStatus > 66 && healthStatus <= 100 && actualHealthStatus != HealthStatus.RED){
                //Red
                if(previousConfiguration == TopBarConfiguration.SMALL) AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.red_status_icon);
                else statusLogoSmall.setImageResource(R.drawable.red_status_icon);

                AnimationHelper.switchImageWithFadeAnimations(statusLogoBig,R.drawable.red_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeBig,R.string.redStatusText,null);
                actualHealthStatus = HealthStatus.RED;
            }
        }else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            if(healthStatus <= 33 && actualHealthStatus != HealthStatus.GREEN) {
                //Green
                AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.green_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeSmall,R.string.greenStatusText,null);
                actualHealthStatus = HealthStatus.GREEN;
            }else if(healthStatus > 33 && healthStatus <= 66 && actualHealthStatus != HealthStatus.YELLOW){
                //Yellow
                AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.yellow_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeSmall,R.string.yelloStatusText,null);
                actualHealthStatus = HealthStatus.YELLOW;
            }else if(healthStatus > 66 && healthStatus <= 100 && actualHealthStatus != HealthStatus.RED){
                //Red
                AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.red_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeSmall,R.string.redStatusText,null);
                actualHealthStatus = HealthStatus.RED;
            }
        }
    }

    /**
     * invoked when a configuration change happen, that is when the bundle is not null.
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
     * Function that execute the animation between the BIG top bar and the SMALL top bar configuration.
     *
     * @param configuration the new configuration to set.
     * @param selectedFragmentClass the new destination fragment. Used to avoid reloading the fragment when the shown fragment button is pressed.
     */
    private void moveTopBar(TopBarConfiguration configuration,Class selectedFragmentClass){
        //Do this only in portrait mode
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

        //if landscape update only the previousConfiguration
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
                Intent i = new Intent("killGattServerService");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
                Intent j = new Intent("killGattServerServiceCrawler");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(j);

                //disattivo la preferenza di BLE una volta killato i suoi processi e flag di disattivazione entrata nel blocco
                SharedPreferencesHelper.setBluetoothPreference(false,this);
                flag = false;
           }

        }

        //Se invece la preferenza di proximity sensor è attiva ma quella di Ble no, allora attiva i servizi di tracciamento del BLe
        if(SharedPreferencesHelper.getProximitySensorPreference(this) && SharedPreferencesHelper.getBluetoothPreference(this) == false) {

            //si attiva nel caso l'utente ha passato la mano sul sensore
            if(event.values[0] == 0.0 && flag == true) {

                //lancio del popUp, avvio dei servizi
                lanchedPopUp(R.layout.activate_tracking_dialog);
                startService(new Intent(getBaseContext(), GattServerService.class));
                startService(new Intent(getBaseContext(), GattServerCrawlerService.class));

                //attivo la preferenza di BLE una volta avviato i servizi e imposto il flag di disattivazione entrata nel blocco
                SharedPreferencesHelper.setBluetoothPreference(true,this);
                flag = false;
            }
        }
    }

    //implementazione popUp personalizzato
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
     * click del button ok all'interno del popUp personalizzato
     * riporta il flag a true per rientrare nel blocco e procede con la chiusura del popUp
     * @param view
     */
    public void onConfermePopUp(View view) {
        flag = true;
        serviceDialog.dismiss();
    }

    public void onClickNotificationButton(View view){
        Intent schermataNotification = new Intent(getApplicationContext(), NotificationActivity.class);
        startActivityForResult(schermataNotification,FROM_NOTIFICATION_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == FROM_NOTIFICATION_RESULT){
            setNotificationNumber();
        }
    }

    public void onClickSettingsButton(View view){
        Intent schermataSettings = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(schermataSettings);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }




    /*

        ENUM DECLARATION

     */

    /**
     * Used to explicit the user interface status based on the user Health status.
     */
    private enum HealthStatus{
        GREEN,
        YELLOW,
        RED,
        NOT_DEFINED_YET;
    }

    /**
     * Used to define the user interface status based on the top bar configuration.
     */
    private enum TopBarConfiguration{
        BIG,
        SMALL
    }
    
}



/**
 * Adepter specifically created to show the 4 fragment inside the HomeActivity
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

    @Override
    public int getItemCount() {
        return 4;
    }
}