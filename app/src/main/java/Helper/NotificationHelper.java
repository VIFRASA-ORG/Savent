package Helper;

import android.content.Context;

import com.vitandreasorino.savent.R;

import java.util.Map;

import Model.Pojo.Notification;

public class NotificationHelper {

    public static final String QUEUE_CLIMBED_NOTIFICATION = "queueClimbed";

    public static final Notification getNotificationFromPayload(Context context, Map<String, String> payloadData){

        Notification n = new Notification();
        switch (payloadData.get("notificationType")){
            case QUEUE_CLIMBED_NOTIFICATION:
                n.setTitle(context.getResources().getStringArray(R.array.queueClimbed)[0] + " \"" + payloadData.get("eventName") + "\"");
                n.setDescription(context.getResources().getStringArray(R.array.queueClimbed)[1]);
                break;
        }
        return n;
    }

}
