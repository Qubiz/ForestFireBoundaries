package robor.forestfireboundaries;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import robor.forestfireboundaries.bluetooth.MLDPConnectionService;
import robor.forestfireboundaries.bluetooth.MLDPDataReceiverService;

/**
 * Created by Mathijs de Groot on 30/10/2017.
 */

public class BaseApplication extends Application {

    private static final String TAG = BaseApplication.class.getSimpleName();

    private static MLDPConnectionService mldpConnectionService;
    private static MLDPDataReceiverService mldpDataReceiverService;

    private static boolean mldpConnectionServiceBound = false;
    private static boolean mldpDataReceiverBound = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Intent intent = new Intent(this, MLDPConnectionService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        intent = new Intent(this, MLDPDataReceiverService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (name.getClassName().equals(MLDPConnectionService.class.getName())) {
                MLDPConnectionService.LocalBinder binder = (MLDPConnectionService.LocalBinder) service;
                mldpConnectionService = binder.getService();
                mldpConnectionServiceBound = true;

                Log.d(TAG, "Connected to MLDPConnectionService.");
            }

            if (name.getClassName().equals(MLDPDataReceiverService.class.getName())) {
                MLDPDataReceiverService.LocalBinder binder = (MLDPDataReceiverService.LocalBinder) service;
                mldpDataReceiverService = binder.getService();
                mldpDataReceiverBound = true;

                Log.d(TAG, "Connected to MLDPDataReceiverService.");
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (name.getClassName().equals(MLDPConnectionService.class.getName())) {
                mldpConnectionServiceBound = false;

                Log.d(TAG, "Disconnected from MLDPConnectionService.");
            }

            if (name.getClassName().equals(MLDPDataReceiverService.class.getName())) {
                mldpDataReceiverBound = false;

                Log.d(TAG, "Disconnected from MLDPDataReceiverService.");
            }
        }
    };

    public static MLDPConnectionService getMLDPConnectionService() {
        return mldpConnectionService;
    }
    public static MLDPDataReceiverService getMldpDataReceiverService() {
        return mldpDataReceiverService;
    }

    public static boolean isMLDPConnectionServiceBound() {
        return mldpConnectionServiceBound;
    }
    public static boolean isMldpDataReceiverBound() { return mldpDataReceiverBound; }



}


