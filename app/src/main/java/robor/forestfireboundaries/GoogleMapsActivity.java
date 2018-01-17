package robor.forestfireboundaries;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import robor.forestfireboundaries.bluetooth.DeviceScanActivity;
import robor.forestfireboundaries.bluetooth.MLDPConnectionService;
import robor.forestfireboundaries.protobuf.HeaderProtos;
import robor.forestfireboundaries.protobuf.HotspotDataProtos;

/**
 * Created by Mathijs de Groot on 27/10/2017.
 */

public class GoogleMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = GoogleMapsActivity.class.getSimpleName();
    private static final int HOTSPOT_MESSAGE_HASH_CODE = HotspotDataProtos.Hotspot.getDescriptor().getName().hashCode();

    private MapFragment mapFragment;
    private GoogleMap googleMap;

    private boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
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
        getMenuInflater().inflate(R.menu.activity_google_maps_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_bt_scan:
                final Intent intent = new Intent(this, DeviceScanActivity.class);
                startActivity(intent);
                return true;
        }

        return false;
    }

    private BroadcastReceiver messageAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                if (action.equals(MLDPConnectionService.ACTION_MESSAGE_RECEIVED)) {
                    ByteString data = MLDPConnectionService.getNextAvailableMessage();

                    if (data != null) {

                    } else {

                    }
                }
            }
        }
    };

    private void processHeader() {
        try {
            ByteString data = MLDPConnectionService.getNextAvailableMessage();

            if (data != null) {
                HeaderProtos.Header header = HeaderProtos.Header.parseFrom(data);
                processMessage(header.getMessageId());
            } else {
                // TODO: Do something when data == null
                Log.d(TAG, "...");
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void processMessage(int messageId) throws InvalidProtocolBufferException {
        ByteString data = MLDPConnectionService.getNextAvailableMessage();

        if (data != null) {
            if (messageId == HOTSPOT_MESSAGE_HASH_CODE) {
                HotspotDataProtos.Hotspot hotspot = HotspotDataProtos.Hotspot.parseFrom(data);
                if (recording) {
                     // TODO: Show marker on map
                } else {
                    // TODO: Do something when we are not interested in showing data on map
                }
            }
        } else {
            // TODO: Do something when data == null
            Log.d(TAG, "...");
        }
    }
}
