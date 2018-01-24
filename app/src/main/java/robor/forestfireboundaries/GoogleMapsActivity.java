package robor.forestfireboundaries;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mikepenz.iconics.context.IconicsLayoutInflater2;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import robor.forestfireboundaries.bluetooth.DeviceScanActivity;
import robor.forestfireboundaries.bluetooth.MLDPDataReceiverService;
import robor.forestfireboundaries.data.HotspotMarker;
import robor.forestfireboundaries.data.HotspotMarkerLayer;
import robor.forestfireboundaries.drawer.DrawerNavigation;
import robor.forestfireboundaries.protobuf.HeaderProtos;
import robor.forestfireboundaries.protobuf.HotspotDataProtos;

import android.view.View;

/**
 * Created by Mathijs de Groot on 27/10/2017.
 */

public class GoogleMapsActivity extends AppCompatActivity implements OnMapReadyCallback, HotspotMarkerLayer.LayerChangeListener, Drawer.OnDrawerItemClickListener {

    private static final String HOTSPOT_LIST = "HotspotList";

    private static final String TAG = GoogleMapsActivity.class.getSimpleName();

    private static final int HOTSPOT_MESSAGE_HASH_CODE = HotspotDataProtos.Hotspot.getDescriptor().getName().hashCode();

    private GoogleMap googleMap;

    private HotspotMarkerLayer hotspotMarkersLayer;

    private LinearGradient gradient;

    private boolean trackingEnabled = true;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new IconicsLayoutInflater2(getDelegate()));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);

        ButterKnife.bind(this);

        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Map");

        setSupportActionBar(toolbar);

        drawer = DrawerNavigation.getDrawer(this, toolbar, this);


        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (savedInstanceState != null) {
            @SuppressWarnings("unchecked")
            ArrayList<HotspotMarker> markers = (ArrayList<HotspotMarker>) savedInstanceState.getSerializable(HOTSPOT_LIST);
            hotspotMarkersLayer = new HotspotMarkerLayer(markers,this);
        } else {
            hotspotMarkersLayer = new HotspotMarkerLayer(this);
        }

        Log.d(TAG, "onCreate()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(messageAvailableReceiver, MLDPDataReceiverService.messageAvailableIntentFilter());

        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        }

        drawer.setSelection(DrawerNavigation.GOOGLE_MAPS_ACTIVITY_ID, false);

        Log.d(TAG, "onResume()");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(messageAvailableReceiver);
        Log.d(TAG, "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(HOTSPOT_LIST, hotspotMarkersLayer.getMarkers());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(true);

        this.googleMap = googleMap;

        hotspotMarkersLayer.addLayerToMap(googleMap);
        hotspotMarkersLayer.setVisible(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_google_maps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.bluetooth_status:
                Intent intent = new Intent(this, DeviceScanActivity.class);
                startActivity(intent);
                return true;

            case R.id.menu_item_tracking_toggle:
                item.setChecked(!item.isChecked());
                trackingEnabled = item.isChecked();
                return true;
        }

        return false;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        drawerToggle.syncState();
    }

    private BroadcastReceiver messageAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                if (action.equals(MLDPDataReceiverService.ACTION_MESSAGE_RECEIVED)) {
                    processHeader();
                }
            }
        }
    };

    private void processHeader() {
        try {
            ByteString data = MLDPDataReceiverService.getNextAvailableMessage();

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
        ByteString data = MLDPDataReceiverService.getNextAvailableMessage();

        if (data != null) {
            if (messageId == HOTSPOT_MESSAGE_HASH_CODE) {
                HotspotDataProtos.Hotspot hotspot = HotspotDataProtos.Hotspot.parseFrom(data);
                hotspotMarkersLayer.addMarker(new HotspotMarker(hotspot,this));
            }
        } else {
            // TODO: Do something when data == null
            Log.d(TAG, "...");
        }
    }

    @Override
    public void onMarkerAdded(HotspotMarker dotMarker) {
        Log.d(TAG, "Marker added.");
        if (trackingEnabled) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dotMarker.getPosition(), 16));
        }
    }

    @Override
    public void onMarkerRemoved(HotspotMarker dotMarker) {
        Log.d(TAG, "Marker removed.");
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        if (drawerItem.getIdentifier() == DrawerNavigation.GOOGLE_MAPS_ACTIVITY_ID) {
            drawer.closeDrawer();
            return true;
        }

        if (drawerItem.getIdentifier() == DrawerNavigation.DEVICE_SCAN_ACTIVITIY_ID) {
            Intent intent = new Intent(this, DeviceScanActivity.class);
            startActivity(intent);
            return true;
        }

        return false;
    }
}
