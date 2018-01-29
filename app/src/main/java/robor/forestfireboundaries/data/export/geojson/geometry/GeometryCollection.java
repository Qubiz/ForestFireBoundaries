package robor.forestfireboundaries.data.export.geojson.geometry;

import com.squareup.moshi.JsonWriter;

import java.io.IOException;

import robor.forestfireboundaries.data.export.geojson.features.GeoJsonObject;

public class GeometryCollection extends GeometryJsonObject {

    public GeometryCollection() {
        super(GeoJsonObject.Type.GeometryCollection);
    }

    @Override
    public JsonWriter writeTo(JsonWriter writer) throws IOException {
        return super.writeTo(writer);
    }
}
