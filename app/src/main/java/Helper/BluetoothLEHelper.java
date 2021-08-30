package Helper;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;


/**
 * Classe Helper con metodi a supporto delle funzionalità
 * per utilizzare il Bluetooth.
 *
 * Contiene anche tutte le costanti utilizzate per definire i
 * BluetoothLE Server e Crawler.
 *
 */
public class BluetoothLEHelper {

    /**
     * Definizione di tutti gli UUID
     * Identificatori per il service e le characteristic del BluetoothLE GATT Server.
     */
    public static final String UUID_SERVICE = "33ae23df-9282-4b3c-8f32-c137be4c4979";
    public static final String UUID_CHARACTERISTIC_SEND = "8eda04c3-38c3-4778-9d06-4e404bcbea33";
    public static final String UUID_CHARACTERISTIC_RECEIVE = "8eda04c3-38c3-4778-9d06-4e404bcbea34";


    /**
     * Controlla se il Bluetooth è abilitato.
     *
     * @return true se il bluetooth è abilitato, false altrimenti.
     */
    public static final boolean isBluetoothEnabled(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) return false;
        else if (!mBluetoothAdapter.isEnabled()) return false;
        else return true;
    }

    /**
     * Controlla se il permesso per FineLocation è stato dato.
     *
     * @param context contesto dell'applicazione o dell'activity.
     * @return true se il permesso è stato dato dall'utente, false altrimenti.
     */
    public static final boolean isFineLocationGranted(Context context){
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return false;
        else return true;
    }

    /**
     * Controlla se la geolocalizzazione è abilitata.
     *
     * @param context contesto dell'applicazione o dell'activity.
     * @return true se la geolocalizzazione è attiva, false altrimenti.
     */
    public static final boolean isGpsEnabled(Context context){
        if(context == null) return false;
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE );
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
