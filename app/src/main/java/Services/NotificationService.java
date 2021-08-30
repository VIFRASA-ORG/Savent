package Services;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import Helper.NotificationHelper;
import Model.DB.Utenti;
import Model.LogDebug;


/**
 * Classe derivata da FirebaseMessagingService per ricevere le notifiche
 * dal Firebase Cloud Messaging.
 */
public class NotificationService extends FirebaseMessagingService {



    /**
     * Ci sono due casi in cui onNewToken viene invocato:
     * 1) Quando un nuovo token viene generato allo sturt up dell'applicazione iniziale;
     * 2) Tutte le volte in cui il token si aggiorna.
     *
     * Per il secondo caso, ci sono tre scenari che causano l'aggiornamento del token:
     * 1) L'applicazione viene ripristinata su un altro device.
     * 2) L'utente disinstalla/reinstalla l'applicazione.
     * 3) Utente cancella i dati dell'app.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(LogDebug.FIREBASE_NOTIFICATION, "Refreshed token: " + token);

        //Invio al server firebase il nuovo token relativo all'utente loggato.
        Utenti.setMessagingToken(token,null);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(LogDebug.FIREBASE_NOTIFICATION, "From: " + remoteMessage.getFrom());

        // Controllo che il payload contenga dei dati.
        if (remoteMessage.getData().size() > 0) {
            Log.d(LogDebug.FIREBASE_NOTIFICATION, "Message data payload: " + remoteMessage.getData());

            //Creazione di un oggetto di calsse Notification con le carateristiche presenti nel payload ricevuto
            Model.Pojo.Notification n = NotificationHelper.getNotificationFromPayload(this,remoteMessage.getData());

            /**
             * Salvataggio della notifica all'interno del database SQLite con
             * invio dei broadcast nel caso l'applicazione sia in esecuzione
             * per aggiornare l'interfaccia grafica.
             */
            long id = NotificationHelper.saveAndAlertNotification(this,n);

            //Invio della notifica all'utente.
            NotificationHelper.sendNotification(this,n,null,id);
        }
    }
}
