package Helper;

import android.content.Context;

import com.vitandreasorino.savent.R;

import java.util.Calendar;
import java.util.Map;

import Model.Pojo.Notification;

public class NotificationHelper {

    public static final String QUEUE_CLIMBED_NOTIFICATION = "queueClimbed";

    /**
     * Convert the Map given from the notification, into a Notification object.
     *
     * @param context the context
     * @param payloadData the data coming from the notification
     * @return  a Notification object with all the information of the notification.
     */
    public static final Notification getNotificationFromPayload(Context context, Map<String, String> payloadData){

        Notification n = new Notification();

        if(payloadData.containsKey(Notification.NOTIFICATION_TYPE)){
            switch (payloadData.get(Notification.NOTIFICATION_TYPE)){
                case QUEUE_CLIMBED_NOTIFICATION:

                    if(payloadData.containsKey(Notification.EVENT_NAME))
                        n.setEventName(payloadData.get(Notification.EVENT_NAME));
                        n.setTitle(context.getResources().getStringArray(R.array.queueClimbed)[0] + " \"" + payloadData.get(Notification.EVENT_NAME) + "\"!");

                    if(payloadData.containsKey(Notification.EVENT_ID))
                        n.setEventId(payloadData.get(Notification.EVENT_ID));

                    Calendar c = Calendar.getInstance();
                    n.setDate(c);
                    n.setDescription(context.getResources().getStringArray(R.array.queueClimbed)[1]);

                    break;
            }
        }

        return n;
    }

}
