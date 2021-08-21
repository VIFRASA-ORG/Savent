package Helper;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreferencesHelper {

    private static final String BLUETOOTH_PREF = "BluetoothInfo";
    private static final String PROXIMITY_SENSOR_PREF = "SensorInfo";

    /**
     * Metodo per salvare la shared preference del tracciamento bluetooth
     *
     * @param value nuovo valore della shared preference
     * @param context contesto relativo all'activity
     */
    public static void setBluetoothPreference(boolean value, Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences(BLUETOOTH_PREF, context.MODE_PRIVATE).edit();
        editor.putBoolean("value", value);
        editor.apply();
    }

    /**
     * Metodo per salvare la shared preference della funzionalità del sensore di prossimità
     *
     * @param value nuovo valore della shared preference
     * @param context contesto relativo all'activity
     */
    public static void setProximitySensorPreference(boolean value, Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences(PROXIMITY_SENSOR_PREF, context.MODE_PRIVATE).edit();
        editor.putBoolean("value",value);
        editor.apply();
    }

    /**
     * Metodo per resettare tutti i valori delle shared preferences
     *
     * @param context contesto relativo all'activity
     */
    public static void resetSharedPreferences(Context context){
        SharedPreferences sharedPreferences1 = context.getSharedPreferences(BLUETOOTH_PREF, context.MODE_PRIVATE);
        sharedPreferences1.edit().clear().apply();

        SharedPreferences sharedPreferences2 = context.getSharedPreferences(PROXIMITY_SENSOR_PREF, context.MODE_PRIVATE);
        sharedPreferences2.edit().clear().apply();
    }

    /**
     * Metodo per leggere il valore della shared preference della funzionalità
     * di tracciamento tramite il bluetooth LE
     *
     * @param context contesto relativo all'activity
     * @return il valore corrente della shared preference se esiste, altrimenti "true"
     */
    public static boolean getBluetoothPreference(Context context){
        SharedPreferences sharedPreferences1 = context.getSharedPreferences(BLUETOOTH_PREF, context.MODE_PRIVATE);
        return sharedPreferences1.getBoolean("value", true);
    }

    /**
     * Metodo per leggere il valore della shared preference della funzionalità
     * del sensore di prossimità
     *
     * @param context contesto relativo all'activity
     * @return il valore corrente della shared preference se esiste, altrimenti "true"
     */
    public static boolean getProximitySensorPreference(Context context){
        SharedPreferences sharedPreferences2 = context.getSharedPreferences(PROXIMITY_SENSOR_PREF, context.MODE_PRIVATE);
        return sharedPreferences2.getBoolean("value", true);
    }
}