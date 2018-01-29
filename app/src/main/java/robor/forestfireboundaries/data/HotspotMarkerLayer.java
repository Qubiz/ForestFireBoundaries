package robor.forestfireboundaries.data;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;

import robor.forestfireboundaries.LinearGradient;

/**
 * Created by Mathijs de Groot on 22/01/2018.
 */

public class HotspotMarkerLayer {

    private static final String TAG = HotspotMarkerLayer.class.getSimpleName();

    private ArrayList<HotspotMarker> markers;
    private GoogleMap googleMap;
    private boolean visible = false;

    private LayerChangeListener layerChangeListener;

    private static final int[] GRADIENT_COLORS = new int[] {Color.rgb(255,255,102), Color.YELLOW, Color.RED};
    private static final LinearGradient linearGradient = new LinearGradient(GRADIENT_COLORS);

    private double minValue = 0;
    private double maxValue = 0;

    public HotspotMarkerLayer(LayerChangeListener layerChangeListener) {
        markers = new ArrayList<>();
        this.layerChangeListener = layerChangeListener;
    }

    public HotspotMarkerLayer(ArrayList<HotspotMarker> markers, LayerChangeListener layerChangeListener) {
        this.markers = new ArrayList<>();
        addMarkers(markers);
        this.layerChangeListener = layerChangeListener;
    }

    public void addMarkers(ArrayList<HotspotMarker> markers) {
        for (HotspotMarker marker : markers) {
            addMarker(marker);
        }
    }

    public void addMarker(HotspotMarker marker) {
        marker.setVisible(isVisible());
        if (isLayerAddedToMap()) {
            marker.addToMap(googleMap);
        }

        double value = marker.getHotspot().getTemperature();

        if (minValue == 0 && maxValue == 0) {
            minValue = value;
            maxValue = value;
        }

        minValue = (minValue > value) ? value : minValue;
        maxValue = (maxValue < value) ? value : maxValue;

        markers.add(marker);
        layerChangeListener.onMarkerAdded(marker);

        updateColors();
    }

    public void removeMarker(int index) {
        if (index < markers.size() && index >= 0) {
            HotspotMarker marker = markers.remove(index);
            marker.removeFromMap();
            layerChangeListener.onMarkerRemoved(marker);
        }
    }

    public void removeMarker(HotspotMarker marker) {
        if (!markers.isEmpty()) {
            marker.removeFromMap();
            markers.remove(marker);
            layerChangeListener.onMarkerRemoved(marker);
        }
    }

    public HotspotMarker getMarker(int index) {
        if (index < markers.size() && index >= 0) {
            return markers.get(index);
        } else {
            return null;
        }
    }

    public void addLayerToMap(GoogleMap googleMap) {
        if (isLayerAddedToMap()) {
            removeLayerFromMap();
        }

        for (HotspotMarker marker : markers) {
            marker.addToMap(googleMap);
            layerChangeListener.onMarkerAdded(marker);
        }

        this.googleMap = googleMap;
    }

    public void removeLayerFromMap() {
        if (isLayerAddedToMap()) {
            for (HotspotMarker marker : markers) {
                marker.removeFromMap();
                layerChangeListener.onMarkerRemoved(marker);
            }
        }

        googleMap = null;
    }

    public void clear() {
        if (isLayerAddedToMap()) {
            googleMap.clear();
        }

        markers.clear();
    }

    public void setVisible(boolean visible) {
        for (HotspotMarker marker : markers) {
            marker.setVisible(visible);
        }

        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isLayerAddedToMap() {
        return googleMap != null;
    }

    public interface LayerChangeListener {
        void onMarkerAdded(HotspotMarker dotMarker);
        void onMarkerRemoved(HotspotMarker dotMarker);
    }

    public ArrayList<HotspotMarker> getMarkers() {
        return markers;
    }

    private void updateColors() {
        double r;
        for (HotspotMarker marker : markers) {
            r = normalize(marker.getHotspot().getTemperature(), minValue, maxValue);
            Log.d(TAG, "normalized value: " + r);
            marker.setColor(linearGradient.getColor(r));
        }
    }

    private double normalize(double value, double min, double max) {
        return (value - min) / (max - min);
    }
}