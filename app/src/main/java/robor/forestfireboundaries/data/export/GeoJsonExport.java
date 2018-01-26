package robor.forestfireboundaries.data.export;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import robor.forestfireboundaries.data.HotspotMarkerLayer;
import robor.forestfireboundaries.data.export.geojson.GeoJsonObject;
import robor.forestfireboundaries.data.export.geojson.GeometryObject;
import robor.forestfireboundaries.data.export.geojson.MultiPoint;
import robor.forestfireboundaries.data.export.geojson.Point;

/**
 * Created by Mathijs de Groot on 26/01/2018.
 */

public class GeoJsonExport {

    public static void writeGeoJSONFrom(HotspotMarkerLayer hotspotMarkerLayer, String fileName) {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("temperature", "35.0");
        properties.put("date","today");

        double[] point = new double[]{6.693429,52.22571};

        Moshi moshi = new Moshi.Builder().build();

        @SuppressWarnings("unchecked")
        GeoJsonObject<Point> geoJsonObject = new GeoJsonObject<Point>(GeoJsonObject.Type.Feature,
                new Point(point),
                properties);
        Type type = Types.newParameterizedType(GeoJsonObject.class, Point.class);
        JsonAdapter<GeoJsonObject<Point>> jsonPointAdapter = moshi.adapter(type);

        String json = jsonPointAdapter.toJson(geoJsonObject);

        System.out.println(json);
    }

}
