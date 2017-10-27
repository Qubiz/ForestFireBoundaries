package robor.forestfireboundaries.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import robor.forestfireboundaries.GoogleMapsActivity;
import robor.forestfireboundaries.MainActivity;
import robor.forestfireboundaries.R;

/**
 * Created by Mathijs de Groot on 26/10/2017.
 */

public class DeviceScanActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = MLDPBluetoothLeService.class.getSimpleName();

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int REQUEST_PERMISSION_LOCATION = 2;

    private static final long SCAN_TIME = 10000;

    private MLDPBluetoothLeService bleService;

    private DeviceListAdapter deviceListAdapter;

    private ProgressBar progressBar;
    private ProgressBarAnimation progressBarAnimation;

    private ServiceConnection bleServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            MLDPBluetoothLeService.LocalBinder binder = (MLDPBluetoothLeService.LocalBinder) service;
            bleService = binder.getService();
            startScan();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bleService = null;
        }
    };

    private BroadcastReceiver bleServiceReceiever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(MLDPBluetoothLeService.ACTION_BLE_SCAN_RESULT.equals(action)) {
                Log.d(TAG, "Scan results received");

                final String address = intent.getStringExtra(MLDPBluetoothLeService.INTENT_EXTRA_ADDRESS);
                final String name = intent.getStringExtra(MLDPBluetoothLeService.INTENT_EXTRA_NAME);

                final BLEDevice device = new BLEDevice(address, name);

                deviceListAdapter.addDevice(device);
                deviceListAdapter.notifyDataSetChanged();
            }
        }
    };

    private Handler scanStopHandler;

    private ListView devicesListView;
    private TextView statusTextView;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        deviceListAdapter = new DeviceListAdapter(this);

        devicesListView = (ListView) findViewById(R.id.devices_list_view);
        devicesListView.setAdapter(deviceListAdapter);
        devicesListView.setOnItemClickListener(this);

        progressBar = findViewById(R.id.progress_bar);
        progressBarAnimation = new ProgressBarAnimation(progressBar, 0, 100);
        progressBarAnimation.setDuration(SCAN_TIME);
        progressBar.setAnimation(progressBarAnimation);

        statusTextView = findViewById(R.id.status_text_field);

        continueButton = findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchActivity();
            }
        });

        scanStopHandler = new Handler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_device_scan_menu, menu);

        menu.findItem(R.id.menu_item_in_progress).setActionView(new ProgressBar(this));

        if (bleService != null && bleService.isScanning()) {
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
        switch(item.getItemId()) {
            case R.id.menu_item_scan:
                if(!bleService.isScanning()) {
                    startScan();
                }
                return true;
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(bleServiceReceiever);
    }

    @Override
    protected void onResume() {
        super.onResume();
        prepare();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(bleServiceConnection);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                prepare();
            } else {
                onBackPressed();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void prepare() {
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

            if (bluetoothManager != null) {
                if (bluetoothManager.getConnectedDevices(BluetoothProfile.GATT) != null) {
                    for (BluetoothDevice device : bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)) {
                        BLEDevice bleDevice = new BLEDevice(device.getAddress(), device.getName());
                        bleDevice.setConnected(true);
                        deviceListAdapter.addDevice(bleDevice);
                    }
                }
            }

            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if(bluetoothAdapter != null) {
                if (bluetoothAdapter.isEnabled()) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Intent bleServiceIntent = new Intent(this, MLDPBluetoothLeService.class);
                        bindService(bleServiceIntent, bleServiceConnection, BIND_AUTO_CREATE);

                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction(MLDPBluetoothLeService.ACTION_BLE_SCAN_RESULT);
                        registerReceiver(bleServiceReceiever, intentFilter);
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
                    }
                } else {
                    Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);
                }
            }
        } else {
            Toast.makeText(this, "BLE NOT SUPPORTED", Toast.LENGTH_SHORT).show();
        }
    }

    private Runnable scanStop = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };

    private void startScan() {
        if (!bleService.isScanning()) {
            if (bleService.isBluetoothEnabled()) {
                deviceListAdapter.clear();
                statusTextView.setText("Scanning...");
                bleService.startScan();
                invalidateOptionsMenu();
                progressBar.startAnimation(progressBarAnimation);
                scanStopHandler.postDelayed(scanStop, SCAN_TIME);
            } else {
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);
            }
        }
    }

    private void stopScan() {
        if (bleService.isScanning()) {
            bleService.stopScan();
            deviceListAdapter.notifyDataSetChanged();
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        final BLEDevice device = deviceListAdapter.getItem(position);

        scanStopHandler.removeCallbacks(scanStop);
        stopScan();
        final Intent intent = new Intent();

        if (device == null) {
            setResult(Activity.RESULT_CANCELED, intent);
        } else {
            // TODO: Do something when a connection is made
            if (!bleService.isConnected()) {
                if (bleService.connect(device.getAddress())) {
                    device.setConnected(true);
                    statusTextView.setText("Connected to " + device.getName());
                } else {
                    statusTextView.setText("Could not connect to " + device.getName());
                    continueButton.setEnabled(false);
                }
            } else {
                bleService.disconnect();
                device.setConnected(false);
                statusTextView.setText("Disconnected from " + device.getName());
                continueButton.setEnabled(false);
            }
        }

        deviceListAdapter.notifyDataSetChanged();
    }

    private class DeviceListAdapter extends ArrayAdapter<BLEDevice> {

        private ArrayList<BLEDevice> devices;
        private Context context;

        public DeviceListAdapter(@NonNull Context context) {
            super(context,0);
            this.context = context;
            devices = new ArrayList<>();
        }

        public void addDevice(BLEDevice device) {
            if (!devices.contains(device)) {
                devices.add(device);
                notifyDataSetChanged();
            }
        }

        @Nullable
        @Override
        public BLEDevice getItem(int position) {
            return devices.get(position);
        }

        public void clear() {
            for (BLEDevice device : devices) {
                if (!device.isConnected()) {
                    devices.remove(device);
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                convertView = inflater.inflate(R.layout.scan_list_item, parent, false);
            }

            BLEDevice device = devices.get(position);

            TextView textViewAddress = (TextView) convertView.findViewById(R.id.device_address);
            textViewAddress.setText(device.getAddress());

            TextView textViewName = (TextView) convertView.findViewById(R.id.device_name);

            if (device.getName() != null) {
                textViewName.setText(device.getName());
            } else {
                textViewName.setText("Unkown");
            }

            ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);

            if (device.isConnected()) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.icons8_checkmark));
            } else {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.icons8_bluetooth));
            }

            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            if (devices.isEmpty() && bleService != null && !bleService.isScanning()) {
                statusTextView.setText("No devices found, please retry a scan...");
            } else {
                if (devices.size() == 1) {
                    statusTextView.setText("Found " + devices.size() + " device...");
                } else {
                    statusTextView.setText("Found " + devices.size() + " devices...");
                }
            }

            for(BLEDevice device : devices) {
                if (device.isConnected()) {
                    statusTextView.setText("Connected to " + device.getName());
                    continueButton.setEnabled(true);
                    break;
                }
            }
        }

        public BLEDevice getConnectedDevice() {
            for (BLEDevice device : devices) {
                if (device.isConnected()) {
                    return device;
                }
            }
            return null;
        }

        public ArrayList<BLEDevice> getDevices() {
            return devices;
        }
    }

    private void switchActivity() {
        final Intent intent = new Intent(this, GoogleMapsActivity.class);
        BLEDevice connectedDevice = deviceListAdapter.getConnectedDevice();
        if (connectedDevice != null) {
            intent.putExtra(deviceListAdapter.getConnectedDevice().getName(), MLDPBluetoothLeService.INTENT_EXTRA_NAME);
            intent.putExtra(deviceListAdapter.getConnectedDevice().getAddress(), MLDPBluetoothLeService.INTENT_EXTRA_ADDRESS);
            startActivity(intent);
        } else {
            Toast.makeText(this, "No device connected", Toast.LENGTH_SHORT).show();
        }
    }

    private class BLEDevice {
        private String address;
        private String name;
        private boolean connected;

        public BLEDevice(String address, String name) {
            this.address = address;
            this.name = name;
            connected = false;
        }

        public String getAddress() {
            return address;
        }

        public String getName() {
            return name;
        }

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        @Override
        public boolean equals(Object object) {
            if (object != null && object instanceof BLEDevice) {
                return this.address.equals(((BLEDevice) object).address);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.address.hashCode();
        }
    }

    private class ProgressBarAnimation extends Animation {
        private ProgressBar progressBar;
        private float from;
        private float to;

        public ProgressBarAnimation(ProgressBar progressBar, float from, float to) {
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
}
