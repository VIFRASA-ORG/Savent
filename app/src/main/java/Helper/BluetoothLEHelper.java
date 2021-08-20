package Helper;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;


/**
 * Helper class with support method for the Bluetooth.
 * It contains also all the UUID for the BLE services and characteristics.
 */
public class BluetoothLEHelper {

    //All UUID used by the GATT Server
    public static final String UUID_SERVICE = "33ae23df-9282-4b3c-8f32-c137be4c4979";
    public static final String UUID_CHARACTERISTIC_SEND = "8eda04c3-38c3-4778-9d06-4e404bcbea33";
    public static final String UUID_CHARACTERISTIC_RECEIVE = "8eda04c3-38c3-4778-9d06-4e404bcbea34";


    /**
     * Check if the bluetooth is enabled.
     *
     * @return true if the bluetooth is enabled, false otherwise.
     */
    public static final boolean isBluetoothEnabled(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) return false;
        else if (!mBluetoothAdapter.isEnabled()) return false;
        else return true;
    }

    /**
     * Check if the FineLocationPermission is Granted.
     *
     * @param context the activity context.
     * @return true if the permission is granted, false othewise.
     */
    public static final boolean isFineLocationGranted(Context context){
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return false;
        else return true;
    }
}
