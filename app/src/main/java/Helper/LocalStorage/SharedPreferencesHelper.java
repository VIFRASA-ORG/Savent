package Helper.LocalStorage;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;
import java.util.Date;

/**
 * Classe Helper con tutti i metodi che permettono di impostare, cambiare
 * e resettare tutti i valori delle shared preference usate dall'applicazione.
 */
public class SharedPreferencesHelper {

    /**
     * COSTANTI CHE INDICANO I NOMI DEI FILE DELLE SharedPreference.
     */
    private static final String PREFS_NAME = "SaventPref";

    /**
     * CONSTANTI CHE INDICANO LE CHIAVI CON CUI VENGONO MEMORIZZATI
     * I VALORI DELLE SCHARED PREFERENCE UTILIZZATE.
     */
    private static final String BLUETOOTH_PREF = "BluetoothInfo";
    private static final String PROXIMITY_SENSOR_PREF = "SensorInfo";
    private static final String LAST_SERVER_POSITIVE_TEK_UPDATE_TIME_PREF = "LAST_SERVER_POSITIVE_TEK_UPDATE_TIME";




    /**
     * Metodo per salvare la shared preference contenente la data dell'ultimo
     * aggiornamento dei tek positivi.
     *
     * @param context il contesto.
     * @param date la nuova data da salvare.
     */
    public static void setLastPositiveTekUpdateTime(Context context, Date date){
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, context.MODE_PRIVATE).edit();
        editor.putLong(LAST_SERVER_POSITIVE_TEK_UPDATE_TIME_PREF,date.getTime());
        editor.apply();
    }

    /**
     * Metodo per salvare la shared preference del tracciamento bluetooth
     *
     * @param value nuovo valore della shared preference
     * @param context contesto relativo all'activity
     */
    public static void setBluetoothPreference(boolean value, Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, context.MODE_PRIVATE).edit();
        editor.putBoolean(BLUETOOTH_PREF, value);
        editor.apply();
    }

    /**
     * Metodo per salvare la shared preference della funzionalità del sensore di prossimità
     *
     * @param value nuovo valore della shared preference
     * @param context contesto relativo all'activity
     */
    public static void setProximitySensorPreference(boolean value, Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, context.MODE_PRIVATE).edit();
        editor.putBoolean(PROXIMITY_SENSOR_PREF,value);
        editor.apply();
    }

    /**
     * Metodo per resettare tutti i valori delle shared preferences
     *
     * @param context contesto relativo all'activity
     */
    public static void resetSharedPreferences(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }

    /**
     * Metodo per leggere il valore della shared preference della funzionalità
     * di tracciamento tramite il bluetooth LE
     *
     * @param context contesto relativo all'activity
     * @return il valore corrente della shared preference se esiste, altrimenti "true"
     */
    public static boolean getBluetoothPreference(Context context){
        SharedPreferences sharedPreferences1 = context.getSharedPreferences(PREFS_NAME, context.MODE_PRIVATE);
        return sharedPreferences1.getBoolean(BLUETOOTH_PREF, true);
    }

    /**
     * Metodo per leggere il valore della shared preference della funzionalità
     * del sensore di prossimità
     *
     * @param context contesto relativo all'activity
     * @return il valore corrente della shared preference se esiste, altrimenti "true"
     */
    public static boolean getProximitySensorPreference(Context context){
        SharedPreferences sharedPreferences2 = context.getSharedPreferences(PREFS_NAME, context.MODE_PRIVATE);
        return sharedPreferences2.getBoolean(PROXIMITY_SENSOR_PREF, true);
    }

    /**
     * Metodo per leggere il valore della shared preference contenente la data
     * dell'ultimo aggiornamento dei tek dei positivi dal server.
     *
     * @param context il contesto
     * @return la data dell'ultimo aggiornamento.
     */
    public static Date getLastPositiveTekUpdateTime(Context context){
        SharedPreferences sharedPreferences2 = context.getSharedPreferences(PREFS_NAME, context.MODE_PRIVATE);
        long l = sharedPreferences2.getLong(LAST_SERVER_POSITIVE_TEK_UPDATE_TIME_PREF, 0);

        if(l == 0 ) return null;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(l);
        return c.getTime();
    }
}