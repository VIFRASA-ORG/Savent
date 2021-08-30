package Services.BluetoothLEServices;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.UUID;

import Helper.BluetoothLEHelper;
import Helper.LocalStorage.SQLiteHelper;
import Model.LogDebug;


/**
 * Service che ricerca per Bluetooth LE dispositivi compatibili nelle vicinanze
 * tentando di stabilirci una connessione.
 *
 * Se la connessione va a buon fine, controlla se il server a cui si e connesso contiene
 * il corretto service e le corrette caratteristiche in maniera tale da leggere il TEK inviato
 * dal server e da inviare il proprio.
 */
public class GattServerCrawlerService extends Service {


    /**
     * Dichiarazione di tutte le costanti per lanciare e ricevere gli intent
     * con cui interagisce questo service.
     *
     * In particolare risponde all'intent per riavviare il GATT crawler,
     * all'intent per stopparlo o per interagire con il cambio di stato con i permessi di FineLocation.
     */
    public static final String RESTART_GATT_CRAWLER_INTENT = "restartGattCrawler";
    public static final String STOP_GATT_CRAWLER_INTENT = "stopGattCrawler";
    public static final String FINE_LOCATION_GRANTED_INTENT = "fineLocationGranted";

    //Flag che indica se il SERVICE è attualmente in esecuzione o no.
    public static boolean isServiceRunning = false;

    //Flag che indicano lo stato dei permessi e degli stati necessari per eseguire il crawler.
    boolean isLocationPermissionGranted = false;
    boolean isBluetoothEnabled = false;
    boolean isGeolocationEnabled = false;


    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bleScanner = null;

    //Definizione delle impostazioni di scansione.
    ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();


    @Override
    public void onCreate() {
        super.onCreate();

        isServiceRunning = true;

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //Settaggio di tutti i flag necessari per eseguire il crawler.
        setFlags();

        //Regitrazione del receiver per i cambi di stato del bluetooth.
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, filter);

        //Add a receiver for the GPS state event
        //Regitrazione del receiver per i cambi di stato della geolocalizzazione.
        IntentFilter filter1 = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        filter1.addAction(Intent.ACTION_PROVIDER_CHANGED);
        registerReceiver(geolocationStateChangeReceiver, filter1);

        //Registrazione dei broadcast receiver per i 3 intent sopra definiti.
        LocalBroadcastManager.getInstance(this).registerReceiver(fineLocationGrantedReceiver, new IntentFilter(FINE_LOCATION_GRANTED_INTENT));
        LocalBroadcastManager.getInstance(this).registerReceiver(stopGattCrawlerReceiver, new IntentFilter(STOP_GATT_CRAWLER_INTENT));
        LocalBroadcastManager.getInstance(this).registerReceiver(restartGattCrawlerReceiver, new IntentFilter(RESTART_GATT_CRAWLER_INTENT));

