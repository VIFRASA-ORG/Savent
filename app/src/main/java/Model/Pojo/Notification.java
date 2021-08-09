package Model.Pojo;

import java.util.Calendar;

public class Notification {

    public static final String NOTIFICATION_TYPE = "notificationType";
    public static final String DATE = "date";
    public static final String EVENT_NAME = "eventName";
    public static final String EVENT_ID = "eventId";

    public static final String GROUP_NAME = "groupName";
    public static final String GROUP_ID = "groupId";


    private int id;
    private String title = "";
    private String description = "";
    private String notificationType = "";
    private Calendar date = null;
    private boolean isRead = false;

    private String eventId = "";
    private String eventName = "";

    private String groupId = "";
    private String groupName = "";


    public Notification() {}



    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
