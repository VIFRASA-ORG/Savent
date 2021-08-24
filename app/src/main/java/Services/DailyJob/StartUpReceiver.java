package Services.DailyJob;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Broadcast receiver invoked when the system is rebooted.
 * It reschedule the daily task, since all the alarm are wiped after a reboot.
 */
public class StartUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DailyJobReceiver.scheduleDailyTask(context);
    }
}
