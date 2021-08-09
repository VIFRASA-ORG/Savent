package Helper;

import android.content.Context;

import com.vitandreasorino.savent.R;

import java.util.Calendar;
import java.util.Map;

import Model.Pojo.Notification;

public class NotificationHelper {

    public static final String QUEUE_CLIMBED_NOTIFICATION = "queueClimbed";
    public static final String NEW_GROUP_NOTIFICATION = "addedToGroup";
    public static final String EVENT_DELETED_NOTIFICATION = "eventDeleted";

    /**
     * Convert the Map given from the notification, into a Notification object.
     *
     * @param context the context
     * @param payloadData the data coming from the notification
     * @return  a Notification object with all the information of the notification.
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
     * Set the notification title and descrption based to the notification type
     *
     * @param n the notification object
     * @param context the context
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
        }
    }

}
