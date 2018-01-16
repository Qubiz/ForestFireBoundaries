package robor.forestfireboundaries.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleDeviceServices;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;

import javax.annotation.CheckForNull;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class MLDPConnectionService extends Service{

    public static final String ACTION_CONNECTED = "robor.forestfireboundaries.bluetooth.ACTION_CONNECTED";
    public static final String ACTION_CONNECTING = "robor.forestfireboundaries.bluetooth.ACTION_CONNECTING";
    public static final String ACTION_DISCONNECTING = "robor.forestfireboundaries.bluetooth.ACTION_DISCONNECTING";
    public static final String ACTION_DISCONNECTED = "robor.forestfireboundaries.bluetooth.ACTION_DISCONNECTED";
    public static final String ACTION_DATA_RECEIVED = "robor.forestfireboundaries.bluetooth.ACTION_DATA_RECEIVED";
    public static final String INTENT_EXTRA_ADDRESS = "INTENT_EXTRA_ADDRESS";
    public static final String INTENT_EXTRA_NAME = "INTENT_EXTRA_NAME";
    private static final String TAG = MLDPConnectionService.class.getSimpleName();
    private final IBinder binder = new LocalBinder();

    private RxBleClient rxBleClient;
    private RxBleDevice rxBleDevice;
    private RxBleConnection rxBleConnection;

    private Subscription connectionStateSubscription;
    private Subscription connectionSubscription;

    private BluetoothGattCharacteristic mldpDataCharacteristic;
    private BluetoothGattCharacteristic mldpControlCharacteristic;
    private BluetoothGattCharacteristic transparentTxDataCharacteristic;
    private BluetoothGattCharacteristic transparentRxDataCharacteristic;

    private static boolean receivingDataFlag = false;
    private static boolean dataAvailable = false;

    static ByteArrayOutputStream stream;

    @Override
    public void onCreate() {
        super.onCreate();
        rxBleClient = RxBleClient.create(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void connect(final String macAddress) {
        rxBleDevice = rxBleClient.getBleDevice(macAddress);

        connectionStateSubscription = rxBleDevice.observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionStateChanged, this::onObserveConnectionStateChangeFailure);

        connectionSubscription = rxBleDevice.establishConnection(false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionReceived, this::onConnectionFailure);

    }

    private void onObserveConnectionStateChangeFailure(Throwable throwable) {
        Log.d(TAG, "onObserveConnectionStateChangeFailure", throwable);
    }

    private void onConnectionReceived(RxBleConnection rxBleConnection) {
        this.rxBleConnection = rxBleConnection;
        rxBleConnection.discoverServices()
                .subscribe(this::onDiscoveredServicesReceived, this::onDiscoverServicesFailure);
    }

    private void onDiscoverServicesFailure(Throwable throwable) {
        Log.d(TAG, "onDiscoveredServicesFailure", throwable);
    }

    private void onDiscoveredServicesReceived(RxBleDeviceServices rxBleDeviceServices) {
        List<BluetoothGattService> gattServices = rxBleDeviceServices.getBluetoothGattServices();
        UUID uuid;

        if (gattServices != null) {
            for (BluetoothGattService gattService : gattServices) {
                uuid = gattService.getUuid();

                if (uuid.equals(Constants.UUID_MLDP_PRIVATE_SERVICE ) || uuid.equals(Constants.UUID_TRANSPARENT_PRIVATE_SERVICE)) {
                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        uuid = gattCharacteristic.getUuid();

                        if (uuid.equals(Constants.UUID_TRANSPARENT_TX_PRIVATE_CHAR)) {
                            transparentTxDataCharacteristic = gattCharacteristic;

                            if (isCharacteristicNotifiable(transparentTxDataCharacteristic)) {
                                BluetoothGattDescriptor descriptor = transparentTxDataCharacteristic.getDescriptor(Constants.UUID_CHAR_NOTIFICATION_DESCRIPTOR);
                                rxBleConnection.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            }

                            if (isCharacteristicWriteable(transparentTxDataCharacteristic)) {
                                transparentTxDataCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                            }

                            Log.d(TAG, "Found Transparent service Tx characteristics.");
                        }

                        if (uuid.equals(Constants.UUID_TRANSPARENT_RX_PRIVATE_CHAR)) {
                            transparentRxDataCharacteristic = gattCharacteristic;

                            if (isCharacteristicWriteable(transparentRxDataCharacteristic)) {
                                transparentRxDataCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                            }

                            Log.d(TAG, "Found Transparent service Rx characteristics");
                        }

                        if (uuid.equals(Constants.UUID_MLDP_DATA_PRIVATE_CHAR)) {
                            mldpDataCharacteristic = gattCharacteristic;

                            if (isCharacteristicNotifiable(mldpDataCharacteristic)) {
                                BluetoothGattDescriptor descriptor = mldpDataCharacteristic.getDescriptor(Constants.UUID_CHAR_NOTIFICATION_DESCRIPTOR);
                                rxBleConnection.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            }

                            if (isCharacteristicWriteable(mldpDataCharacteristic)) {
                                mldpDataCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                            }

                            rxBleConnection.setupNotification(mldpDataCharacteristic)
                                    .doOnNext(observable -> {
                                        Log.d(TAG, "Noticiations have been set up.");
                                    })
                                    .flatMap(observable -> observable)
                                    .subscribe(this::onDataReceived);

                            Log.d(TAG, "Found MLDP service and characteristics");
                        }
                    }
                    break;
                }
            }
            if (mldpDataCharacteristic == null && (transparentTxDataCharacteristic == null || transparentRxDataCharacteristic == null)) {
                Log.d(TAG, "All characteristics are null.");
            }
        } else {
            // TODO: gattServices == null
        }
    }

    public void disconnect() {
        if (isConnected()) {
            if (connectionSubscription != null) {
                connectionSubscription.unsubscribe();
                connectionSubscription = null;
            }
        }
    }

    public boolean isConnected() {
        return rxBleDevice != null && rxBleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    public RxBleDevice getConnectedDevice() {
        if (isConnected()) {
            return rxBleDevice;
        } else {
            return null;
        }
    }

    private void onConnectionStateChanged(RxBleConnection.RxBleConnectionState state) {
        Intent intent = new Intent();

        Log.d(TAG, state.toString());
        switch (state) {
            case CONNECTING:
                intent.setAction(ACTION_CONNECTING);
                break;
            case CONNECTED:
                intent.setAction(ACTION_CONNECTED);
                break;
            case DISCONNECTED:
                intent.setAction(ACTION_DISCONNECTED);
                break;
            case DISCONNECTING:
                intent.setAction(ACTION_DISCONNECTING);
                break;
        }

        intent.putExtra(INTENT_EXTRA_NAME, rxBleDevice.getName());
        intent.putExtra(INTENT_EXTRA_ADDRESS, rxBleDevice.getMacAddress());
        sendBroadcast(intent);
    }

    private void onConnectionFailure(Throwable throwable) {
        Log.d(TAG,"onConnectionFailure", throwable);
    }

    private void onDataReceived(byte[] data) {
        if (!receivingDataFlag) {
            receivingDataFlag = true;
            if (stream == null) {
                stream = new ByteArrayOutputStream();
            } else {
                stream.reset();
            }
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
        }

        Log.d(TAG, sb.toString());

        ByteString byteString = ByteString.copyFrom(data);

        try {
            byteString.writeTo(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }


//        if(byteString.endsWith(ByteString.copyFrom())) {
//            Intent intent = new Intent();
//
//            intent.setAction(ACTION_DATA_RECEIVED);
//
//            sendBroadcast(intent);
//
//            receivingDataFlag = false;
//            dataAvailable = true;
//        }
    }

    public void writeMLDP(String string) {
        writeMLDP(string.getBytes());
    }

    public void writeMLDP(byte[] bytes) {
        BluetoothGattCharacteristic writeDataCharacteristic;
        if (mldpDataCharacteristic != null) {
            writeDataCharacteristic = mldpDataCharacteristic;
        } else {
            writeDataCharacteristic = transparentRxDataCharacteristic;
        }

        rxBleConnection.writeCharacteristic(writeDataCharacteristic, bytes)
            .subscribe(this::onDataWritten, this::onDataWriteFailure);
    }

    private void onDataWriteFailure(Throwable throwable) {
        Log.d(TAG, throwable.getMessage());
        // TODO: Handle data write failures
    }

    private void onDataWritten(byte[] bytes) {
        Log.d(TAG, new String(bytes));
    }

    private boolean isCharacteristicNotifiable(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }

    private boolean isCharacteristicReadable(BluetoothGattCharacteristic characteristic) {
        return ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0);
    }

    private boolean isCharacteristicWriteable(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE
                | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0;
    }

    public static byte[] getAvailableData() {
        Log.d(TAG, "getAvailableData()");
        byte[] data;
        if (isDataAvailable()) {
            data = stream.toByteArray();
        } else {
            data = null;
        }

        dataAvailable = false;
        return data;
    }

    public static boolean isDataAvailable() {
        return dataAvailable;
    }

    public static boolean isReceivingData() {
        return receivingDataFlag;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    public class LocalBinder extends Binder {
        public MLDPConnectionService getService() {
            return MLDPConnectionService.this;
        }
    }
}
