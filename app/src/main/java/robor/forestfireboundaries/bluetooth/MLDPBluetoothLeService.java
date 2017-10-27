package robor.forestfireboundaries.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

/**
 * Created by Mathijs de Groot on 26/10/2017.
 */

public class MLDPBluetoothLeService extends Service {

    private static final String TAG = MLDPBluetoothLeService.class.getSimpleName();

    public static final String INTENT_EXTRA_ADDRESS     = "INTENT_EXTRA_ADDRESS";
    public static final String INTENT_EXTRA_NAME        = "INTENT_EXTRA_NAME";
    public static final String INTENT_EXTRA_DATA_STRING = "INTENT_EXTRA_DATA_STRING";
    public static final String INTENT_EXTRA_DATA_BYTES  = "INTENT_EXTRA_DATA_BYTES";

    public static final String ACTION_BLE_SCAN_RESULT   = "ACTION_BLE_SCAN_RESULT";
    public static final String ACTION_BLE_CONNECTED     = "ACTION_BLE_CONNECTED";
    public static final String ACTION_BLE_DISCONNECTED  = "ACTION_BLE_DISCONNECTED";
    public static final String ACTION_BLE_DATA_RECEIVED = "ACTION_BLE_DATA_RECEIVED";

    private final Queue<BluetoothGattDescriptor> descriptorWriteQueue = new LinkedList<>();
    private final Queue<BluetoothGattCharacteristic> characteristicWriteQueue = new LinkedList<>();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScannerCompat bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic mldpDataCharacteristic;
    private BluetoothGattCharacteristic transparentTxDataCharacteristic;
    private BluetoothGattCharacteristic transparentRxDataCharacteristic;

    private int connectionAttemptCountdown = 0;

    private static final int SCAN_MODE = ScanSettings.SCAN_MODE_LOW_LATENCY;
    private static final int REPORT_DELAY = 1000;

    private boolean isScanning = false;

