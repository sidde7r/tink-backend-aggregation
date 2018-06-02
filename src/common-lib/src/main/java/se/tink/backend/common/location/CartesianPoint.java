package se.tink.backend.common.location;

/**
 * Cartesian Point in the 3-dimensional space
 */
public class CartesianPoint {
    private double x;
    private double y;
    private double z;

    /**
     * Create a point from latitude and longitude
     *
     * @param latitude
     * @param longitude
     */
    public CartesianPoint(double latitude, double longitude) {

        // Convert degrees to radians.
        double lat = Math.toRadians(latitude);
        double lon = Math.toRadians(longitude);

        // Convert to cartesian from polar coordinates.
        x = Math.cos(lat) * Math.cos(lon);
        y = Math.cos(lat) * Math.sin(lon);
        z = Math.sin(lat);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}