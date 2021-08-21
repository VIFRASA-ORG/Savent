package com.vitandreasorino.savent.Utenti.BluetoothLEServices;

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
import Helper.SQLiteHelper;
import Model.LogDebug;

/**
 * Service that search for nearby BluetoothLE compatible device,
 * try to connect to them.
 *
 * If the connection is successful, search for the correct service and characteristics
 * to read the TEK and to send the actual TEK (Temporary Exposure Key).
 */
public class GattServerCrawlerService extends Service {

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    boolean isLocationPermissionGranted = false;
    boolean isBluetoothEnabled = false;

    //Defining the scan settings
    ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();



    @Override
    public void onCreate() {
        super.onCreate();

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        isLocationPermissionGranted = BluetoothLEHelper.isFineLocationGranted(this);
        isBluetoothEnabled = BluetoothLEHelper.isBluetoothEnabled();

        //Set a filter to only receive bluetooth state changed events.
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, filter);

        //Broadcast to manage the location granted event
        LocalBroadcastManager.getInstance(this).registerReceiver(fineLocationGrantedReceiver, new IntentFilter("fineLocationGranted"));

        startBleScan();
    }



    /**
     * Broadcast receiver to run the crawler after the file location permission granted
     */
    private BroadcastReceiver fineLocationGrantedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isLocationPermissionGranted = BluetoothLEHelper.isFineLocationGranted(getApplicationContext());;
            startBleScan();
        }
    };

    /**
     * Broadcast receiver to run the crawler when the bluetooth is enabled during the execution.
     */
    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (bluetoothState) {
                    case BluetoothAdapter.STATE_ON:
                        isBluetoothEnabled = true;
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
     * Method that performs the necessary checks and starts the scan in search of BLE device nearby.
     * The bluetooth must be enabled and the FineLocationPermission must granted.
     */
    private void startBleScan(){
        if(isLocationPermissionGranted && isBluetoothEnabled) {
            //Perform scan
            BluetoothLeScanner bleScanner = bluetoothAdapter.getBluetoothLeScanner();
            bleScanner.startScan(null, settings, scanCallback);
        }
    }



    /**
     * Callback method invoked when a BLE device is found.
     * This method try also to connect to the device found.
     */
    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            Log.i(LogDebug.GAT_CRAWLER_SCAN_RESULT,"Connecting to name: " + result.getDevice().getName() + ", address: "+ result.getDevice().getAddress());

            //Connecting to the Gatt server on the device.
            result.getDevice().connectGatt(getApplicationContext(),false, gattCallback);
        }
    };

    /**
     * BluetoothGattCallback abstract class implementation to manage all the
     * callback from the connection to a GATT server on a device.
     */
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            BluetoothDevice device = gatt.getDevice();

            //Checking if the operation is successful
            if(status == BluetoothGatt.GATT_SUCCESS){

                //Check if the connection was successful
                if(newState == BluetoothGatt.STATE_CONNECTED){
                    Log.w(LogDebug.GAT_CRAWLER_CONN_RESULT, "Successfully connected to " + device.getName() + " " + device.getAddress());

                    //Discovering the services
                    gatt.discoverServices();
                }else if (newState == BluetoothGatt.STATE_DISCONNECTED){
                    //The connection is closed
                    Log.w(LogDebug.GAT_CRAWLER_CONN_RESULT, "Successfully disconnected from "+device.getName());
                    gatt.close();
                }
            }else{
                //Error trying to connect to the Gatt server
                Log.w(LogDebug.GAT_CRAWLER_CONN_RESULT, "Error "+status+" encountered for "+device.getName()+" Disconnecting...");
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            Log.w(LogDebug.GAT_CRAWLER_SERVI_FOUND, "Service discovered for the device: "+gatt.getDevice().getName());

            //Checking if the Gatt Server has the service needed by this application.
            BluetoothGattService service = gatt.getService(UUID.fromString(BluetoothLEHelper.UUID_SERVICE));
            if(service != null){
                //The Gatt Server has the correct Service
                Log.w(LogDebug.GAT_CRAWLER_SERVI_FOUND, "Service on device "+gatt.getDevice().getName()+ " UUID: "+service.getUuid());

                //Calling the method to read the characteristic value, the TEK, from the device service.
                readSENDCharacteristicFromService(gatt,service);

                //Calling the method to send to the connected device your TEK.
                writeRECEIVECharatceristicFromService(gatt, service);
            }
        }

        /**
         * Method to read the characteristic value, the TEK, from the Gatt service.
         * It check if the correct service has the correct characteristic (BluetoothLEHelper.UUID_CHARACTERISTIC_SEND).
         *
         * @param gatt gatt object for communicating with the Gatt server.
         * @param service the service in which to read the characteristic.
         */
        private void readSENDCharacteristicFromService(BluetoothGatt gatt, BluetoothGattService service){
            //Checking if the characteristic exists inside the service
            BluetoothGattCharacteristic characteristicSend = service.getCharacteristic(UUID.fromString(BluetoothLEHelper.UUID_CHARACTERISTIC_SEND));
            if(characteristicSend != null) gatt.readCharacteristic(characteristicSend);
        }

        /**
         * Method to WRITE inside the characteristic (UUID_CHARACTERISTIC_RECEIVE) the current TEK.
         * It check if the service contains the correct characteristic (UUID_CHARACTERISTIC_RECEIVE).
         *
         * @param gatt gatt object for communicating with the Gatt server.
         * @param service the service in which to send the TEK.
         */
        private void writeRECEIVECharatceristicFromService(BluetoothGatt gatt, BluetoothGattService service){
            //Getting the TEK to send
            SQLiteHelper db = new SQLiteHelper(getApplicationContext());
            String tek = db.getLastTek();

            if(tek == null) return;

            //Checking if the characteristic exists inside the service
            BluetoothGattCharacteristic characteristicReceive = service.getCharacteristic(UUID.fromString(BluetoothLEHelper.UUID_CHARACTERISTIC_RECEIVE));
            if(characteristicReceive != null){

                //Checking if the characteristic has the write flag set
                if(characteristicReceive.getWriteType() == BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT){

                    //Setting the value to send
                    characteristicReceive.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    characteristicReceive.setValue(tek);

                    //Executing the write inside a thread who will run after 500millisecond
                    //Because we have to wait for the read operation to finish.
                    HandlerThread handlerThread = new HandlerThread("background-thread-write-chara");
                    handlerThread.start();

                    Handler handler = new Handler(handlerThread.getLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            gatt.writeCharacteristic(characteristicReceive);
                        }
                    }, 500);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            //Converting the data read from the Gatt Server into a String
            String value = new String(characteristic.getValue(), StandardCharsets.UTF_8);

            Log.i(LogDebug.GAT_CRAWLER_CHARAC_READ, "Code found: " + value + " | Putting inside the local SQLite database.");

            //Save the value of the characteristic into the SQLlite database into the ContattiAvvenuti table.
            SQLiteHelper db = new SQLiteHelper(getApplicationContext());
            db.insertContattiAvvenuti(value, Calendar.getInstance());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            //Check if the write operation is successful.
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



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
