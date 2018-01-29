package robor.forestfireboundaries.data.export.geojson.geometry;

import com.squareup.moshi.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import robor.forestfireboundaries.data.export.geojson.features.GeoJsonObject;

public class MultiLineString extends GeometryJsonObject {

    private List<LineString> lineStrings;

    public MultiLineString(List<LineString> lineStrings) {
        super(GeoJsonObject.Type.MultiLineString);
        this.lineStrings = lineStrings;
    }

    public MultiLineString(LineString ... lineStrings) {
        this(Arrays.asList(lineStrings));
    }

    @Override
    public JsonWriter writeTo(JsonWriter writer) throws IOException {
        writer = super.writeTo(writer);
        writer.name("coordinates");
        writer = writeLineStrings(writer);
        return writer;
    }

    private JsonWriter writeLineStrings(JsonWriter writer) throws IOException {
        writer.beginArray();
        for (LineString lineString : lineStrings) {
            writer = lineString.writeCoordinates(writer);
        }
        writer.endArray();
        return writer;
    }
}