        //Chaimata del metodo che esegue tutti i controlli prima di avviare il GATT Crawler.
        startBleScan();
    }

    /**
     * Imposta i flag per i permessi e le funzionalità necessarie per eseguire il crawler.
     */
    private void setFlags(){
        isLocationPermissionGranted = BluetoothLEHelper.isFineLocationGranted(this);
        isBluetoothEnabled = BluetoothLEHelper.isBluetoothEnabled();
        isGeolocationEnabled = BluetoothLEHelper.isGpsEnabled(this);
    }

    /**
     * Metodo che esegue i controlli necessari prima di avviare la scansione dei dispositivi BLE nelle vicinanze.
     * Il bluetooth e la geolocalizzazione devono essere abilitati e i permessi FineLocation devono essere dati.
     */
    private void startBleScan(){
        if(isLocationPermissionGranted && isBluetoothEnabled && isGeolocationEnabled) {
            //Esegui scansione
            if(bleScanner == null) bleScanner = bluetoothAdapter.getBluetoothLeScanner();
            bleScanner.startScan(null, settings, scanCallback);
        }
    }




    /**
     * CALLBACK
     */

    /**
     * Implementazione della classe astratta ScanCallback per gestire tutti i callback
     * risultanti la scnasione dei device BLE nelle vicinanze.
     */
    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.i(LogDebug.GAT_CRAWLER_SCAN_RESULT,"Connecting to name: " + result.getDevice().getName() + ", address: "+ result.getDevice().getAddress());

            //Tentativo di connessione al GATT Server del device trovato.
            result.getDevice().connectGatt(getApplicationContext(),false, gattCallback);
        }
    };

    /**
     * Implementazione della classe astratta BluetoothGattCallback per gestire tutti i metodi di callback
     * risultanti la connessione ad un GATT Server.
     */
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            BluetoothDevice device = gatt.getDevice();

            //Controllo e l'operazione è avvenuta con successo.
            if(status == BluetoothGatt.GATT_SUCCESS){

                //Controllo se la connessione è avvenuta con successo.
                if(newState == BluetoothGatt.STATE_CONNECTED){
                    Log.w(LogDebug.GAT_CRAWLER_CONN_RESULT, "Successfully connected to " + device.getName() + " " + device.getAddress());

                    //Avvio del ritrovamento dei service sul server.
                    //Richiamerà il callback onServiceDiscovered se trova qualcosa.
                    gatt.discoverServices();
                }else if (newState == BluetoothGatt.STATE_DISCONNECTED){
                    //Connessione chiusa.
                    Log.w(LogDebug.GAT_CRAWLER_CONN_RESULT, "Successfully disconnected from "+device.getName());
                    gatt.close();
                }
            }else{
                //Errore nella connessione con il gatt server.
                Log.w(LogDebug.GAT_CRAWLER_CONN_RESULT, "Error "+status+" encountered for "+device.getName()+" Disconnecting...");
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            Log.w(LogDebug.GAT_CRAWLER_SERVI_FOUND, "Service discovered for the device: "+gatt.getDevice().getName());

            //Controllo se è presente il service Send tra la lista dei service trovati.
            BluetoothGattService service = gatt.getService(UUID.fromString(BluetoothLEHelper.UUID_SERVICE));
            if(service != null){
                //Il gatt server a cui siamo connessi ha il service da noi richiesto.
                Log.w(LogDebug.GAT_CRAWLER_SERVI_FOUND, "Service on device "+gatt.getDevice().getName()+ " UUID: "+service.getUuid());

                //Invocazione del metodo per leggere il valore della caratteristica Send.
                readSENDCharacteristicFromService(gatt,service);

                //Invocazione del metodo per inviare il proprio TEK nella caratteristica Receive.
                writeRECEIVECharatceristicFromService(gatt, service);
            }
        }

        /**
         * Metodo per leggere il valore dalla caratteristica di Send, se essa esiste nel Gatt Service.
         * Il valore corrisponderà con il tek del'altro dispositivo.
         *
         * @param gatt oggetto gatt per comunicare con il GATT Server.
         * @param service il service in cui leggere e controllare la caratteristica di Send.
         */
        private void readSENDCharacteristicFromService(BluetoothGatt gatt, BluetoothGattService service){
            //Controllo se la caratteristica di Send esiste dentro il service passato come parametro.
            BluetoothGattCharacteristic characteristicSend = service.getCharacteristic(UUID.fromString(BluetoothLEHelper.UUID_CHARACTERISTIC_SEND));
            if(characteristicSend != null) {
                //Richiamerà il metodo di callback onCharacteristicRead con il valore letto.
                gatt.readCharacteristic(characteristicSend);
            }
        }

        /**
         * Metodo per scrivere dentro la caratteristica UUID_CHARACTERISTIC_RECEIVE l'attuale TEK.
         * Controlla che la caratteristica esista dentro il service del GAtt server.
         *
         * @param gatt oggetto gatt per comunicare con il gat Server.
         * @param service il service in cui leggere e controllare la caratteristica di Receive.
         */
        private void writeRECEIVECharatceristicFromService(BluetoothGatt gatt, BluetoothGattService service){
            //Ritrovamento dell'ultima TEK
            SQLiteHelper db = new SQLiteHelper(getApplicationContext());
            String tek = db.getLastTek();

            if(tek == null) return;

            //Controllo che la caratteristica di receive esista nel service.
            BluetoothGattCharacteristic characteristicReceive = service.getCharacteristic(UUID.fromString(BluetoothLEHelper.UUID_CHARACTERISTIC_RECEIVE));
            if(characteristicReceive != null){

                //Controllo che la caratteristica sia abilitata alla scrittura
                if(characteristicReceive.getWriteType() == BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT){

                    //Imposto il valore da scrivere
                    characteristicReceive.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    characteristicReceive.setValue(tek);

                    //Esegua la scrittura all'interno di un thread che verra eseguito dopo 500 millisecondi
                    //in quanto bisogna aspettare che l'operazione di read finisca.
                    HandlerThread handlerThread = new HandlerThread("background-thread-write-chara");
                    handlerThread.start();

                    Handler handler = new Handler(handlerThread.getLooper());
                    handler.postDelayed(() -> gatt.writeCharacteristic(characteristicReceive), 500);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            //Conversione dei dati letti dal Gatt Server in una Stringa.
            String value = new String(characteristic.getValue(), StandardCharsets.UTF_8);

            Log.i(LogDebug.GAT_CRAWLER_CHARAC_READ, "Code found: " + value + " | Putting inside the local SQLite database.");

            //Salvataggio del valore letto, quindi il TEK, dentro la tabella ContattiAvvenuti nel database SQLite locale.
            SQLiteHelper db = new SQLiteHelper(getApplicationContext());
            db.insertContattiAvvenuti(value, Calendar.getInstance());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            //Controllo se l'operazione di scrittura è avvenuta con successo.
            if(status == BluetoothGatt.GATT_SUCCESS){
                Log.i(LogDebug.GAT_CRAWLER_CHARA_WRITE, "Wrote value: " + new String(characteristic.getValue(), StandardCharsets.UTF_8));
            }else if(status == BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH){
                Log.i(LogDebug.GAT_CRAWLER_CHARA_WRITE, "Write exceeded connection ATT MTU! ");
            }else if(status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED){
                Log.e(LogDebug.GAT_CRAWLER_CHARA_WRITE, "Write not permitted for "+characteristic.getUuid());
            }else{
                Log.e(LogDebug.GAT_CRAWLER_CHARA_WRITE, "Characteristic write failed for "+characteristic.getUuid()+", error: "+status);
            }
        }
    };





    /**
     * TUTTI BROADCAST RECEIVER
     */

    /**
     * Broadcast receiver per l'intent di riavvio del crawler.
     */
    private BroadcastReceiver restartGattCrawlerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LogDebug.GAT_SERVER_LOG, "Restarting the gatt crawler.");
            setFlags();
            startBleScan();
        }
    };

    /**
     * Broadcast receiver per l'intent di cancellazione del crawler.
     */
    private BroadcastReceiver stopGattCrawlerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LogDebug.GAT_SERVER_LOG, "Stopping the gatt crawler.");
            if(bleScanner != null) bleScanner.stopScan(scanCallback);
        }
    };

    /**
     * Broadcast receiver per i cambi di stato della geolocalizzazione.
     */
    private final BroadcastReceiver geolocationStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothLEHelper.isGpsEnabled(getApplicationContext())){
                setFlags();
                startBleScan();
            }
        }
    };

    /**
     * Broadcast receiver per i cambi di stato del permesso FineLocation.
     */
    private BroadcastReceiver fineLocationGrantedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setFlags();
            isLocationPermissionGranted = BluetoothLEHelper.isFineLocationGranted(getApplicationContext());
            startBleScan();
        }
    };

    /**
     * Broadcast receiver per i cambi di stato del bluetooth.
     */
    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                setFlags();
                switch (bluetoothState) {
                    case BluetoothAdapter.STATE_ON:
                        startBleScan();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        isBluetoothEnabled = false;
                        break;
                }
            }
        }
    };

    /**
     *  FINE BROADCAST RECEIVER
     */





    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        //Rimozione dei broadcast receiver.
        try{
            unregisterReceiver(fineLocationGrantedReceiver);
            unregisterReceiver(bluetoothStateReceiver);
            unregisterReceiver(geolocationStateChangeReceiver);
            unregisterReceiver(stopGattCrawlerReceiver);
            unregisterReceiver(restartGattCrawlerReceiver);
        }catch(IllegalArgumentException e){
            Log.i("GAT_SERVER_LOG","Error unregistering the receiver.");
        }

        super.onDestroy();
        isServiceRunning = false;
    }
}
