package robor.forestfireboundaries.data.export;

import android.util.Log;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okio.Buffer;
import robor.forestfireboundaries.data.HotspotMarker;
import robor.forestfireboundaries.data.HotspotMarkerLayer;
import robor.forestfireboundaries.data.export.geojson.features.Feature;
import robor.forestfireboundaries.data.export.geojson.features.FeatureCollection;
import robor.forestfireboundaries.data.export.geojson.geometry.Point;
import robor.forestfireboundaries.data.export.geojson.geometry.Position;

/**
 * Created by Mathijs de Groot on 26/01/2018.
 */

public class GeoJsonExport {

    private static final String TAG = GeoJsonExport.class.getSimpleName();

    public static void writeGeoJSONFrom(HotspotMarkerLayer hotspotMarkerLayer, String fileName) throws IOException {
        List<Feature> featureList = new ArrayList<>();

        for (HotspotMarker marker : hotspotMarkerLayer.getMarkers()) {
            featureList.add(createPointFeature(marker));
            Log.d(TAG, "Add!");
        }

        FeatureCollection featureCollection = new FeatureCollection(featureList);

        Buffer buffer = new Buffer();
        JsonWriter jsonWriter = JsonWriter.of(buffer);
        jsonWriter = featureCollection.writeTo(jsonWriter);
        Log.d(TAG, buffer.readUtf8());
    }

    private static Feature<Point> createPointFeature(HotspotMarker marker) {
        Position position = new Position(marker.getPosition().longitude, marker.getPosition().latitude);

        HashMap<String, String> properties = new HashMap<>();
        properties.put("date", marker.getDateString());
        properties.put("temperature", String.valueOf(marker.getHotspot().getTemperature()));

        Feature<Point> pointFeature = new Feature<>(new Point(position), properties);

        return pointFeature;
    }


}
