package Helper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import Model.Pojo.Notification;


public class SQLiteHelper extends SQLiteOpenHelper {

    public final class SaventContract {


        private SaventContract() {
        }

        public static final String DATABASE_NAME = "Savent.db";

        /* Inner class che definisce la tabella TemporaryExposureKeys */
        public class TemporaryExposureKeys implements BaseColumns {
            public static final String TABLE_NAME = "TemporaryExposureKeys";
            public static final String COLUMN_NAME_CODICI = "Key";
            public static final String COLUMN_NAME_DATA_CREAZIONE = "generationTime";
        }

        /* Inner class che definisce la tabella ContattiAvvenuti */
        public class ContattiAvvenuti implements BaseColumns {
            public static final String TABLE_NAME = "ContattiAvvenuti";
            public static final String COLUMN_NAME_CODICI = "Codici";
            public static final String COLUMN_NAME_DATA_CONTATTO = "DataContatto";
        }

        /* Inner class che definisce la tabella ContattiAvvenuti */
        public class Notifiche implements BaseColumns {
            public static final String TABLE_NAME = "Notifiche";
            public static final String COLUMN_NAME_NOTIFICATION_TYPE = "NotificationType";
            public static final String COLUMN_NAME_EVENT_ID = "EventId";
            public static final String COLUMN_NAME_EVENT_NAME = "EventName";
            public static final String COLUMN_NAME_DATE = "Date";
            public static final String COLUMN_NAME_READ = "Read";

            public static final String COLUMN_NAME_GROUP_ID = "GroupId";
            public static final String COLUMN_NAME_GROUP_NAME = "GroupName";
        }
    }

    public static final int DISTANCE_TIME_CONTACT = 15;
    public static final int DATABASE_VERSION = 3;

    /* Costruttore della classe SQLiteHelper */
    public SQLiteHelper(Context context) {
        super(context, SaventContract.DATABASE_NAME, null, DATABASE_VERSION);
    }


