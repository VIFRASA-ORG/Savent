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

    //All component for the permission and bluetooth box
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

        //Setting the on click
        textTechnicalInfo.setOnClickListener(v -> onClickTechnicalInfo());
        enableBluetoothButton.setOnClickListener( v -> onEnableBluetoothClick());
        givePermissionButton.setOnClickListener(v -> onGivePermissionButtonClick());
        enableGeolocationButton.setOnClickListener(v -> onEnableGeolocationClick());

        //Getting the status of the bluetooth and of the permission
        isBluetoothEnabled = BluetoothLEHelper.isBluetoothEnabled();
        isLocationGranted = BluetoothLEHelper.isFineLocationGranted(getContext());
        isGpsEnabled = BluetoothLEHelper.isGpsEnabled(getContext());

        //Show the errorsBox
        setUpErrorsBox(!isBluetoothEnabled, !isLocationGranted, !isGpsEnabled,0);

        //Set a filter to only receive bluetooth state changed events.
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        getActivity().registerReceiver(bluetoothStateReceiver, filter);

        //Add a receiver for the GPS state event
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
     * Broadcast receiver to change the bluetooth box when the bluetooth status is updated from the system.
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
                        //Bluethooth is on
                        isBluetoothEnabled = true;
                        //Remove the bluetooth enable request box
                        setUpErrorsBox(false,!isLocationGranted, !isGpsEnabled,0);
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        isBluetoothEnabled = false;
                        //Leave visible the bluetooth enable request box
                        setUpErrorsBox(true,!isLocationGranted, !isGpsEnabled,0);
                        break;
                }
            }
        }
    };

    /**
     * Method that show or hide the box with the bluetooth enable box and with the Fine Location permission request box.
     *
     * @param bluetooth true to show the bluetooth enable box, false to hide it.
     * @param fineLocation true to show the Fine Location Permission request box, false to hide it.
     * @param animationDuration duration of the animation.
     */
    private void setUpErrorsBox(boolean bluetooth, boolean fineLocation, boolean gps, int animationDuration){
        //Show the bluetooth error box
        if(bluetooth) AnimationHelper.fadeIn(bluetoothDisabledBox,animationDuration);
        else AnimationHelper.fadeOutWithGone(bluetoothDisabledBox,animationDuration);

        //Show only one between the Fine location permission and gps location with priority to the Fine location permission
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

        //Show the boxes container if at least one inner box has to be shown.
        if(bluetooth || fineLocation || gps) AnimationHelper.fadeIn(errorsBox,animationDuration);
        else AnimationHelper.fadeOutWithGone(errorsBox,animationDuration);

        //Show a space betwee the two box only if both has to be shown
        if(bluetooth && (fineLocation || gps)) AnimationHelper.fadeIn(errorsBoxSpace,animationDuration);
        else AnimationHelper.fadeOutWithGone(errorsBoxSpace,animationDuration);

        //start the gatt server
        if(bluetooth && !(gps || fineLocation)){

        }else if(bluetooth && gps && fineLocation){

        }
    }



    /**
     * Method invoked when the technical info TextView is clicked.
     */
    private void onClickTechnicalInfo(){
        Intent viewTechnicalInfo = new Intent(getActivity(), TechnicalInfoActivity.class);
        startActivity(viewTechnicalInfo);
    }

    /**
     * Method invoked when the Enable Bluetooth button is clicked.
     */
    private void onEnableBluetoothClick(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_CODE);
            }
        }else{
            // Device doesn't support Bluetooth
            Toast.makeText(getContext(),R.string.bluetoothNotSupported, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Method invoked when the grant Fine Location permission button is clicked.
     */
    private void onGivePermissionButtonClick(){
        //Request the Fine Locarion permssion.
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void onEnableGeolocationClick(){
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }



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

                //Sending the broadcast message to the crawler
                Intent fineLocationGranted = new Intent("fineLocationGranted");
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(fineLocationGranted);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case ENABLE_BT_REQUEST_CODE:
                if(resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getContext(),R.string.bluetoothEnabledToast,Toast.LENGTH_SHORT).show();

                    //Update the box
                    setUpErrorsBox(false, !isLocationGranted,!isGpsEnabled,0);
                    isBluetoothEnabled = true;
                } else if(resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(getContext(),R.string.bluetoothNotEnabledToast,Toast.LENGTH_SHORT).show();
                    isBluetoothEnabled = false;
                }
                break;
        }
    }
}
