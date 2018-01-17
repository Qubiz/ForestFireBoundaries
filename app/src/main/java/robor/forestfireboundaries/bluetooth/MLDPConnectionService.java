package robor.forestfireboundaries.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleDeviceServices;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import robor.forestfireboundaries.protobuf.HeaderProtos;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class MLDPConnectionService extends Service{

    public static final String ACTION_CONNECTED = "robor.forestfireboundaries.bluetooth.ACTION_CONNECTED";
    public static final String ACTION_CONNECTING = "robor.forestfireboundaries.bluetooth.ACTION_CONNECTING";
    public static final String ACTION_DISCONNECTING = "robor.forestfireboundaries.bluetooth.ACTION_DISCONNECTING";
    public static final String ACTION_DISCONNECTED = "robor.forestfireboundaries.bluetooth.ACTION_DISCONNECTED";
    public static final String ACTION_MESSAGE_RECEIVED = "robor.forestfireboundaries.bluetooth.ACTION_MESSAGE_RECEIVED";
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

    private static boolean newMessage = false;
    private static boolean dataAvailable = false;
    private static ByteString byteStringBuffer = ByteString.EMPTY;
    private int bytesToRead = -1;
    private int bytesRead = 0;

    private static Queue<ByteString> messageQueue = new LinkedList<>();

    public static IntentFilter connectionStateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CONNECTED);
        intentFilter.addAction(ACTION_DISCONNECTING);
        intentFilter.addAction(ACTION_DISCONNECTED);
        intentFilter.addAction(ACTION_CONNECTING);
        return intentFilter;
    }

    public static IntentFilter messageAvailableIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MLDPConnectionService.ACTION_MESSAGE_RECEIVED);
        return intentFilter;
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

    /**
     * Incoming data looks like:
     *
     *  [ HEADER ] [ MESSAGE ] [ ... ]
     *
     * Header [10 bytes]:
     * - MessageID (int32)
     * - MessageLength (int32)
     *
     * Message [MessageLength]:
     * - Data
     *
     * @param data
     */
    private void onDataReceived(byte[] data) {
        byteStringBuffer = byteStringBuffer.concat(ByteString.copyFrom(data));
        bytesRead += data.length;

        if (bytesRead >= 10 && bytesToRead == -1) {
            try {
                HeaderProtos.Header header = HeaderProtos.Header.parseFrom(byteStringBuffer.substring(0, 10));
                messageQueue.add(ByteString.copyFrom(header.toByteArray()));
                bytesToRead = header.getMessageLength();
                byteStringBuffer = byteStringBuffer.substring(10);
                Log.d(TAG, "The header has been fully received...");
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        } else if (bytesToRead != -1) {
            // Header has been read, therefore the length of the upcoming message is known.
            if (bytesRead - 10 < bytesToRead) {
                // The complete message has not yet been received.
                // TODO: Create a timeout??
                Log.d(TAG, "The message has not yet been fully received... [" + (bytesRead - 10) + " / " + bytesToRead + "] bytes");
            } else {
                Log.d(TAG, "Message has been fully received...");
                // We have read the complete message or more.

                // Get the message from the buffer
                ByteString message = byteStringBuffer.substring(0, bytesToRead);

                // Add the message to the queue
                messageQueue.add(message);

                // Notify (broadcast) that there is a new message available.
                Intent intent = new Intent();
                intent.setAction(ACTION_MESSAGE_RECEIVED);
                sendBroadcast(intent);

                // Remove the read message from the buffer
                byteStringBuffer = byteStringBuffer.substring(bytesToRead);

                // Reset bytesToRead
                bytesToRead = -1;

                // Update the number of bytes that are left in the buffer.
                bytesRead = byteStringBuffer.size();
            }
        } else {
            // The header has not been completely read
            // TODO: Create a timeout??
            Log.d(TAG, "Header has not yet been fully received... [" + bytesRead + " / 10] bytes" );
        }


//        StringBuilder sb = new StringBuilder();
//        for (byte b : data) {
//            sb.append(String.format("%02X ", b));
//        }
//
//        Log.d(TAG, sb.toString());
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

    public static ByteString getNextAvailableMessage() {
        Log.d(TAG, "getNextAvailableMessage()");

        ByteString data;
        if (isDataAvailable()) {
            data = messageQueue.remove();
        } else {
            data = null;
        }

        return data;
    }

    public static boolean isDataAvailable() {
        return !messageQueue.isEmpty();
    }

    public static boolean isReceivingData() {
        return newMessage;
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