    /* Creazione delle due tabelle MieiCodici, ContattiAvvenuti */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + SaventContract.TemporaryExposureKeys.TABLE_NAME + "(" + SaventContract.TemporaryExposureKeys.COLUMN_NAME_CODICI +
                   " TEXT PRIMARY KEY, " + SaventContract.TemporaryExposureKeys.COLUMN_NAME_DATA_CREAZIONE + " TEXT)");

        db.execSQL("CREATE TABLE " + SaventContract.ContattiAvvenuti.TABLE_NAME + "(" + SaventContract.ContattiAvvenuti._ID +
                   " INTEGER PRIMARY KEY AUTOINCREMENT, " + SaventContract.ContattiAvvenuti.COLUMN_NAME_CODICI + " TEXT, " +
                SaventContract.ContattiAvvenuti.COLUMN_NAME_DATA_CONTATTO +  " TEXT )" );

        db.execSQL("CREATE TABLE " + SaventContract.Notifiche.TABLE_NAME + "(" + SaventContract.Notifiche._ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + SaventContract.Notifiche.COLUMN_NAME_NOTIFICATION_TYPE + " TEXT, " +
                SaventContract.Notifiche.COLUMN_NAME_EVENT_ID + " TEXT, " + SaventContract.Notifiche.COLUMN_NAME_EVENT_NAME + " TEXT, " +
                SaventContract.Notifiche.COLUMN_NAME_DATE + " TEXT, " +
                SaventContract.Notifiche.COLUMN_NAME_READ + " INTEGER," +
                SaventContract.Notifiche.COLUMN_NAME_GROUP_ID + " TEXT," +
                SaventContract.Notifiche.COLUMN_NAME_GROUP_NAME + " TEXT )");
    }


    /* Drop delle tabelle */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SaventContract.TemporaryExposureKeys.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SaventContract.ContattiAvvenuti.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SaventContract.Notifiche.TABLE_NAME);
        this.onCreate(db);
    }

    /**
     * Esegue la query per avere il numero di notifiche non lette
     *
     * @return il numero di notifiche non lette
     */
    public int getNumberOfUnreadNotification() {
        SQLiteDatabase databaseSQLite = this.getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM " + SaventContract.Notifiche.TABLE_NAME + " WHERE " + SaventContract.Notifiche.COLUMN_NAME_READ + "= 0" ;
        SQLiteStatement statement = databaseSQLite.compileStatement(sql);
        int count = (int) statement.simpleQueryForLong();
        return count;
    }

    /**
     * Imposta il flag della notifica a vero per identificare che la stessa è stata letta dall'utente.
     *
     * @param notificationId l'id della notifica che è stata letta
     */
    public void readANotification(int notificationId){
        SQLiteDatabase databaseSQLite = this.getWritableDatabase();
        ContentValues data = new ContentValues();
        data.put(SaventContract.Notifiche.COLUMN_NAME_READ,1);
        databaseSQLite.update(SaventContract.Notifiche.TABLE_NAME,data,"" + SaventContract.Notifiche._ID + " = " + notificationId,null);
    }

    /**
     * Permette di salvare sul database SQLite una nuova notifica
     *
     * @param n l'oggetto della notifica da salvare
     */
    public void insertNewNotification(Notification n){
        SQLiteDatabase databaseSQLite = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SaventContract.Notifiche.COLUMN_NAME_NOTIFICATION_TYPE, n.getNotificationType());
        contentValues.put(SaventContract.Notifiche.COLUMN_NAME_EVENT_ID, n.getEventId());
        contentValues.put(SaventContract.Notifiche.COLUMN_NAME_EVENT_NAME, n.getEventName());

        contentValues.put(SaventContract.Notifiche.COLUMN_NAME_GROUP_ID, n.getGroupId());
        contentValues.put(SaventContract.Notifiche.COLUMN_NAME_GROUP_NAME, n.getGroupName());

        if(n.getDate() != null) contentValues.put(SaventContract.Notifiche.COLUMN_NAME_DATE, n.getDate().getTimeInMillis());
        else contentValues.put(SaventContract.Notifiche.COLUMN_NAME_DATE, "");

        contentValues.put(SaventContract.Notifiche.COLUMN_NAME_READ, n.isRead() ? 1 : 0);
        databaseSQLite.insert(SaventContract.Notifiche.TABLE_NAME, null, contentValues);
    }

    /**
     * Permette di cancellare da SQLite una notifica.
     *
     * @param notificationId l'id della notifica da cancellare.
     */
    public void deleteNotification(int notificationId){
        SQLiteDatabase databaseSQLite = this.getWritableDatabase();
        databaseSQLite.delete(SaventContract.Notifiche.TABLE_NAME, SaventContract.Notifiche._ID + " = ?", new String[] {""+notificationId});
    }

    /**
     * Ritorna una lista di tutte le notifiche presenti nel database SQLite.
     *
     * @param context il contesto di utilizzo
     * @return una List<Notification> contenente tutte le notifiche presenti
     */
    public List<Notification> getAllNotificaton(Context context){
        SQLiteDatabase databaseSQLite = this.getReadableDatabase();
        ArrayList<Notification> notifications = new ArrayList<>();

        String query = "SELECT * FROM " + SaventContract.Notifiche.TABLE_NAME + " ORDER BY " + SaventContract.Notifiche.COLUMN_NAME_DATE + " DESC";
        Cursor cursor = databaseSQLite.rawQuery(query,null);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Notification n = new Notification();
            n.setNotificationType(cursor.getString(cursor.getColumnIndexOrThrow(SaventContract.Notifiche.COLUMN_NAME_NOTIFICATION_TYPE)));
            n.setEventName(cursor.getString(cursor.getColumnIndexOrThrow(SaventContract.Notifiche.COLUMN_NAME_EVENT_NAME)));
            n.setEventId(cursor.getString(cursor.getColumnIndexOrThrow(SaventContract.Notifiche.COLUMN_NAME_EVENT_ID)));
            n.setGroupName(cursor.getString(cursor.getColumnIndexOrThrow(SaventContract.Notifiche.COLUMN_NAME_GROUP_NAME)));
            n.setGroupId(cursor.getString(cursor.getColumnIndexOrThrow(SaventContract.Notifiche.COLUMN_NAME_GROUP_ID)));
            n.setRead(cursor.getInt(cursor.getColumnIndexOrThrow(SaventContract.Notifiche.COLUMN_NAME_READ)) == 0 ? false : true);
            n.setId(cursor.getInt(cursor.getColumnIndexOrThrow(SaventContract.Notifiche._ID)));

            NotificationHelper.setTitleAndDescription(n,context);

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(SaventContract.Notifiche.COLUMN_NAME_DATE))));
            n.setDate(c);

            notifications.add(n);
        }

        return notifications;
    }

    /**
     * Ritorna l'ultima Temporary Exposure Key memorizzata all'interno della tabella TemporaryExposureKeys
     *
     * @return l'ultima TEK se esiste, null altrimenti.
     */
    public String getLastTek(){
        SQLiteDatabase databaseSQLite = this.getReadableDatabase();

        String query = "SELECT * FROM " + SaventContract.TemporaryExposureKeys.TABLE_NAME + " ORDER BY " + SaventContract.TemporaryExposureKeys.COLUMN_NAME_DATA_CREAZIONE + " LIMIT 1" ;
        Cursor cursor = databaseSQLite.rawQuery(query,null);

        if(cursor.moveToFirst()){
            return cursor.getString(cursor.getColumnIndexOrThrow(SaventContract.TemporaryExposureKeys.COLUMN_NAME_CODICI));
        }else return null;
    }

    /**
     * Inserimento dei campi nella tabella MieiCodici
     * @param codice
     * @param dataCreazione
     */
    public void insertNewTek(String codice, Calendar dataCreazione) {
        SQLiteDatabase databaseSQLite = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SaventContract.TemporaryExposureKeys.COLUMN_NAME_CODICI, codice);
        contentValues.put(SaventContract.TemporaryExposureKeys.COLUMN_NAME_DATA_CREAZIONE, dataCreazione.getTimeInMillis());
        databaseSQLite.insert(SaventContract.TemporaryExposureKeys.TABLE_NAME, null, contentValues);
    }

    /**
     * Inserimento dei campi nella tabella ContattiAvvenuti
     * @param codice
     * @param dataAvvenutoContatto
     */
    public void insertContattiAvvenuti(String codice, Calendar dataAvvenutoContatto) {
        SQLiteDatabase databaseSQLite = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SaventContract.ContattiAvvenuti.COLUMN_NAME_CODICI, codice);
        contentValues.put(SaventContract.ContattiAvvenuti.COLUMN_NAME_DATA_CONTATTO, dataAvvenutoContatto.getTimeInMillis());
        databaseSQLite.insert(SaventContract.ContattiAvvenuti.TABLE_NAME, null, contentValues);
    }


    /**
     * Cancellazione delle tuple ogni 15 giorni dalla tabella MieiCodici
     */
    public void deleteExpiredTek() {
        SQLiteDatabase databaseSQLite = this.getWritableDatabase();
        Calendar currentTime = Calendar.getInstance();
        // sottrae alla data corrente 15 giorni, in modo tale da usare questo oggetto currentTime all'interno della query
        currentTime.add(Calendar.DAY_OF_MONTH, -DISTANCE_TIME_CONTACT);
        databaseSQLite.delete(SaventContract.TemporaryExposureKeys.TABLE_NAME, SaventContract.TemporaryExposureKeys.COLUMN_NAME_DATA_CREAZIONE + " < ?", new String[] {"" + currentTime.getTimeInMillis()});
    }

    /**
     * Cancellazione delle tuple ogni 15 giorni dalla tabella ContattiAvvenuti
     */
    public void deleteExpiredContact() {
        SQLiteDatabase databaseSQLite = this.getWritableDatabase();
        Calendar currentTime = Calendar.getInstance();
        // sottrae alla data corrente 15 giorni, in modo tale da usare questo oggetto currentTime all'interno della query
        currentTime.add(Calendar.DAY_OF_MONTH, -DISTANCE_TIME_CONTACT);
        databaseSQLite.delete(SaventContract.ContattiAvvenuti.TABLE_NAME,SaventContract.ContattiAvvenuti.COLUMN_NAME_DATA_CONTATTO + " < ?", new String[] {"" + currentTime.getTimeInMillis()});
    }


    /**
     * Permette di controllare la presenza dei codici all'interno della tabella ContattiAvvenuti
     * @param codici
     * @return il numero di tuple trovate
     */
    public int controlloContattiAvvenuti(ArrayList<String> codici) {

        SQLiteDatabase databaseSQLite = this.getReadableDatabase();
        String finalIn = "(";
        for(String codice : codici) {
            finalIn += codice + ",";
        }

        // la stringa finalIn permette di ricreare la condizione all'interno del WHERE IN
        finalIn = finalIn.substring(0,  finalIn.length()-1);
        finalIn += ")";
        String queryControlloContatti = "SELECT * FROM " + SaventContract.ContattiAvvenuti.TABLE_NAME +
                                        " WHERE "+ SaventContract.ContattiAvvenuti.COLUMN_NAME_CODICI + " IN " + finalIn;

        Cursor cursore = databaseSQLite.rawQuery(queryControlloContatti,null);
        return  cursore.getCount();
    }



}
