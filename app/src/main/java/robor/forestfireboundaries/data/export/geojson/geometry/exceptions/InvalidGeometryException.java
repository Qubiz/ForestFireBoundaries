package robor.forestfireboundaries.data.export.geojson.geometry.exceptions;

import robor.forestfireboundaries.data.export.geojson.geometry.GeometryJsonObject;

public class InvalidGeometryException extends Exception {

    public InvalidGeometryException(GeometryJsonObject geometryJsonObject, String message) {
        super("\n\nInvalid " + geometryJsonObject.type + ":\n" + message + "\n");
    }

}
