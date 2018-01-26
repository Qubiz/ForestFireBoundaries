package robor.forestfireboundaries.data.export.geojson;

import android.opengl.GLException;

import com.squareup.moshi.Types;

import java.util.List;
import java.util.Map;

/**
 * Created by Mathijs de Groot on 26/01/2018.
 */

public class GeoJsonObject<T extends GeometryObject> {

    public static enum Type {
        Feature,
        FeatureCollection,
        Point,
        MultiPoint,
        LineString,
        MultiLineString,
        Polygon,
        MultiPolygon,
        GeometryCollection
    }

    public final Type type;
    public final T geometry;
    public final Map<String, String> properties;

    public GeoJsonObject(Type type, T geometry, Map<String, String> properties) {
        this.type = type;
        this.geometry = geometry;
        this.properties = properties;
    }
}