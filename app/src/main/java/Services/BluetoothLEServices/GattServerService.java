package Services.BluetoothLEServices;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.ParcelUuid;
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
 * Service che lancia un BLE GATT Server con al suo interno un singolo service identificato dell'UUID definito nella classe BluetoothLEHelper.
 * Il service contiene due caratteristiche identificate rispettivamente da: BluetoothLEHelper.UUID_SERVICE_SEND and BluetoothLEHelper.UUID_SERVICE_RECEIVE.
 *
 * La prima caratteristica, di sola lettura, invia in broadcast il TEK dell'utente loggato.
 * La seconda caratteristica, di sola scrittura, riceve i TEK dai crawler degli altri dispositivi.
 *
 * Per far funzionare il GATT Server, è necessario avere il bluetooth attivo
 * e inoltre reagisce anche agli eventi di cambiamento di stato del bluetooth.
 */
public class GattServerService extends Service {

    /**
     * Dichiarazione di tutte le costanti per lanciare e ricevere gli intent
     * con cui interagisce questo service.
     *
     * In particolare risponde all'intent per riavviare il GATT Server,
     * all'intent per stopparlo o per aggiornare il valore inviato dalla caratteristicha di send.
     */
    public static final String RESTART_GATT_SERVER_INTENT = "restartGattServer";
    public static final String STOP_GATT_SERVER_INTENT = "stopGattServer";
    public static final String UPDATE_GATT_CHARACTERISTIC_INTENT = "updateGattCharacteristicValue";

    //Flag che indica se il SERVICE è attualmente in esecuzione o no.
    public static boolean isServiceRunning = false;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGattServer bluetoothGattServer;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser = null;

    //Creazione degli oggetti UUID partendo dai valori uuid stringa.
    private UUID serviceUUID = UUID.fromString(BluetoothLEHelper.UUID_SERVICE);
    private UUID characteristic_send_UUID = UUID.fromString(BluetoothLEHelper.UUID_CHARACTERISTIC_SEND);
    private UUID characteristic_receive_UUID = UUID.fromString(BluetoothLEHelper.UUID_CHARACTERISTIC_RECEIVE);

    //Definizione delle settings per il BluetoothLE advertising.
    AdvertiseSettings settings = new AdvertiseSettings.Builder()
            .setConnectable(true)
            .build();

    //Definizione dei dati advertisement da usare negli advertisement packet.
    AdvertiseData advertiseData = new AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(true)
            .build();

    //Definizione della risposta di scansione associata ad i dati di advertisement.
    AdvertiseData scanResponseData = new AdvertiseData.Builder()
            .addServiceData(new ParcelUuid(serviceUUID),"bt".getBytes(StandardCharsets.UTF_8 ))
            .setIncludeTxPowerLevel(true)
            .build();




    @Override
    public void onCreate() {
        super.onCreate();

        isServiceRunning = true;

        //Regitrazione del receiver per i cambi di stato del bluetooth.
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, filter);

        //Registrazione dei broadcast receiver per i 3 intent sopra definiti.
        LocalBroadcastManager.getInstance(this).registerReceiver(restartServer, new IntentFilter(RESTART_GATT_SERVER_INTENT));
        LocalBroadcastManager.getInstance(this).registerReceiver(stopServer, new IntentFilter(STOP_GATT_SERVER_INTENT));
        LocalBroadcastManager.getInstance(this).registerReceiver(updateCharacteristicValue, new IntentFilter(UPDATE_GATT_CHARACTERISTIC_INTENT));

