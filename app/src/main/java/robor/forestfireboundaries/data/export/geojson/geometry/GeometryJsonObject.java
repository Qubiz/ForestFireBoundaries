package robor.forestfireboundaries.data.export.geojson.geometry;

import com.squareup.moshi.JsonWriter;

import java.io.IOException;

import robor.forestfireboundaries.data.export.geojson.features.GeoJsonObject;

public abstract class GeometryJsonObject {

    public final GeoJsonObject.Type type;

    public GeometryJsonObject(GeoJsonObject.Type type) {
        this.type = type;
    }

    public JsonWriter writeTo(JsonWriter writer) throws IOException {
        writer.name("type").value(type.toString());
        return writer;
    }
}
