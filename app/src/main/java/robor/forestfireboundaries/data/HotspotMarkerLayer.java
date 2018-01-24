package robor.forestfireboundaries.data;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;

/**
 * Created by Mathijs de Groot on 22/01/2018.
 */

public class HotspotMarkerLayer {

    private static final String TAG = HotspotMarkerLayer.class.getSimpleName();

    /**
     * List of markers on this layer
     */
    private ArrayList<HotspotMarker> markers;

    /**
     * The GoogleMap this layer is displayed on
     */
    private GoogleMap googleMap;

    /**
     * Determines whether the layer is visible on the GoogleMap.
     */
    private boolean visible = false;

    private LayerChangeListener layerChangeListener;

    public HotspotMarkerLayer(LayerChangeListener layerChangeListener) {
        markers = new ArrayList<>();
        this.layerChangeListener = layerChangeListener;
    }

    public HotspotMarkerLayer(ArrayList<HotspotMarker> markers, LayerChangeListener layerChangeListener) {
        this.markers = markers;
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
        markers.add(marker);
        layerChangeListener.onMarkerAdded(marker);
    }

    public void removeMarker(int index) {
        if (index < markers.size() && index >= 0) {
            HotspotMarker marker = markers.remove(index);
            marker.removeFromMap();
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
}