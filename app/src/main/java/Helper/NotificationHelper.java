package Helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.vitandreasorino.savent.R;
import com.vitandreasorino.savent.Utenti.Notification.NotificationActivity;
import java.util.Calendar;
import java.util.Map;
import Helper.LocalStorage.SQLiteHelper;
import Model.POJO.Notification;

/**
 * Classe Helper con tutti i metodi per gestire, creare e inviare le notifiche
 * all'utente anche quando l'app non è in esecuzione.
 */
public class NotificationHelper {

    /**
     * COSTANTI CHE INDICANO I TIPI DI NOTIFICHE CHE È
     * POSSIBILE RICEVERE DA FIREBASE.
     *
     * CORRISPONDONO AL CAMPO TYPE PRESENTE NEL PAYLOAD DELLE
     * NOTIFICHE RICEVUTE.
     */
    public static final String QUEUE_CLIMBED_NOTIFICATION = "queueClimbed";
    public static final String NEW_GROUP_NOTIFICATION = "addedToGroup";
    public static final String EVENT_DELETED_NOTIFICATION = "eventDeleted";
    public static final String CONTACT_RISK_NOTIFICATION = "contactRisk";


    /**
     * Metodo che, dato il payload ricevuto dalla notifica di Firebase (remoteMessage.getData()),
     * compila un oggetto di POJO di classe Notification gestibile
     * dall'applicazione.
     *
     * @param context il contesto dell'applicazione o dell'activity chiamante.
     * @param payloadData payload riceuto insieme alla notifica (remoteMessage.getData())
     * @return oggetto di classe Notification con tutte le informazioni ricevute dalla notifica di Firebase.
     */
    public static final Notification getNotificationFromPayload(Context context, Map<String, String> payloadData){

        Notification n = new Notification();
        Calendar c = Calendar.getInstance();

        if(payloadData.containsKey(Notification.NOTIFICATION_TYPE)){
            n.setNotificationType(payloadData.get(Notification.NOTIFICATION_TYPE));
            switch (payloadData.get(Notification.NOTIFICATION_TYPE)){
                case QUEUE_CLIMBED_NOTIFICATION:
                case EVENT_DELETED_NOTIFICATION:
                    if(payloadData.containsKey(Notification.EVENT_NAME)) n.setEventName(payloadData.get(Notification.EVENT_NAME));
                    if(payloadData.containsKey(Notification.EVENT_ID)) n.setEventId(payloadData.get(Notification.EVENT_ID));

                    c = Calendar.getInstance();
                    n.setDate(c);
                    break;

                case NEW_GROUP_NOTIFICATION:
                    if(payloadData.containsKey(Notification.GROUP_ID)) n.setGroupId(payloadData.get(Notification.GROUP_ID));
                    if(payloadData.containsKey(Notification.GROUP_NAME)) n.setGroupName(payloadData.get(Notification.GROUP_NAME));

                    c = Calendar.getInstance();
                    n.setDate(c);
                    break;

            }
        }

        setTitleAndDescription(n,context);
        return n;
    }

    /**
     * Metodo che compila i cambi "Title" e "description" dell'oggetto Notification passato come paramentro.
     * I due campi sono compilati in base al notificationType presente nell'oggetto Notification.
     * Le stringhe vengono prese dalle risorse in maniera tale da rendere le notifiche multilingua
     * in base alla lingua del sistema.
     *
     * @param n oggetto Notification in cui compilare i campi "Title" e "Notification".
     * @param context
     */
    public static final void setTitleAndDescription(Notification n,Context context){
        switch (n.getNotificationType()){
            case QUEUE_CLIMBED_NOTIFICATION:
                n.setTitle(context.getResources().getStringArray(R.array.queueClimbed)[0] + " \"" + n.getEventName() + "\"!");
                n.setDescription(context.getResources().getStringArray(R.array.queueClimbed)[1]);
                break;
            case NEW_GROUP_NOTIFICATION:
                n.setDescription(context.getResources().getStringArray(R.array.addedToGroup)[1]+ " \"" + n.getGroupName() + "\"!");
                n.setTitle(context.getResources().getStringArray(R.array.addedToGroup)[0]);
                break;
            case EVENT_DELETED_NOTIFICATION:
                n.setDescription( " \"" + n.getEventName() + "\" " + context.getResources().getStringArray(R.array.eventDeleted)[1]);
                n.setTitle(context.getResources().getStringArray(R.array.eventDeleted)[0]);
                break;
            case CONTACT_RISK_NOTIFICATION:
                n.setDescription(context.getResources().getStringArray(R.array.contactRisk)[1]);
                n.setTitle(context.getResources().getStringArray(R.array.contactRisk)[0]);
                break;
        }
    }

    /**
     * Metodo che crea e invia una notifica al sistema operativo,
     * notifica basata sui dati ricevuti da Firebase.
     *
     * @param context contesto dell'applicazione o dell'activity chiamante.
     * @param n oggetto della notifica da inviare all'utente.
     * @param icon icona da visualizzare nella notifica.
     * @param id id della notifica da inviare.
     */
    public static void sendNotification(Context context, Model.POJO.Notification n, @Nullable Integer icon, long id) {
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(NotificationActivity.FROM_NOTIFICATION_INTENT,true);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "ChannelId";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.app_icon_no_back)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        (icon == null) ? R.drawable.app_icon_no_back : icon ))
                .setContentTitle(n.getTitle())
                .setContentText(n.getDescription())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setDefaults(android.app.Notification.BADGE_ICON_LARGE)
                .setContentIntent(pendingIntent)
                .setColorized((icon != null))
                .setColor((icon == null ) ? Color.TRANSPARENT : Color.RED);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify((int) id /* ID of notification */, notificationBuilder.build());
    }

    /**
     * Metodo che salva la notifica nella tabella Notifiche del database SQLite.
     * Inoltre invia un messaggio di broadcast locale per notificare tutti gli activity
     * iteressati dell'arrivo di una notifica.
     * Questo viene fatto nel caso la notifica arrivi mentre l'app è in esecuzione
     * per aggiornare il numero di notifiche non lette nella tab bar.
     *
     * @param context contesto dell'activity chiamante.
     * @param n oggetto notification da inviare.
     * @return ritorna l'id generato nel database locale SQLite.
     */
    public static long saveAndAlertNotification(Context context, Notification n){
        //Insert the notification into the database
        SQLiteHelper databaseHelper = new SQLiteHelper(context);
        long id = databaseHelper.insertNewNotification(n);

        //Sending the broadcast message to update the notification number
        Intent broadcast = new Intent("UpdateNotification");
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);

        return id;
    }

}
