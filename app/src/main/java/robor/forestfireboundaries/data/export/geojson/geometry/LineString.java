package robor.forestfireboundaries.data.export.geojson.geometry;

import com.squareup.moshi.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import robor.forestfireboundaries.data.export.geojson.features.GeoJsonObject;
import robor.forestfireboundaries.data.export.geojson.geometry.exceptions.InvalidGeometryException;

public class LineString extends GeometryJsonObject {

    public final List<Position> positions;

    public LineString(List<Position> positions) throws InvalidGeometryException {
        super(GeoJsonObject.Type.LineString);
        if (positions.size() < 2) {
            throw new InvalidGeometryException(this, "A LineString should have at least two Position elements (Currently " + positions.size() + ").");
        }
        this.positions = positions;

    }

    public LineString(Position ... positions) throws InvalidGeometryException {
        this(Arrays.asList(positions));
    }

    public int getSize() {
        return positions.size();
    }

    @Override
    public JsonWriter writeTo(JsonWriter writer) throws IOException {
        writer = super.writeTo(writer);
        writer.name("coordinates");
        writer = writeCoordinates(writer);
        return writer;
    }

    JsonWriter writeCoordinates(JsonWriter writer) throws IOException {
        writer.beginArray();
        for(Position position : positions) {
            writer = position.writeTo(writer);
        }
        writer.endArray();
        return writer;
    }
}
