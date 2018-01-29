package robor.forestfireboundaries.data.export.geojson.features;

import com.squareup.moshi.JsonWriter;

import java.io.IOException;
import java.util.*;

import robor.forestfireboundaries.data.export.geojson.geometry.GeometryCollection;
import robor.forestfireboundaries.data.export.geojson.geometry.GeometryJsonObject;
import robor.forestfireboundaries.data.export.geojson.geometry.LineString;
import robor.forestfireboundaries.data.export.geojson.geometry.MultiLineString;
import robor.forestfireboundaries.data.export.geojson.geometry.MultiPoint;
import robor.forestfireboundaries.data.export.geojson.geometry.MultiPolygon;
import robor.forestfireboundaries.data.export.geojson.geometry.Point;
import robor.forestfireboundaries.data.export.geojson.geometry.Polygon;

public class Feature<T extends GeometryJsonObject> extends GeoJsonObject {

    public final T geometry;
    public final Map<String, String> properties;

    public Feature(T geometry, Map<String, String> properties) {
        super(Type.Feature);
        this.geometry = geometry;
        this.properties = properties;

    }
    @Override
    public JsonWriter writeTo(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type").value(type.toString());
        writer = writeGeometry(writer);
        writer = writeProperties(writer);
        writer.endObject();
        return writer;
    }

    private JsonWriter writeProperties(JsonWriter writer) throws IOException {
        writer.name("properties");
        writer.beginObject();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            writer.name(entry.getKey()).value(entry.getValue());
        }
        writer.endObject();
        return writer;
    }

    private JsonWriter writeGeometry(JsonWriter writer) throws  IOException {
        writer.name("geometry");
        writer.beginObject();
        switch (geometry.type) {
            case Point:
                Point point = (Point) geometry;
                writer = point.writeTo(writer);
                break;
            case MultiPoint:
                MultiPoint multiPoint = (MultiPoint) geometry;
                writer = multiPoint.writeTo(writer);
                break;
            case LineString:
                LineString lineString = (LineString) geometry;
                writer = lineString.writeTo(writer);
                break;
            case MultiLineString:
                MultiLineString multiLineString = (MultiLineString) geometry;
                writer = multiLineString.writeTo(writer);
                break;
            case Polygon:
                Polygon polygon = (Polygon) geometry;
                writer = polygon.writeTo(writer);
                break;
            case MultiPolygon:
                MultiPolygon multiPolygon = (MultiPolygon) geometry;
                writer = multiPolygon.writeTo(writer);
                break;
            case GeometryCollection:
                GeometryCollection geometryCollection = (GeometryCollection) geometry;
                writer = geometryCollection.writeTo(writer);
                break;
        }
        writer.endObject();
        return writer;
    }
}
