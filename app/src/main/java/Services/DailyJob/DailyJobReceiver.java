package Services.DailyJob;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import com.vitandreasorino.savent.R;
import Helper.AuthHelper;
import Helper.LocalStorage.SQLiteHelper;
import Helper.LocalStorage.SharedPreferencesHelper;
import Helper.NotificationHelper;
import Model.DB.TemporaryExposureKeys;
import Model.DB.Utenti;
import Model.LogDebug;
import Model.Pojo.Notification;


/**
 * Classe derivata da BroadcastReceiver per ricevere i broadcast del task giornaliero.
 * Il task giornaliero deve scaricare un nuovo TEK, cancellare quelli scaduti,
 * scaricare quelli positivi, notificare l'utente se qualcuno dei suoi contatti è risultato positivo
 * e aggiornare il suo status sanitario in caso positivo.
 */
public class DailyJobReceiver extends BroadcastReceiver {

    // Codice da usare per avviare l'intent di relativo a questo receiver.
    private static final int DAILY_JOB_REQUEST_CODE = 123;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LogDebug.DAILY_JOB, "Daily task running right now");
        allWorkToDo(context);
    }

    /**
     * Metodo che esegue tutte le operazion da fare nel task giornaliero.
     *
     * @param context contesto della applicazione o dell'activity chiamante.
     */
    private void allWorkToDo(Context context){

        //Scaircamento del nuovo TEK
        TemporaryExposureKeys.generateNewTEK(context, null);

        //Cancellazione dei tek e dei tek dei contatti scaduti.
        SQLiteHelper db = new SQLiteHelper(context);
        db.deleteExpiredContact();
        db.deleteExpiredTek();

        /**
         * Ottenimento, dalle shared preference, dell'utima data di scaricamento dei TEK positivi
         * In maniera tale da scaricare solo quelli sucessivi a quella data soglia.
         */
        Date latestUpdatePositive = SharedPreferencesHelper.getLastPositiveTekUpdateTime(context);

        //Scaricamento dei TEK positivi a partire dall'ultima data di scaricamento se esiste, altrimenti scarichera tutti i TEK positivi.
        TemporaryExposureKeys.downloadPositiveTek(latestUpdatePositive, positiveTekList ->{

            if(positiveTekList != null){
                Log.i(LogDebug.DAILY_JOB_POS_TEK_DOWNL, "DOWNLOADED "+ positiveTekList.size());

                //Aggiornamento dell'ultima data di scaricamento
                SharedPreferencesHelper.setLastPositiveTekUpdateTime(context,new Date());

                //Salvataggio dei nuovi TEK nel database SQLite
                db.addPositiveTek(positiveTekList);

                //Controllo se si ha dei match con i positivi e calcolo del rischio.
                int sumRisk = 0;
                Map<String, Integer> contacts = db.matchTekContattiConTekPositivi(positiveTekList);
                for (String key : contacts.keySet()){
                    int numberOfOccur = contacts.get(key);

                    Log.i(LogDebug.DAILY_JOB_POS_TEK_DOWNL, "POSITIVE "+ key + " Occ "+numberOfOccur);

                    if(numberOfOccur >= TemporaryExposureKeys.DANGER_CONTACT_THRESHOLD){
                        //setting the health status to 100
                        sumRisk = 100;
                        break;
                    }else{
                        sumRisk += numberOfOccur * TemporaryExposureKeys.DANGER_CONTACT_SINGLE_VALUE;
                    }
                }

                Log.i(LogDebug.DAILY_JOB_POS_TEK_DOWNL, "TOTAL RISK " + sumRisk);

                //Creazione della notifica da inviare.
                Notification n = new Notification();
                n.setNotificationType(NotificationHelper.CONTACT_RISK_NOTIFICATION);
                NotificationHelper.setTitleAndDescription(n,context);

                if(sumRisk == 100) {
                    //Se il rischio è 100%, aggiornamento diretto del campo.
                    Utenti.updateFields(AuthHelper.getUserId(), isSucc -> {
                        //Invio della notifica.
                        long id = NotificationHelper.saveAndAlertNotification(context,n);
                        NotificationHelper.sendNotification(context,n,R.drawable.red_status_icon,id);
                    },Utenti.STATUS_SANITARIO_FIELD,100);
                }else if(sumRisk != 0){
                    //Se il rischio è minore del 100%, sommare il rischio calcolato con quello gia presente nel database.
                    Utenti.sumValueToHealthStatus(sumRisk, isSuccess -> {
                        //Invio della notifica.
                        long id = NotificationHelper.saveAndAlertNotification(context,n);
                        NotificationHelper.sendNotification(context,n,R.drawable.red_status_icon,id);
                    });
                }
            }else{
                Log.i(LogDebug.DAILY_JOB_POS_TEK_DOWNL, "NOTHING DOWNLOADED");
            }
        });
    }

    /**
     * Metodo statico da usare per programmare il task giornaliero.
     * Eseguito mediante l'uso dell'AlarmManager
     *
     * @param context contesto della applicazione.
     */
    public static final void scheduleDailyTask(Context context){
        Intent intent = new Intent(context, DailyJobReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, DAILY_JOB_REQUEST_CODE, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        /**
         * Impostazione di un allarme usando setInexactRepeating in quanto non ci interessa
         * l'esecuzione in un determinato momento della giornata e anche per aumentare la randomess nelle richieste al server
         * di tutti i task giornalieri di tutti gli utenti.
         */
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);
        Log.i(LogDebug.DAILY_JOB,"Daily task scheduled!");
    }

    /**
     * Metodo static da usare per cancellare la programmazione del task giornaliero.
     *
     * @param context contesto dell'applicazione
     */
    public static final void removeDailyTask(Context context){
        Intent intent = new Intent(context, DailyJobReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, DAILY_JOB_REQUEST_CODE, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
