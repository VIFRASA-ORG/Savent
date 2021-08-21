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
 * Service that run a BLE Gatt Server with a single single service with the following UUID: BluetoothLEHelper.UUID_SERVICE
 * This service has two characteristic: BluetoothLEHelper.UUID_SERVICE_SEND and BluetoothLEHelper.UUID_SERVICE_RECEIVE.
 *
 * The first characteristic send, as broadcast, the current logged-in TEK.
 * The second characteristic is used to allow the crawler of the other device to send their TEK to this device.
 *
 * In order to work, this service need bluetooth turned on and react to the bluetooth state change event.
 */
public class GattServerService extends Service {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGattServer bluetoothGattServer;

    //Creating all the UUID from the UUID string
    private UUID serviceUUID = UUID.fromString(BluetoothLEHelper.UUID_SERVICE);
    private UUID characteristic_send_UUID = UUID.fromString(BluetoothLEHelper.UUID_CHARACTERISTIC_SEND);
    private UUID characteristic_receive_UUID = UUID.fromString(BluetoothLEHelper.UUID_CHARACTERISTIC_RECEIVE);

    //Defining the settings for Bluetooth LE advertising
    AdvertiseSettings settings = new AdvertiseSettings.Builder()
            .setConnectable(true)
            .build();

    //Defining the advertisement data to be advertised in advertisement packet
    AdvertiseData advertiseData = new AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(true)
            .build();

    //Defining the scan response associated with the advertisement data
    AdvertiseData scanResponseData = new AdvertiseData.Builder()
            .addServiceData(new ParcelUuid(serviceUUID),"bt".getBytes(StandardCharsets.UTF_8 ))
            .setIncludeTxPowerLevel(true)
            .build();




    @Override
    public void onCreate() {
        super.onCreate();

        //Set a filter to only receive bluetooth state changed events.
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, filter);

        //Invoking the method to perform all checks before starting the server
        tryToStartServer();
    }

    /**
     * Method to perform all checks before starting the server.
     * If the check are all successful, is invoked the method to start the server.
     * It also register the broadcast receiver to kill the service or to update the TEK broadcasted.
     */
    private void tryToStartServer(){
        //Getting the last TEK
        SQLiteHelper db = new SQLiteHelper(this);
        String lastTek = db.getLastTek();

        if(lastTek != null){
            //Checking if the bluetooth is supported and enabled, and check also
            //if the device supports the multiple advertisement.
            if (checkBluetoothService()){
                startServer(lastTek);

                //Registering the two broadcast receiver
                LocalBroadcastManager.getInstance(this).registerReceiver(killProcess, new IntentFilter("killGattServerService"));
                LocalBroadcastManager.getInstance(this).registerReceiver(updateCharacteristicValue, new IntentFilter("updateGattCharacteristicValue"));
            }
        }
    }

    /**
     * Method that start the Gatt server with the single service and the two characteristics.
     *
     * @param characteristicValue the TEK to broadcast inside the characteristic BluetoothLEHelper.UUID_SERVICE_SEND.
     */
    private void startServer(String characteristicValue){

        Log.d(LogDebug.GAT_SERVER_LOG, "SERVICE: " + serviceUUID.toString());
        Log.d(LogDebug.GAT_SERVER_LOG, "CHARAC SEND: " + characteristic_send_UUID.toString());
        Log.d(LogDebug.GAT_SERVER_LOG, "CHARAC RECEIVE: " + characteristic_receive_UUID.toString());

        BluetoothLeAdvertiser bluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, scanResponseData, callback);
        bluetoothGattServer = mBluetoothManager.openGattServer(this.getApplicationContext(), gattServerCallback);
        BluetoothGattService service = new BluetoothGattService(serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        //add the characteristic to send the TEK
        BluetoothGattCharacteristic characteristicSend = new BluetoothGattCharacteristic(characteristic_send_UUID, BluetoothGattCharacteristic.PROPERTY_READ ,  BluetoothGattCharacteristic.PERMISSION_READ);
        characteristicSend.setValue(characteristicValue);
        service.addCharacteristic(characteristicSend);

        //Characteristic to receive the TEK from other Gatt Crawler
        BluetoothGattCharacteristic characteristicReceive = new BluetoothGattCharacteristic(characteristic_receive_UUID,BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT, BluetoothGattCharacteristic.PERMISSION_WRITE |  BluetoothGattCharacteristic.PERMISSION_READ);
        service.addCharacteristic(characteristicReceive);

        //Adding the final service to the Gatt Server.
        bluetoothGattServer.addService(service);
    }

    /**
     * Method that performs all check for proper Gatt Server startup.
     *
     * @return true if all the check are successful, false otherwise.
     */
    private boolean checkBluetoothService(){

        //Checking if the bluetooth is supported or enabled
        BluetoothAdapter bluAdap = BluetoothAdapter.getDefaultAdapter();
        if (bluAdap == null) {
            // Device does not support Bluetooth
            Log.i(LogDebug.GAT_SERVER_LOG, "BLUETOOTH NOT SUPPORTED");
            return false;
        } else if (! bluAdap.isEnabled()) {
            // Bluetooth is not enabled
            Log.i(LogDebug.GAT_SERVER_LOG, "BLUETOOTH TURNED OFF");
            return false;
        } else {
            // Bluetooth is enabled
            //Checking if the MultipleAdvertisement is supported
            mBluetoothManager  = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();

            if(mBluetoothAdapter.isMultipleAdvertisementSupported()){
                Log.i(LogDebug.GAT_SERVER_LOG, "SUPPORTED");
                return true;
            }else {
                Log.i(LogDebug.GAT_SERVER_LOG, "NOT SUPPORTED");
                return false;
            }
        }
    }



    /**
     * Broadcast receiver to run the server when the bluetooth is enabled during the execution.
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
                        //Bluethooth is on, now you can perform your tasks
                        tryToStartServer();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        break;
                }
            }
        }
    };

    /**
     * Broadcast receiver to kill the service
     */
    private BroadcastReceiver killProcess = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LogDebug.GAT_SERVER_LOG, "Killing the service.");
            bluetoothGattServer.close();
            stopSelf();
        }
    };

    /**
     * Broadcast receiver to update the characteristic value
     */
    private BroadcastReceiver updateCharacteristicValue = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newValue = intent.getStringExtra("newValue");
            bluetoothGattServer.getService(UUID.fromString(BluetoothLEHelper.UUID_SERVICE)).getCharacteristic(characteristic_send_UUID).setValue(newValue);
        }
    };



    /**
     * Callback method used to deliver advertising operation status.
     * Such as when there is a change in the Gatt Server Status.
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
     * BluetoothGattServerCallback abstract class implementation to manage all the
     * callback from the inbound connections to the GATT server from other Crawler.
     */
    BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

            //Converting the received data into a String
            String TEK = new String(value,StandardCharsets.UTF_8);
            System.out.println("Value wanted to assign: " + TEK);
            Log.i(LogDebug.GAT_SERVER_LOG,"New code: "+TEK);

            //Inserting the received TEK to the local SQLite database
            SQLiteHelper db = new SQLiteHelper(getApplicationContext());
            db.insertContattiAvvenuti(TEK, Calendar.getInstance());

            //Sending the response to the crawler otherwise the connection will not be closed.
            bluetoothGattServer.sendResponse(device,requestId,BluetoothGatt.GATT_SUCCESS,offset,value);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(LogDebug.GAT_SERVER_LOG, "Sending response to characteristic read.");

            //Sending the current TEK to the crawler requesting it.
            bluetoothGattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,offset,characteristic.getValue());
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
