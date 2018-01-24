package robor.forestfireboundaries.data;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

import robor.forestfireboundaries.protobuf.HotspotDataProtos;

/**
 * Created by Mathijs de Groot on 22/01/2018.
 */

public class HotspotMarker extends DotMarker implements Serializable {

    private HotspotDataProtos.Hotspot hotspot;

    public HotspotMarker(HotspotDataProtos.Hotspot hotspot, Context context) {
        super(new LatLng(hotspot.getLatitude(), hotspot.getLongitude()), context);
        // TODO: Set color depending on hotspot temperature.
        this.hotspot = hotspot;
        setSnippet("Temperature: " + hotspot.getTemperature() + " \u2103");
    }

    public HotspotDataProtos.Hotspot getHotspot() {
        return hotspot;
    }
}