    private final ScanCallback SCAN_CALLBACK = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i(TAG, "onScanResult: " + result.getDevice().getAddress());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            if (!results.isEmpty()) {
                ScanResult scanResult = results.get(0);
                BluetoothDevice bluetoothDevice = scanResult.getDevice();

                final Intent intent = new Intent(ACTION_BLE_SCAN_RESULT);

                intent.putExtra(INTENT_EXTRA_ADDRESS, bluetoothDevice.getAddress());
                intent.putExtra(INTENT_EXTRA_NAME, bluetoothDevice.getName());

                sendBroadcast(intent);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.w(TAG, "onScanFailed: " + errorCode);
        }
    };

    private final BluetoothGattCallback GATT_CALLBACK = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    connectionAttemptCountdown = 0;

                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        final Intent intent = new Intent(ACTION_BLE_CONNECTED);
                        intent.putExtra(gatt.getDevice().getName(), INTENT_EXTRA_NAME);

                        sendBroadcast(intent);

                        Log.i(TAG, "Connected to BLE device");

                        descriptorWriteQueue.clear();
                        characteristicWriteQueue.clear();
                        bluetoothGatt.discoverServices();
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        final Intent intent = new Intent(ACTION_BLE_DISCONNECTED);
                        sendBroadcast(intent);

                        Log.i(TAG, "Disconnected from BLE device");
                    }
                } else {
                    if (connectionAttemptCountdown-- > 0) {
                        bluetoothGatt.connect();

                        Log.d(TAG, "Connection attempt failed, retrying...");
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                        final Intent intent = new Intent(ACTION_BLE_DISCONNECTED);
                        sendBroadcast(intent);

                        Log.i(TAG, "Unexpectedly disconnected from BLE device");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Oops, exception caught in " + e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            try {
                mldpDataCharacteristic = null;
                transparentTxDataCharacteristic = null;
                transparentRxDataCharacteristic = null;

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    List<BluetoothGattService> gattServices = bluetoothGatt.getServices();

                    if (gattServices == null) {
                        Log.d(TAG, "No BLE services found");
                    }

                    UUID uuid;

                    for (BluetoothGattService gattService : gattServices) {
                        uuid = gattService.getUuid();

                        if (uuid.equals(Constants.UUID_MLDP_PRIVATE_SERVICE ) || uuid.equals(Constants.UUID_TANSPARENT_PRIVATE_SERVICE)) {
                            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

                            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                                uuid = gattCharacteristic.getUuid();

                                if (uuid.equals(Constants.UUID_TRANSPARENT_TX_PRIVATE_CHAR)) {
                                    transparentTxDataCharacteristic = gattCharacteristic;

                                    final int characteristicProperties = gattCharacteristic.getProperties();

                                    if ((characteristicProperties & BluetoothGattCharacteristic.PROPERTY_NOTIFY)  > 0) {
                                        bluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);

                                        BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(Constants.UUID_CHAR_NOTIFICATION_DESCRIPTOR);

                                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                                        descriptorWriteQueue.add(descriptor);

                                        if (descriptorWriteQueue.size() == 1) {
                                            bluetoothGatt.writeDescriptor(descriptor);
                                        }
                                    }

                                    if ((characteristicProperties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                                        gattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                                    }

                                    Log.d(TAG, "Found Transparent service Tx characteristics");
                                }

                                if (uuid.equals(Constants.UUID_TRANSPARENT_RX_PRIVATE_CHAR)) {
                                    transparentRxDataCharacteristic = gattCharacteristic;

                                    final int characteristicProperties = gattCharacteristic.getProperties();

                                    if ((characteristicProperties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                                        gattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                                    }

                                    Log.d(TAG, "Found Transparent service Rx characteristics");
                                }

                                if (uuid.equals(Constants.UUID_MLDP_DATA_PRIVATE_CHAR)) {
                                    mldpDataCharacteristic = gattCharacteristic;

                                    final int characteristicProperties = gattCharacteristic.getProperties();

                                    if ((characteristicProperties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {

                                        bluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);

                                        BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(Constants.UUID_CHAR_NOTIFICATION_DESCRIPTOR);

                                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                                        descriptorWriteQueue.add(descriptor);

                                        if(descriptorWriteQueue.size() == 1) {
                                            bluetoothGatt.writeDescriptor(descriptor);
                                        }
                                    }

                                    if ((characteristicProperties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                                        gattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                                    }

                                    Log.d(TAG, "Found MLDP service and characteristics");
                                }
                            }
                            break;
                        }
                    }

                    if (mldpDataCharacteristic == null && transparentTxDataCharacteristic == null || transparentRxDataCharacteristic == null) {
                        Log.d(TAG, "Found MLDP service and characteristics");
                    }
                } else {
                    Log.w(TAG, "Failed service and characteristics");
                }
            } catch (Exception e) {
                Log.e(TAG, "Oops, exception caught in " + e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            try {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.w(TAG, "Error writing GATT characteristic with status: " + status);
                }

                characteristicWriteQueue.remove();

                if(characteristicWriteQueue.size() > 0) {
                    bluetoothGatt.writeCharacteristic(characteristicWriteQueue.element());
                }
            } catch (Exception e) {
                Log.e(TAG, "Oops, exception caught in " + e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            try {
                if (characteristic.getUuid().equals(Constants.UUID_MLDP_DATA_PRIVATE_CHAR) || characteristic.getUuid().equals(Constants.UUID_TRANSPARENT_TX_PRIVATE_CHAR)) {
                    String dataStringValue = characteristic.getStringValue(0);
                    byte[] dataBytesValue = characteristic.getValue();

                    Log.d(TAG, "New notification or indication");

                    final Intent intent = new Intent(ACTION_BLE_DATA_RECEIVED);
                    intent.putExtra(INTENT_EXTRA_DATA_STRING, dataStringValue);
                    intent.putExtra(INTENT_EXTRA_DATA_BYTES, dataBytesValue);
                    sendBroadcast(intent);
                }
            } catch (Exception e) {
                Log.e(TAG, "Oops, exception caught in " + e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());
            }
        }

    };

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            if(bluetoothAdapter != null) {
                bluetoothLeScanner = BluetoothLeScannerCompat.getScanner();
            } else {
                Log.e(TAG, "Unable to obtain a BluetoothAdapter");
            }
        } catch (Exception e) {
            Log.e(TAG, "Oops, exception caught in " + e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());
        }

    }

    @Override
    public void onDestroy() {
        try {
            if (bluetoothGatt != null) {
                bluetoothGatt.close();
                bluetoothGatt = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Oops, exception caught in " + e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void startScan() {
        try {
            isScanning = true;

            ScanSettings scanSettings = new ScanSettings.Builder()
                    .setScanMode(SCAN_MODE)
                    .setReportDelay(REPORT_DELAY)
                    .build();

            ScanFilter scanFilterMLDP = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(Constants.UUID_MLDP_PRIVATE_SERVICE))
                    .build();

            ScanFilter scanFilterTransparent = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(Constants.UUID_TANSPARENT_PRIVATE_SERVICE))
                    .build();

            List<ScanFilter> filters = new ArrayList<>();

            filters.add(scanFilterMLDP);
            filters.add(scanFilterTransparent);

            bluetoothLeScanner.startScan(filters, scanSettings, SCAN_CALLBACK);

        } catch (Exception e) {
            Log.e(TAG, "Oops, exception caught in " + e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());
        }
    }

    public void stopScan() {
        try {
            if (isScanning) {
                isScanning = false;

                bluetoothLeScanner.stopScan(SCAN_CALLBACK);
            }
        } catch (Exception e) {
            Log.e(TAG, "Oops, exception caught in " + e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());
        }
    }

    public boolean connect(final String address) {
        try {
            if (bluetoothAdapter == null || address == null) {
                Log.w(TAG, "BluetoothAdapter is not initialized or unspecified address");
            }

            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            if (bluetoothDevice == null) {
                Log.w(TAG, "Unable to connect because the device was not found");
                return false;
            }

            if (bluetoothGatt != null) {
                bluetoothGatt.close();
            }

            connectionAttemptCountdown = 3;

            bluetoothGatt = bluetoothDevice.connectGatt(this, false, GATT_CALLBACK);

            Log.d(TAG, "Attempting to create a new Bluetooth connection");

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Oops, exception caught in " + e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());
        }
        return false;
    }

    public void disconnect() {
        try {
            if (bluetoothAdapter == null || bluetoothGatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            }

            connectionAttemptCountdown = 0;
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        } catch (Exception e) {
            Log.e(TAG, "Oops, exception caught in " + e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());
        }
    }

    public void writeMLDP(String string) {
        try {
            BluetoothGattCharacteristic writeDataCharacteristic;
            if (mldpDataCharacteristic != null) {
                writeDataCharacteristic = mldpDataCharacteristic;
            } else {
                writeDataCharacteristic = transparentRxDataCharacteristic;
            }

            if (bluetoothAdapter == null || bluetoothGatt == null || writeDataCharacteristic == null) {
                Log.w(TAG, "Write attempted with Bluetooth uninitialized or not connected");
                return;
            }

            writeDataCharacteristic.setValue(string);

            characteristicWriteQueue.add(writeDataCharacteristic);

            if(characteristicWriteQueue.size() == 1){
                if (!bluetoothGatt.writeCharacteristic(writeDataCharacteristic)) {
                    Log.d(TAG, "Failed to write characteristic");
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Oops, exception caught in " + e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());
        }
    }

    public void writeMLDP(byte[] byteValues) {
        try {
            BluetoothGattCharacteristic writeDataCharacteristic;

            if (mldpDataCharacteristic != null) {
                writeDataCharacteristic = mldpDataCharacteristic;
            } else {
                writeDataCharacteristic = transparentRxDataCharacteristic;
            }

            if (bluetoothAdapter == null || bluetoothGatt == null || writeDataCharacteristic == null) {
                Log.w(TAG, "Write attempted with Bluetooth uninitialized or not connected");
                return;
            }

            writeDataCharacteristic.setValue(byteValues);

            characteristicWriteQueue.add(writeDataCharacteristic);

            if(characteristicWriteQueue.size() == 1){
                if (!bluetoothGatt.writeCharacteristic(writeDataCharacteristic)) {
                    Log.d(TAG, "Failed to write characteristic");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Oops, exception caught in " + e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());
        }
    }

    public boolean isScanning() {
        return isScanning;
    }

    public class LocalBinder extends Binder {
        MLDPBluetoothLeService getService() {
            return MLDPBluetoothLeService.this;
        }
    }

    public boolean isBluetoothEnabled() {
        try {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled())  {
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Oops, exception caught in " + e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());
        }
        return false;
    }

    public boolean isConnected() {
        return bluetoothGatt != null;
    }
}
