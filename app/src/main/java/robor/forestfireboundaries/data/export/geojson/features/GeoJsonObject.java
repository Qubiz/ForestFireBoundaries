package robor.forestfireboundaries.data.export.geojson.features;

import com.squareup.moshi.JsonWriter;

import java.io.IOException;

public abstract class GeoJsonObject {

    public enum Type {
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

    public GeoJsonObject(Type type) {
        this.type = type;
    }

    public abstract JsonWriter writeTo(JsonWriter writer) throws IOException;
}
