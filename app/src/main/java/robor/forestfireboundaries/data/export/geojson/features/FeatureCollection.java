package robor.forestfireboundaries.data.export.geojson.features;

import com.squareup.moshi.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FeatureCollection extends GeoJsonObject {

    private final List<Feature> features;

    public FeatureCollection(Feature... features) {
        this(Arrays.asList(features));
    }

    public FeatureCollection(List<Feature> features) {
        super(Type.FeatureCollection);
        this.features = features;
    }

    @Override
    public JsonWriter writeTo(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type").value(type.toString());
        writer = writeFeatures(writer);
        writer.endObject();
        return writer;
    }

    private JsonWriter writeFeatures(JsonWriter writer) throws IOException {
        writer.name("features");
        writer.beginArray();
        for (Feature feature : features) {
            writer = feature.writeTo(writer);
        }
        writer.endArray();
        return writer;
    }
}
