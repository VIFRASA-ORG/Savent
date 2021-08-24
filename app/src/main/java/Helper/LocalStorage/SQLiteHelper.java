package Helper.LocalStorage;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Helper.NotificationHelper;
import Model.Pojo.Notification;
import Model.Pojo.TemporaryExposureKey;


/**
 * Helper class with all the method to create, update and drop all the
 * SQLite tables needed by the application.
 * It contain also all the needed query to manage the data inside the tables.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    public final class SaventContract {

        private SaventContract() { }

        private static final String DATABASE_NAME = "Savent.db";

        /* Inner class che definisce la tabella TemporaryExposureKeys */
        private class TemporaryExposureKeys implements BaseColumns {
            public static final String TABLE_NAME = "TemporaryExposureKeys";
            public static final String COLUMN_NAME_CODICI = "Tek";
            public static final String COLUMN_NAME_DATA = "date";
        }

        /* Inner class che definisce la tabella ContattiAvvenuti */
        private class ContattiAvvenuti implements BaseColumns {
            public static final String TABLE_NAME = "ContattiAvvenuti";
            public static final String COLUMN_NAME_CODICI = "Codici";
            public static final String COLUMN_NAME_DATA_CONTATTO = "DataContatto";
        }

        /* Inner class che definisce la tabella ContattiAvvenuti */
        private class Notifiche implements BaseColumns {
            public static final String TABLE_NAME = "Notifiche";
            public static final String COLUMN_NAME_NOTIFICATION_TYPE = "NotificationType";
            public static final String COLUMN_NAME_EVENT_ID = "EventId";
            public static final String COLUMN_NAME_EVENT_NAME = "EventName";
            public static final String COLUMN_NAME_DATE = "Date";
            public static final String COLUMN_NAME_READ = "Read";

            public static final String COLUMN_NAME_GROUP_ID = "GroupId";
            public static final String COLUMN_NAME_GROUP_NAME = "GroupName";
        }

        /* Inner class che definisce la tabella TekPositivi */
        private class TekPositivi implements BaseColumns{
            public static final String TABLE_NAME = "TekPositivi";
            public static final String COLUMN_NAME_CODICI = "Tek";
            public static final String COLUMN_NAME_DATA = "data";
        }
    }

    public static final int DISTANCE_TIME_CONTACT = 15;
    public static final int DATABASE_VERSION = 4;

    /* Costruttore della classe SQLiteHelper */
    public SQLiteHelper(Context context) {
        super(context, SaventContract.DATABASE_NAME, null, DATABASE_VERSION);
    }


    /* Creazione delle due tabelle MieiCodici, ContattiAvvenuti */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + SaventContract.TemporaryExposureKeys.TABLE_NAME + "(" + SaventContract.TemporaryExposureKeys.COLUMN_NAME_CODICI +
                   " TEXT PRIMARY KEY, " + SaventContract.TemporaryExposureKeys.COLUMN_NAME_DATA + " TEXT)");

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

        db.execSQL("CREATE TABLE " + SaventContract.TekPositivi.TABLE_NAME + "(" + SaventContract.TekPositivi.COLUMN_NAME_CODICI +
                " TEXT PRIMARY KEY, " + SaventContract.TekPositivi.COLUMN_NAME_DATA + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SaventContract.TemporaryExposureKeys.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SaventContract.ContattiAvvenuti.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SaventContract.Notifiche.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SaventContract.TekPositivi.TABLE_NAME);
        this.onCreate(db);
    }

    /**
     * funzione per eseguire il drop delle tabelle
     */
    public void dropDatabase(){
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + SaventContract.TemporaryExposureKeys.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SaventContract.ContattiAvvenuti.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SaventContract.Notifiche.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SaventContract.TekPositivi.TABLE_NAME);
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
        databaseSQLite.close();
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
        databaseSQLite.close();
    }

    /**
     * Permette di salvare sul database SQLite una nuova notifica
     *
     * @param n l'oggetto della notifica da salvare
     */
    public long insertNewNotification(Notification n){
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
        long id = databaseSQLite.insert(SaventContract.Notifiche.TABLE_NAME, null, contentValues);
        databaseSQLite.close();
        return id;
    }

    /**
     * Permette di cancellare da SQLite una notifica.
     *
     * @param notificationId l'id della notifica da cancellare.
     */
    public void deleteNotification(int notificationId){
        SQLiteDatabase databaseSQLite = this.getWritableDatabase();
        databaseSQLite.delete(SaventContract.Notifiche.TABLE_NAME, SaventContract.Notifiche._ID + " = ?", new String[] {""+notificationId});
        databaseSQLite.close();
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
        databaseSQLite.close();
        return notifications;
    }

    /**
     * Ritorna l'ultima Temporary Exposure Key memorizzata all'interno della tabella TemporaryExposureKeys
     *
     * @return l'ultima TEK se esiste, null altrimenti.
     */
    public String getLastTek(){
        SQLiteDatabase databaseSQLite = this.getReadableDatabase();

        String query = "SELECT * FROM " + SaventContract.TemporaryExposureKeys.TABLE_NAME + " ORDER BY " + SaventContract.TemporaryExposureKeys.COLUMN_NAME_DATA + " LIMIT 1" ;
        Cursor cursor = databaseSQLite.rawQuery(query,null);

        if(cursor.moveToFirst()){
            String res = cursor.getString(cursor.getColumnIndexOrThrow(SaventContract.TemporaryExposureKeys.COLUMN_NAME_CODICI));
            databaseSQLite.close();
            return res;
        }else{
            databaseSQLite.close();
            return null;
        }
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
        contentValues.put(SaventContract.TemporaryExposureKeys.COLUMN_NAME_DATA, dataCreazione.getTimeInMillis());
        databaseSQLite.insert(SaventContract.TemporaryExposureKeys.TABLE_NAME, null, contentValues);
        databaseSQLite.close();
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
        databaseSQLite.close();
    }


    /**
     * Cancellazione delle tuple ogni 15 giorni dalla tabella MieiCodici
     */
    public void deleteExpiredTek() {
        SQLiteDatabase databaseSQLite = this.getWritableDatabase();
        Calendar currentTime = Calendar.getInstance();
        // sottrae alla data corrente 15 giorni, in modo tale da usare questo oggetto currentTime all'interno della query
        currentTime.add(Calendar.DAY_OF_MONTH, -DISTANCE_TIME_CONTACT);
        databaseSQLite.delete(SaventContract.TemporaryExposureKeys.TABLE_NAME, SaventContract.TemporaryExposureKeys.COLUMN_NAME_DATA + " < ?", new String[] {"" + currentTime.getTimeInMillis()});
        databaseSQLite.close();
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
        databaseSQLite.close();
    }


    /**
     * Permette di controllare la presenza dei codici all'interno della tabella ContattiAvvenuti
     * @param codici
     * @return il numero di tuple trovate
     */
    public int controlloContattiAvvenuti(ArrayList<String> codici) {

        SQLiteDatabase databaseSQLite = this.getReadableDatabase();
        String finalIn = "";
        for(String codice : codici) {
            finalIn += "\""+codice + "\",";
        }

        // la stringa finalIn permette di ricreare la condizione all'interno del WHERE IN
        finalIn = finalIn.substring(0,  finalIn.length()-1);
        String queryControlloContatti = "SELECT * FROM " + SaventContract.ContattiAvvenuti.TABLE_NAME +
                                        " WHERE "+ SaventContract.ContattiAvvenuti.COLUMN_NAME_CODICI + " IN (" + finalIn + ")";

        Cursor cursore = databaseSQLite.rawQuery(queryControlloContatti,null);
        int count = cursore.getCount();
        databaseSQLite.close();
        return count;
    }

    /**
     * Lettura dei Miei Codici presenti nel database locale "Savent.db"
     * @return arrayMieiCodici, contenente tutti i codici presenti nella tabella "MieiCodici"
     */
    public ArrayList<TemporaryExposureKey> letturaMieiCodici() {
        ArrayList<TemporaryExposureKey> arrayMieiCodici = new ArrayList<TemporaryExposureKey>();
        SQLiteDatabase databaseSQLite = this.getReadableDatabase();
        String queryLettura = "SELECT " + SaventContract.TemporaryExposureKeys.COLUMN_NAME_CODICI + " FROM " + SaventContract.TemporaryExposureKeys.TABLE_NAME;
        Cursor cursore = databaseSQLite.rawQuery(queryLettura,null);

        /* Controllo se il cursore è posizionato alla prima tupla di quelle ritornate */
        if (cursore.moveToFirst()) {

            /* Settiamo la data del codice del tampone da comunicare azzerrandone millisecondi, secondi, minuti e ore */
            do {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                arrayMieiCodici.add(new TemporaryExposureKey(cursore.getString(0), cal.getTime()));

            } while (cursore.moveToNext());
        }
        databaseSQLite.close();
        return arrayMieiCodici;
    }


    /**
     * Cerca dei matching tra i tek passati come paramentro e i tek presenti nella tabella ContattiAvvenuti.
     * Ritorna il numero di occorrenze per ogni diverso contatto passato come paramentro.
     * Se un tek non viene trovato, non viene nemmeno restituito nel valore di ritorno.
     *
     * @param codici
     * @return HashMap<String,int> contente come chiave il codice della tek, come valore il numero di occorrenze.
     */
    public Map<String,Integer> matchTekContattiConTekPositivi(List<TemporaryExposureKey> codici) {

        if(codici.size() == 0) return new HashMap<>();

        SQLiteDatabase databaseSQLite = this.getReadableDatabase();
        String finalIn = "(";
        for(TemporaryExposureKey tek : codici) {
            finalIn += "\""+tek.getId() + "\",";
        }

        // la stringa finalIn permette di ricreare la condizione all'interno del WHERE IN
        finalIn = finalIn.substring(0,  finalIn.length()-1);

        String queryControlloContatti = "SELECT COUNT("+SaventContract.ContattiAvvenuti.COLUMN_NAME_CODICI+") AS NumberOfContacts,"+SaventContract.ContattiAvvenuti.COLUMN_NAME_CODICI+" FROM " + SaventContract.ContattiAvvenuti.TABLE_NAME +
                " WHERE "+ SaventContract.ContattiAvvenuti.COLUMN_NAME_CODICI + " IN " + finalIn + ") GROUP BY "+SaventContract.ContattiAvvenuti.COLUMN_NAME_CODICI;

        Cursor cursore = databaseSQLite.rawQuery(queryControlloContatti,null);
        HashMap<String,Integer> map = new HashMap<>();

        if (cursore.moveToFirst()) {
            do {
                map.put(cursore.getString(1),cursore.getInt(0));
            } while (cursore.moveToNext());
        }
        databaseSQLite.close();
        return map;
    }

    /**
     * Inserisce la lista di tek passata come parametro nella tabella di codici tek positivi "TekPositivi".
     *
     * @param positiveTekList lista di tek da aggiungere nella tabella TekPositivi.
     */
    public void addPositiveTek(List<TemporaryExposureKey> positiveTekList){
        SQLiteDatabase databaseSQLite = this.getWritableDatabase();
        for(TemporaryExposureKey t : positiveTekList){
            ContentValues contentValues = new ContentValues();
            contentValues.put(SaventContract.TekPositivi.COLUMN_NAME_CODICI, t.getId());
            contentValues.put(SaventContract.TekPositivi.COLUMN_NAME_DATA, t.getData().getTime());
            databaseSQLite.insert(SaventContract.TekPositivi.TABLE_NAME, null, contentValues);
        }
        databaseSQLite.close();
    }



}