        //Chaimata del metodo che esegue tutti i controlli prima di avviare il GATT Server.
        tryToStartServer();
    }

    /**
     * Metodo che esegue tutti i controlli prima di avviare il GATT Server.
     * Se i controlli vanno a buon fine, avvia effettivamente il server.
     */
    private void tryToStartServer(){
        //Scaricamento dell'ultimo TEK da comunicare nella caratteristica di send.
        SQLiteHelper db = new SQLiteHelper(this);
        String lastTek = db.getLastTek();

        if(lastTek != null){
            //Controlla se il bluetooth è supportato e abilitato
            //Controlla anche se il multiple advertisment è supportato.
            if (checkBluetoothService()){
                startServer(lastTek);
            }
        }
    }

    /**
     * Metodo che esegue tutte le operazioni per avviare effettivamente il GATT Server
     * con il singolo servizio e le due caratteristiche.
     *
     * @param characteristicValue il TEK da inviare in broadcast nella caratteristica di sola lettura BluetoothLEHelper.UUID_SERVICE_SEND.
     */
    private void startServer(String characteristicValue){

        Log.d(LogDebug.GAT_SERVER_LOG, "SERVICE: " + serviceUUID.toString());
        Log.d(LogDebug.GAT_SERVER_LOG, "CHARAC SEND: " + characteristic_send_UUID.toString());
        Log.d(LogDebug.GAT_SERVER_LOG, "CHARAC RECEIVE: " + characteristic_receive_UUID.toString());

        //Creazione dell'oggetto bluetoothLeAdvertiser se non esiste ancora.
        if(bluetoothLeAdvertiser == null){
            bluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, scanResponseData, callback);
        }

        //Apertura del server con assegnazione dei metodi di callback per i cambi di stato del server.
        bluetoothGattServer = mBluetoothManager.openGattServer(this.getApplicationContext(), gattServerCallback);
        BluetoothGattService service = new BluetoothGattService(serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        //Creazione e aggiunta della caratteristica send del TEK al service.
        BluetoothGattCharacteristic characteristicSend = new BluetoothGattCharacteristic(characteristic_send_UUID, BluetoothGattCharacteristic.PROPERTY_READ ,  BluetoothGattCharacteristic.PERMISSION_READ);
        characteristicSend.setValue(characteristicValue);
        service.addCharacteristic(characteristicSend);

        //Creazione e aggiunta della caratteristica receive del TEK di altri dispositivi al service.
        BluetoothGattCharacteristic characteristicReceive = new BluetoothGattCharacteristic(characteristic_receive_UUID,BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT, BluetoothGattCharacteristic.PERMISSION_WRITE |  BluetoothGattCharacteristic.PERMISSION_READ);
        service.addCharacteristic(characteristicReceive);

        //Aggiunta del service finale al GATT Server.
        if(bluetoothGattServer != null) bluetoothGattServer.addService(service);
    }

    /**
     * Metodo che esegui i controlli del bluetooth per un corretto avvio del GATT Server.
     *
     * @return true se tutti i controlli sono corretti, false altrimenti.
     */
    private boolean checkBluetoothService(){

        //Controllo se il bluetooth è supportato e abilitato.
        BluetoothAdapter bluAdap = BluetoothAdapter.getDefaultAdapter();
        if (bluAdap == null) {
            // Il device non supporta il bluetooth.
            Log.i(LogDebug.GAT_SERVER_LOG, "BLUETOOTH NOT SUPPORTED");
            return false;
        } else if (! bluAdap.isEnabled()) {
            // Bluetooth spento.
            Log.i(LogDebug.GAT_SERVER_LOG, "BLUETOOTH TURNED OFF");
            return false;
        } else {
            // Bluetooth acceso.
            //Controllo se il MultipleAdvertisement è supportato.
            mBluetoothManager  = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();

            if(mBluetoothAdapter.isMultipleAdvertisementSupported()){
                Log.i(LogDebug.GAT_SERVER_LOG, "MultipleAdvertisement SUPPORTATO");
                return true;
            }else {
                Log.i(LogDebug.GAT_SERVER_LOG, "MultipleAdvertisement NON SUPPORTATO");
                return false;
            }
        }
    }



    /**
     * Broadcast receiver per i cambi di stato del bluetooth.
     */
    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (bluetoothState) {
                    case BluetoothAdapter.STATE_ON:
                        tryToStartServer();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        break;
                }
            }
        }
    };

    /**
     * Broadcast receiver per l'intent di riavvio del server.
     */
    private BroadcastReceiver restartServer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LogDebug.GAT_SERVER_LOG, "Restarting the server.");
            tryToStartServer();
        }
    };

    /**
     * Broadcast receiver per l'intent di cancellazione del server.
     */
    private BroadcastReceiver stopServer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LogDebug.GAT_SERVER_LOG, "Stopping the server.");
            if(bluetoothGattServer != null){
                bluetoothGattServer.close();
                bluetoothGattServer.clearServices();
            }
        }
    };

    /**
     * Broadcast receiver per aggiornare il valore della caratteristica di send.
     */
    private BroadcastReceiver updateCharacteristicValue = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newValue = intent.getStringExtra("newValue");
            bluetoothGattServer.getService(UUID.fromString(BluetoothLEHelper.UUID_SERVICE)).getCharacteristic(characteristic_send_UUID).setValue(newValue);
        }
    };

    /**
     * Metodo di callback utilizzato per ricevere i cambi di stato del BLE advertisement.
     */
    AdvertiseCallback callback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d(LogDebug.GAT_SERVER_LOG, "BLE advertisement added successfully");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e(LogDebug.GAT_SERVER_LOG, "Failed to add BLE advertisement, reason: " + errorCode);
        }
    };

    /**
     * Implementazione della classe astratta BluetoothGattServerCallbackper gestire tutti i
     * callback provenienti della connessioni entranti al GATT Server da parte dei Crawler.
     */
    BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

            //Conversione dei dati ricevuti in stringa.
            String TEK = new String(value,StandardCharsets.UTF_8);
            System.out.println("Value wanted to assign: " + TEK);
            Log.i(LogDebug.GAT_SERVER_LOG,"New code: "+TEK);

            //Inserimento del TEK ricevuto nel database SQLlite locale.
            SQLiteHelper db = new SQLiteHelper(getApplicationContext());
            db.insertContattiAvvenuti(TEK, Calendar.getInstance());

            //Invio della risposta al crawler altrimenti la connessione non viene mai chiusa.
            bluetoothGattServer.sendResponse(device,requestId,BluetoothGatt.GATT_SUCCESS,offset,value);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(LogDebug.GAT_SERVER_LOG, "Sending response to characteristic read.");

            //Invio del TEK corrent al crawler che lo richiede.
            bluetoothGattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,offset,characteristic.getValue());
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        //Rimozione dei broadcast receiver.
        try{
            unregisterReceiver(stopServer);
            unregisterReceiver(restartServer);
            unregisterReceiver(updateCharacteristicValue);
            unregisterReceiver(bluetoothStateReceiver);
        } catch (IllegalArgumentException e){
            Log.i("GAT_SERVER_LOG","Error unregistering the receiver.");
        }

        super.onDestroy();
        isServiceRunning = false;
    }
}
