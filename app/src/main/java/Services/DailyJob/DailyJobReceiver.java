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
 * Receiver for the Daily task.
 * The daily task has to download the new TEK, remove all the expired tek,
 * download the new positive, notify the user if someone resulted positive
 * and update the health status of the logged in user.
 */
public class DailyJobReceiver extends BroadcastReceiver {

    private static final int DAILY_JOB_REQUEST_CODE = 123;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LogDebug.DAILY_JOB, "Daily task running right now");

        allWorkToDo(context);
    }

    /**
     * Method with alla the taks operation.
     * @param context
     */
    private void allWorkToDo(Context context){

        //Downloading the new TEK
        TemporaryExposureKeys.generateNewTEK(context, null);

        //Deleting tek and contacts older than 15 days
        SQLiteHelper db = new SQLiteHelper(context);
        db.deleteExpiredContact();
        db.deleteExpiredTek();

        //Trying the code
        Date latestUpdatePositive = SharedPreferencesHelper.getLastPositiveTekUpdateTime(context);

        //Download the positive tek from the latest update if exists, otherwise all the tek will be downloaded
        TemporaryExposureKeys.downloadPositiveTek(latestUpdatePositive, positiveTekList ->{

            if(positiveTekList != null){
                Log.i(LogDebug.DAILY_JOB_POS_TEK_DOWNL, "DOWNLOADED "+ positiveTekList.size());
                //Save the latest update time
                SharedPreferencesHelper.setLastPositiveTekUpdateTime(context,new Date());

                //Save the new tek to the databse
                db.addPositiveTek(positiveTekList);

                //Check if we have some match and calculate the risk
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

                //Preparing the notification
                Notification n = new Notification();
                n.setNotificationType(NotificationHelper.CONTACT_RISK_NOTIFICATION);
                NotificationHelper.setTitleAndDescription(n,context);

                if(sumRisk == 100) {
                    //If the risk is 100%, directily update the field.
                    Utenti.updateFields(AuthHelper.getUserId(), isSucc -> {
                        //Send notification
                        long id = NotificationHelper.saveAndAlertNotification(context,n);
                        NotificationHelper.sendNotification(context,n,R.drawable.red_status_icon,id);
                    },Utenti.STATUS_SANITARIO_FIELD,100);
                }else if(sumRisk != 0){
                    //If the risk is below 100%, sum the calculated risk to the one on the database.
                    Utenti.sumValueToHealthStatus(sumRisk, isSuccess -> {
                        //Send notification
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
     * Static method used to schedule the task
     *
     * @param context the context.
     */
    public static final void scheduleDailyTask(Context context){
        Intent intent = new Intent(context, DailyJobReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, DAILY_JOB_REQUEST_CODE, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);
        Log.i(LogDebug.DAILY_JOB,"Daily task scheduled!");
    }

    /**
     * Static method used to cancel the repeating task.
     * @param context
     */
    public static final void removeDailyTask(Context context){
        Intent intent = new Intent(context, DailyJobReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, DAILY_JOB_REQUEST_CODE, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
