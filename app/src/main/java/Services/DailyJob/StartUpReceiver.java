package Services.DailyJob;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Classe derivata da BroadcastReceiver da utilizzare per riprogrammare
 * il task giornaliero nel caso in cui il dispositivo venga riavviato.
 *
 * Questo deve essere fatto perche tutti gli allarmi creati dall'AlarmManager
 * vengono cancellati al riavvio.
 *
 * Creare l'intent filter android.intent.action.BOOT_COMPLETED nel Manifest per
 * far funzionare il seguente Receiver.
 */
public class StartUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        //Invocazione del metodo per riprogrammare il task giornaliero.
        DailyJobReceiver.scheduleDailyTask(context);
    }
}
