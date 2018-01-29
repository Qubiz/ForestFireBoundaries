package robor.forestfireboundaries.data.export.geojson.geometry;

import com.squareup.moshi.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import robor.forestfireboundaries.data.export.geojson.features.GeoJsonObject;

public class MultiPoint extends GeometryJsonObject {

    private final List<Position> positions;

    public MultiPoint(List<Position> positions) {
        super(GeoJsonObject.Type.MultiPoint);
        this.positions = positions;
    }

    public MultiPoint(Position ... positions) {
        this(Arrays.asList(positions));
    }

    @Override
    public JsonWriter writeTo(JsonWriter writer) throws IOException {
        writer = super.writeTo(writer);
        writer.name("coordinates");
        writer = writeCoordinates(writer);
        return writer;
    }

    private JsonWriter writeCoordinates(JsonWriter writer) throws IOException {
        writer.beginArray();
        for (Position position : positions) {
            writer = position.writeTo(writer);
        }
        writer.endArray();
        return writer;
    }


}
