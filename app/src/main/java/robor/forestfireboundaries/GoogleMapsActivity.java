package robor.forestfireboundaries;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import robor.forestfireboundaries.bluetooth.MLDPBluetoothLeService;

/**
 * Created by xborre on 27/10/2017.
 */

public class GoogleMapsActivity extends AppCompatActivity implements OnMapReadyCallback{

    private MapFragment mapFragment;
    private GoogleMap googleMap;

    private TextView connectionStatusTextView;
    private TextView connectedDeviceNameTextView;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(MLDPBluetoothLeService.ACTION_BLE_CONNECTED)) {
                connectedDeviceNameTextView.setText(intent.getStringExtra(MLDPBluetoothLeService.INTENT_EXTRA_NAME));
                connectionStatusTextView.setText("Status: connected");
            }

            if (action.equals(MLDPBluetoothLeService.ACTION_BLE_DISCONNECTED)) {
                connectionStatusTextView.setText("Status: disconnected");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);

        connectedDeviceNameTextView = (TextView) findViewById(R.id.connected_device_name);
        connectionStatusTextView = (TextView) findViewById(R.id.device_connection_status);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registerReceiver(broadcastReceiver, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        this.googleMap = googleMap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
