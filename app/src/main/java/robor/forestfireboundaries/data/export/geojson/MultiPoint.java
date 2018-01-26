package robor.forestfireboundaries.data.export.geojson;

import java.util.List;

/**
 * Created by Mathijs de Groot on 26/01/2018.
 */

public class MultiPoint extends GeometryObject {

    public final List<double[]> coordinates;

    public MultiPoint(List<double[]> coordinates) {
        super(GeoJsonObject.Type.MultiPoint);
        this.coordinates = coordinates;
    }
}
