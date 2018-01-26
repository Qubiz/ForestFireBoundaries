package robor.forestfireboundaries.data.export.geojson;

/**
 * Created by Mathijs de Groot on 26/01/2018.
 */

public class Point extends GeometryObject {

    public final double[] coordinates;

    public Point(double[] coordinates) {
        super(GeoJsonObject.Type.Point);
        this.coordinates = coordinates;
    }
}
