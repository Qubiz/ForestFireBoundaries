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

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleDeviceServices;

import java.util.List;
import java.util.UUID;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class MLDPConnectionService extends Service{

    private static final String TAG = MLDPConnectionService.class.getSimpleName();

    private final IBinder binder = new LocalBinder();

    private RxBleClient rxBleClient;
    private RxBleDevice rxBleDevice;
    private RxBleConnection rxBleConnection;

    private Subscription connectionStateSubscription;
    private Subscription connectionSubscription;

    private BluetoothGattCharacteristic mldpDataCharacteristic;
    private BluetoothGattCharacteristic transparentTxDataCharacteristic;
    private BluetoothGattCharacteristic transparentRxDataCharacteristic;

    public class LocalBinder extends Binder {
        MLDPConnectionService getService() {
            return MLDPConnectionService.this;
        }
    }

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
                .subscribe(this::onConnectionStateChanged);

        connectionSubscription = rxBleDevice.establishConnection(false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionReceived, this::onConnectionFailure);

    }

    private void onConnectionReceived(RxBleConnection rxBleConnection) {
        this.rxBleConnection = rxBleConnection;
        rxBleConnection.discoverServices()
                .subscribe(this::onDiscoveredServicesReceived);
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

                            Log.d(TAG, "Found MLDP service and characteristics");
                        }
                    }
                    break;
                }
            }
            if (mldpDataCharacteristic == null && transparentTxDataCharacteristic == null || transparentRxDataCharacteristic == null) {
                Log.d(TAG, "All characteristics are null.");
            }
        } else {
            // TODO: gattServices == null
        }
    }

    public void disconnect() {
        if (isConnected()) {
           connectionSubscription.unsubscribe();
           connectionSubscription = null;
        }
    }

    public boolean isConnected() {
        return rxBleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    private void onConnectionStateChanged(RxBleConnection.RxBleConnectionState state) {
        // TODO:  Handle the connection state changes
        Log.d(TAG, state.toString());
        switch (state) {
            case CONNECTING:
                break;
            case CONNECTED:
                break;
            case DISCONNECTED:
                break;
            case DISCONNECTING:
                break;
        }
    }

    private void onConnectionFailure(Throwable throwable) {
        // TODO: Handle connection failures
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
        // TODO: Handle data write failures
    }

    private void onDataWritten(byte[] bytes) {
        Log.d(TAG, "Written data: " + new String(bytes));
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
}
