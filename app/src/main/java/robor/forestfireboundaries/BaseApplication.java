package robor.forestfireboundaries;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import robor.forestfireboundaries.bluetooth.MLDPConnectionService;

/**
 * Created by Mathijs de Groot on 30/10/2017.
 */

public class BaseApplication extends Application {

    private static final String TAG = BaseApplication.class.getSimpleName();

    private static MLDPConnectionService mldpConnectionService;
    private static boolean mldpConnectionServiceBound = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Intent intent = new Intent(this, MLDPConnectionService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MLDPConnectionService.LocalBinder binder = (MLDPConnectionService.LocalBinder) service;
            mldpConnectionService = binder.getService();
            mldpConnectionServiceBound = true;

            Log.d(TAG, "Connected to MLDPConnectionService.");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mldpConnectionServiceBound = false;

            Log.d(TAG, "Disconnected from MLDPConnectionService.");
        }
    };

    public static MLDPConnectionService getMLDPConnectionService() {
        return mldpConnectionService;
    }

    public static boolean isMLDPConnectionServiceBound() {
        return mldpConnectionServiceBound;
    }

}


