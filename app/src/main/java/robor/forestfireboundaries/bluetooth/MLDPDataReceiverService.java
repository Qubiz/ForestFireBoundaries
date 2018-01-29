package robor.forestfireboundaries.bluetooth;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.EOFException;
import java.util.LinkedList;
import java.util.Queue;

import okio.Buffer;
import robor.forestfireboundaries.protobuf.HeaderProtos;

/**
 * Created by Mathijs de Groot on 23/01/2018.
 */

public class MLDPDataReceiverService extends Service {

    private static final String TAG = MLDPDataReceiverService.class.getSimpleName();

    public static final String ACTION_MESSAGE_RECEIVED = "robor.forestfireboundaries.bluetooth.ACTION_MESSAGE_RECEIVED";

    private final IBinder binder = new LocalBinder();

    private static Queue<ByteString> messageQueue = new LinkedList<>();
    private static ByteString byteStringBuffer = ByteString.EMPTY;
    private int bytesToRead = -1;
    private int bytesRead = 0;

    boolean inSync = false;

    private Buffer buffer = new Buffer();
    private Buffer messageBuffer = new Buffer();

    public static IntentFilter messageAvailableIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_MESSAGE_RECEIVED);
        return intentFilter;
    }

    @Override
    public void onCreate() {
        super.onCreate();
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

    public void onDataReceived(byte[] data) throws EOFException {
        buffer.write(data);

        if (!inSync) {
            if (buffer.size() >= 4) {
                okio.ByteString byteString = buffer.readByteString();

            }
        }

        /* OLD START */
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
        /* OLD END */
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

    public class LocalBinder extends Binder {
        public MLDPDataReceiverService getService() {
            return MLDPDataReceiverService.this;
        }
    }
}
