package Helper.LocalStorage;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;
import java.util.Date;


/**
 * Helper class with all the method to set, reset and change all
 * the shared preference used by the app.
 */
public class SharedPreferencesHelper {

    private static final String BLUETOOTH_PREF = "BluetoothInfo";
    private static final String PROXIMITY_SENSOR_PREF = "SensorInfo";

    private static final String LAST_SERVER_POSITIVE_TEK_UPDATE_TIME = "LAST_SERVER_POSITIVE_TEK_UPDATE_TIME";




    /**
     * Metodo per salvare la shared preference contenente la data dell'ultimo
     * aggiornamento dei tek positivi.
     *
     * @param context il contesto.
     * @param date la nuova data da salvare.
     */
    public static void setLastPositiveTekUpdateTime(Context context, Date date){
        SharedPreferences.Editor editor = context.getSharedPreferences(LAST_SERVER_POSITIVE_TEK_UPDATE_TIME, context.MODE_PRIVATE).edit();
        editor.putLong("value", Calendar.getInstance().getTimeInMillis());
        editor.apply();
    }

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

        SharedPreferences sharedPreferences3 = context.getSharedPreferences(LAST_SERVER_POSITIVE_TEK_UPDATE_TIME, context.MODE_PRIVATE);
        sharedPreferences3.edit().clear().apply();
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

    /**
     * Metodo per leggere il valore della shared preference contenente la data
     * dell'ultimo aggiornamento dei tek dei positivi dal server.
     *
     * @param context il contesto
     * @return la data dell'ultimo aggiornamento.
     */
    public static Date getLastPositiveTekUpdateTime(Context context){
        SharedPreferences sharedPreferences2 = context.getSharedPreferences(LAST_SERVER_POSITIVE_TEK_UPDATE_TIME, context.MODE_PRIVATE);
        long l = sharedPreferences2.getLong("value", 0);

        if(l == 0 ) return null;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(l);
        return c.getTime();
    }
}