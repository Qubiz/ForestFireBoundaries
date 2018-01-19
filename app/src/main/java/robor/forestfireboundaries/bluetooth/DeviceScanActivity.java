package robor.forestfireboundaries.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.internal.RxBleLog;
import com.polidea.rxandroidble.scan.ScanFilter;
import com.polidea.rxandroidble.scan.ScanSettings;

import butterknife.BindView;
import butterknife.ButterKnife;
import robor.forestfireboundaries.BaseApplication;
import robor.forestfireboundaries.GoogleMapsActivity;
import robor.forestfireboundaries.R;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class DeviceScanActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, ScanResultsAdapter.OnScanResultAddedListener {

    private static final String TAG = DeviceScanActivity.class.getSimpleName();

    private static final String STATUS_SCANNING = "Scanning...";
    private static final String STATUS_NO_DEVICES_FOUND = "No devices found, please retry a scan...";

    public static final int REQUEST_ENABLE_BLUETOOTH = 1;
    public static final int REQUEST_PERMISSION_LOCATION = 2;

    private static final long SCAN_TIME = 5000;

    private static final ScanSettings SCAN_SETTINGS = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build();

    private static final ScanFilter SCAN_FILTER_MLDP_PRIVATE_SERVICE = new ScanFilter.Builder()
            .setServiceUuid(new ParcelUuid(Constants.UUID_MLDP_PRIVATE_SERVICE))
            .build();

    private static final ScanFilter SCAN_FILTER_TRANSPARENT_PRIVATE_SERVICE = new ScanFilter.Builder()
            .setServiceUuid(new ParcelUuid(Constants.UUID_TRANSPARENT_PRIVATE_SERVICE))
            .build();

    private ScanResultsAdapter  scanResultsAdapter;
    private RxBleClient         rxBleClient;
    private Subscription        scanSubscription;
    private Handler             stopScanHandler;

    @BindView(R.id.status_text_field)   TextView    statusTextField;
    @BindView(R.id.progress_bar)        ProgressBar progressBar;
    @BindView(R.id.devices_list_view)   ListView    devicesListView;
    @BindView(R.id.continue_button)     Button      continueButton;

    private boolean isBusy = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        setContentView(R.layout.activity_device_scan);
        ButterKnife.bind(this);

        rxBleClient = RxBleClient.create(this);
        RxBleClient.setLogLevel(RxBleLog.DEBUG);

        scanResultsAdapter = new ScanResultsAdapter(this, this, this);

        devicesListView.setAdapter(scanResultsAdapter);
        devicesListView.setOnItemClickListener(this);

        stopScanHandler = new Handler();

        continueButton.setOnClickListener(v -> {
            final Intent intent = new Intent(this, GoogleMapsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        registerReceiver(connectionStateReceiver, MLDPConnectionService.connectionStateIntentFilter());
//        registerReceiver(dataAvailableReceiver, MLDPConnectionService.messageAvailableIntentFilter());

        startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "onPause()");

        unregisterReceiver(connectionStateReceiver);
//        unregisterReceiver(dataAvailableReceiver);

        if (isScanning()) {
            clearSubscription();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop()");

        if (isScanning()) {
            clearSubscription();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy()");

        if (isScanning()) {
            clearSubscription();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_device_scan_menu, menu);

        menu.findItem(R.id.menu_item_in_progress).setActionView(new ProgressBar(this));

        if (isBusy) {
            menu.findItem(R.id.menu_item_scan).setVisible(false);
            menu.findItem(R.id.menu_item_in_progress).setVisible(true);
        } else {
            menu.findItem(R.id.menu_item_scan).setVisible(true);
            menu.findItem(R.id.menu_item_in_progress).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_scan:
                if (!isScanning()) {
                    startScan();
                }
                return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        setBusy(5000);

        final RxBleDevice device = scanResultsAdapter.getItem(position);
        if (BaseApplication.isMLDPConnectionServiceBound()) {
            if (BaseApplication.getMLDPConnectionService().isConnected()) {
                RxBleDevice connectedDevice = BaseApplication.getMLDPConnectionService().getConnectedDevice();
                if (connectedDevice.getMacAddress().equals(device.getMacAddress())) {
                    BaseApplication.getMLDPConnectionService().disconnect();
                    Log.d(TAG, "(DISCONNECT) " + device.getMacAddress());
                } else {
                    Log.d(TAG, "(CONNECT) OLD: " + connectedDevice.getMacAddress() + " NEW: " + device.getMacAddress());
                    BaseApplication.getMLDPConnectionService().disconnect();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            BaseApplication.getMLDPConnectionService().connect(device.getMacAddress());
                        }
                    },1500);
                }
            } else {
                Log.d(TAG, "(CONNECT) " + device.getMacAddress());
                BaseApplication.getMLDPConnectionService().connect(device.getMacAddress());
            }
        }
    }

    private boolean ready() {
        switch (rxBleClient.getState()) {
            case READY:
                return true;
            case BLUETOOTH_NOT_AVAILABLE:
                return false;
            case BLUETOOTH_NOT_ENABLED:
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);
                return false;
            case LOCATION_PERMISSION_NOT_GRANTED:
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
                return false;
            case LOCATION_SERVICES_NOT_ENABLED:
                Toast.makeText(this, "Please enable Location Services", Toast.LENGTH_SHORT).show();
                return false;
        }
        return false;
    }

    private void startScan() {
        if (ready()) {
            scanResultsAdapter.clearScanResults();
            if (BaseApplication.getMLDPConnectionService().isConnected()) {
                scanResultsAdapter.addDevice(BaseApplication.getMLDPConnectionService().getConnectedDevice());
                continueButton.setEnabled(true);
            }

            statusTextField.setText(STATUS_SCANNING);

            scanSubscription = rxBleClient.scanBleDevices(SCAN_SETTINGS, SCAN_FILTER_MLDP_PRIVATE_SERVICE, SCAN_FILTER_TRANSPARENT_PRIVATE_SERVICE)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnUnsubscribe(() -> Log.d(TAG, "onUnsubscribe"))
                    .subscribe(scanResultsAdapter::addScanResult, this::onScanFailure);

            setBusy(SCAN_TIME);

            ProgressBarAnimation progressBarAnimation = new ProgressBarAnimation(progressBar, 0, 100);
            progressBarAnimation.setDuration(SCAN_TIME);

            progressBar.startAnimation(progressBarAnimation);

            stopScanHandler.postDelayed(this::stopScan, SCAN_TIME);
        }
    }

    @SuppressLint("SetTextI18n")
    private void stopScan() {
        if (isScanning()) {
            clearSubscription();
        }

        invalidateOptionsMenu();

        if (scanResultsAdapter.getCount() == 0) {
            statusTextField.setText(STATUS_NO_DEVICES_FOUND);
        }

        if (BaseApplication.getMLDPConnectionService().isConnected()) {
            String status = "Connected to " + BaseApplication.getMLDPConnectionService().getConnectedDevice().getName();
            statusTextField.setText(status);
        }

    }

    private void clearSubscription() {
        scanSubscription.unsubscribe();
        scanSubscription = null;
    }

    private boolean isScanning() {
        return scanSubscription != null;
    }

    private void onScanFailure(Throwable throwable) {
        if (throwable instanceof BleScanException) {
            Log.d(TAG, throwable.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    startScan();
                } else {
                    Log.e(TAG, "Could not enable Bluetooth (result code: " + resultCode + ").");
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScan();
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onScanResultAdded(RxBleDevice rxBleDevice) {
        Log.d(TAG, "Scan result added: " + rxBleDevice.getName() + "(" + rxBleDevice.getMacAddress() + ")");

        int count = scanResultsAdapter.getCount();

        if (count == 1) {
            statusTextField.setText("Found " + count + " device...");
        } else {
            statusTextField.setText("Found " + count + " devices...");
        }
    }

    private class ProgressBarAnimation extends Animation {
        private ProgressBar progressBar;
        private float from;
        private float to;

        ProgressBarAnimation(ProgressBar progressBar, float from, float to) {
            super();
            this.progressBar = progressBar;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float value = from + (to - from) * interpolatedTime;
            progressBar.setProgress((int) value);
        }
    }

    private BroadcastReceiver connectionStateReceiver = new BroadcastReceiver() {
        String status = "";
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case MLDPConnectionService.ACTION_CONNECTED:
                        continueButton.setEnabled(true);
                        status = "Connected to " + intent.getStringExtra(MLDPConnectionService.INTENT_EXTRA_NAME);
                        break;
                    case MLDPConnectionService.ACTION_DISCONNECTED:
                        continueButton.setEnabled(false);
                        status = "Disconnected from " + intent.getStringExtra(MLDPConnectionService.INTENT_EXTRA_NAME);
                        break;
                    case MLDPConnectionService.ACTION_CONNECTING:
                        continueButton.setEnabled(false);
                        status = "Connecting to " + intent.getStringExtra(MLDPConnectionService.INTENT_EXTRA_NAME);
                        break;
                    case MLDPConnectionService.ACTION_DISCONNECTING:
                        continueButton.setEnabled(false);
                        status = "Disconnecting from " + intent.getStringExtra(MLDPConnectionService.INTENT_EXTRA_NAME);
                        break;
                }
            }
            statusTextField.setText(status);
            scanResultsAdapter.notifyDataSetChanged();
        }
    };

    private void setBusy(long time) {
        if (time > 0) {
            isBusy = true;
            devicesListView.setOnItemClickListener(null);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setBusy(0);
                }
            }, time);
        } else {
            isBusy = false;
            devicesListView.setOnItemClickListener(this);
        }

        invalidateOptionsMenu();
    }
}
