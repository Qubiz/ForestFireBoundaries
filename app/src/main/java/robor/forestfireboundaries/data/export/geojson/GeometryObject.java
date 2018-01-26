package robor.forestfireboundaries.data.export.geojson;

import java.util.List;

/**
 * Created by Mathijs de Groot on 26/01/2018.
 */

public abstract class GeometryObject {
    public final GeoJsonObject.Type type;

    public GeometryObject(GeoJsonObject.Type type) {
        this.type = type;
    }
}
