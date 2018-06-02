package se.tink.backend.system.cli.interestmap;

import com.google.common.collect.Lists;
import java.util.List;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;

public class Polygons {

    private static final double TWOPI = 2 * Math.PI;

    public static boolean isCoordinatesInside(Polygon polygon, double latitude, double longitude) {

        List<LngLatAlt> exteriorRing = polygon.getExteriorRing();

        double angle = 0;
        double lat1, lng1, lat2, lng2;
        int n = exteriorRing.size();

        for (int i = 0; i < n; i++) {
            lat1 = exteriorRing.get(i).getLatitude() - latitude;
            lng1 = exteriorRing.get(i).getLongitude() - longitude;

            lat2 = exteriorRing.get((i + 1) % n).getLatitude() - latitude;
            lng2 = exteriorRing.get((i + 1) % n).getLongitude() - longitude;
            angle += angle2D(lat1, lng1, lat2, lng2);
        }

        if (Math.abs(angle) < Math.PI) {
            return false;
        } else {
            List<Polygon> holes = Lists.newArrayList();
            for (List<LngLatAlt> interiorRing : polygon.getInteriorRings()) {
                holes.add(new Polygon(interiorRing));
            }

            if (holes.size() == 0) {
                // Found coordinate inside exterior ring and no holes
                return true;
            } else {
                for (Polygon hole : holes) {
                    if (isCoordinatesInside(hole, latitude, longitude)) {
                        // Found coordinates inside on of the holes, i.e. coordinates is not inside main polygon
                        return false;
                    }
                }

                // Coordinates wasn't found inside any of the holes, i.e. coordinates is inside main polygon
                return true;
            }
        }
    }

    private static double angle2D(double y1, double x1, double y2, double x2) {
        double dtheta, theta1, theta2;

        theta1 = Math.atan2(y1, x1);
        theta2 = Math.atan2(y2, x2);
        dtheta = theta2 - theta1;
        while (dtheta > Math.PI) {
            dtheta -= TWOPI;
        }
        while (dtheta < -Math.PI) {
            dtheta += TWOPI;
        }

        return (dtheta);
    }
}

