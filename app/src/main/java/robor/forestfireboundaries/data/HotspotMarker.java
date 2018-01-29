package robor.forestfireboundaries.data;

import android.annotation.SuppressLint;
import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import robor.forestfireboundaries.protobuf.HotspotDataProtos;

/**
 * Created by Mathijs de Groot on 22/01/2018.
 */

public class HotspotMarker extends DotMarker implements Serializable {

    private HotspotDataProtos.Hotspot hotspot;
    private String dateString;

    public HotspotMarker(HotspotDataProtos.Hotspot hotspot, Context context) {
        super(new LatLng(hotspot.getLatitude(), hotspot.getLongitude()), context);
        this.hotspot = hotspot;
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        dateString = dateFormat.format(date);

        setSnippet("Temperature: " + hotspot.getTemperature() + " \u2103 \nDate: " + dateString);

    }

    public HotspotDataProtos.Hotspot getHotspot() {
        return hotspot;
    }

    public String getDateString() { return dateString; }
}
