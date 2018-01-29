package robor.forestfireboundaries.data.export.geojson.geometry;

import com.squareup.moshi.JsonWriter;

import java.io.IOException;

public class Position {
    public final double x;
    public final double y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    JsonWriter writeTo(JsonWriter writer) throws IOException {
        writer.beginArray();
        writer.value(x);
        writer.value(y);
        writer.endArray();
        return writer;
    }
}
