package robor.forestfireboundaries.data.export.geojson.geometry;

public final class Utils {

    public static boolean isLinearRing(LineString lineString) {
        boolean xEqual = lineString.positions.get(0).x == lineString.positions.get(lineString.positions.size() - 1).x;
        boolean yEqual = lineString.positions.get(0).y == lineString.positions.get(lineString.positions.size() - 1).y;
        return (lineString.positions.size() >= 4) && xEqual && yEqual;
    }

}
