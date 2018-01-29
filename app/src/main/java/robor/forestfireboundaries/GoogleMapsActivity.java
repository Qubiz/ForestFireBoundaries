package robor.forestfireboundaries;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsLayoutInflater2;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import butterknife.OnClick;
import robor.forestfireboundaries.bluetooth.DeviceScanActivity;
import robor.forestfireboundaries.bluetooth.MLDPConnectionService;
import robor.forestfireboundaries.bluetooth.MLDPDataReceiverService;
import robor.forestfireboundaries.data.HotspotMarker;
import robor.forestfireboundaries.data.HotspotMarkerLayer;
import robor.forestfireboundaries.data.export.GeoJsonExport;
import robor.forestfireboundaries.drawer.DrawerNavigation;
import robor.forestfireboundaries.fab.Fab;
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

    MaterialSheetFab materialSheetFab;

    @BindView(R.id.toolbar)                 Toolbar     toolbar;
    @BindView(R.id.fab)                     Fab         fab;
    @BindView(R.id.fab_sheet)               View        fabSheet;
    @BindView(R.id.fab_overlay)             View        fabOverlay;

    @OnClick(R.id.fab_sheet_item_clear)
    public void clearMap() {
        hotspotMarkersLayer.clear();
        materialSheetFab.hideSheet();
    }

    @OnClick(R.id.fab_sheet_item_locate)
    public void locateHotspot() {
        if (hotspotMarkersLayer.isLayerAddedToMap()) {
            if (!hotspotMarkersLayer.getMarkers().isEmpty()) {
                HotspotMarker hotspotMarker = hotspotMarkersLayer.getMarker(hotspotMarkersLayer.getMarkers().size() - 1);
                LatLng position = new LatLng(hotspotMarker.getHotspot().getLatitude(), hotspotMarker.getHotspot().getLongitude());

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16));
            }
        }
        materialSheetFab.hideSheet();
    }

    @OnClick(R.id.fab_sheet_item_export)
    public void exportLayer() {
        try {
            Log.d(TAG, "exportLayer()");
            GeoJsonExport.writeGeoJSONFrom(hotspotMarkersLayer, "test");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

        materialSheetFab = new MaterialSheetFab<>(fab, fabSheet, fabOverlay, Color.WHITE, getResources().getColor(R.color.colorAccent));

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (savedInstanceState != null) {
            @SuppressWarnings("unchecked")
            ArrayList<HotspotMarker> markers = (ArrayList<HotspotMarker>) savedInstanceState.getSerializable(HOTSPOT_LIST);
            hotspotMarkersLayer = new HotspotMarkerLayer(markers,this);
        } else {
            hotspotMarkersLayer = new HotspotMarkerLayer(this);
        }

        registerReceiver(messageAvailableReceiver, MLDPDataReceiverService.messageAvailableIntentFilter());
        registerReceiver(connectionStateReceiver, MLDPConnectionService.connectionStateIntentFilter());

        Log.d(TAG, "onCreate()");
    }

    @Override
    protected void onResume() {
        super.onResume();

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
        unregisterReceiver(messageAvailableReceiver);
        unregisterReceiver(connectionStateReceiver);
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        int padding = (int) convertDpToPixel(32.0f, this);

        googleMap.setPadding(0, padding, 0, padding);

        this.googleMap = googleMap;

        hotspotMarkersLayer.addLayerToMap(googleMap);
        hotspotMarkersLayer.setVisible(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_google_maps_menu, menu);

        IconicsDrawable iconicsDrawable = new IconicsDrawable(this)
                .icon(MaterialDesignIconic.Icon.gmi_bluetooth_off)
                .color(Color.WHITE)
                .sizeDp(16);

        if (BaseApplication.isMLDPConnectionServiceBound() && BaseApplication.getMLDPConnectionService().isConnected()) {
            iconicsDrawable = new IconicsDrawable(this)
                    .icon(MaterialDesignIconic.Icon.gmi_bluetooth_connected)
                    .color(Color.WHITE)
                    .sizeDp(16);

            String deviceName = BaseApplication.getMLDPConnectionService().getConnectedDevice().getName();

            menu.findItem(R.id.bluetooth_status).setTitle(" " + deviceName);
        } else {
            menu.findItem(R.id.bluetooth_status).setTitle("");
        }


        menu.findItem(R.id.bluetooth_status).setIcon(iconicsDrawable);
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

    private BroadcastReceiver connectionStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case MLDPConnectionService.ACTION_CONNECTED:
                        break;
                    case MLDPConnectionService.ACTION_DISCONNECTED:
                        break;
                    case MLDPConnectionService.ACTION_CONNECTING:
                        break;
                    case MLDPConnectionService.ACTION_DISCONNECTING:
                        break;
                }
                invalidateOptionsMenu();
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

                HotspotMarker hotspotMarker = new HotspotMarker(hotspot, this);

                hotspotMarkersLayer.addMarker(hotspotMarker);
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

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
}
