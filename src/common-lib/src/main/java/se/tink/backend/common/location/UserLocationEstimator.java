package se.tink.backend.common.location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

import se.tink.backend.core.User;
import se.tink.backend.core.UserLocation;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;

public class UserLocationEstimator {

    private boolean debug = false;

    /**
     * Uses the weights and calculate the geographical midpoint of the user locations.
     *
     * @param locations
     * @param weights
     */
    public UserLocation getWeightedGeographicalMidpoint(List<UserLocation> locations, List<Double> weights) {

        Preconditions.checkArgument(locations != null, "No locations have been provided");
        Preconditions.checkArgument(weights != null, "No weights have been provided");
        Preconditions.checkArgument(locations.size() > 0, "No location points included");
        Preconditions.checkArgument(weights.size() == locations.size(),
                "Number of weights does not match number of locations");

        double weightsSum = 0;
        for (Double weight : weights) {
            weightsSum += weight;
        }

        List<CartesianPoint> cartesianPoints = Lists.newArrayList();

        for (UserLocation location : locations) {
            cartesianPoints.add(new CartesianPoint(location.getLatitude(), location.getLongitude()));
        }

        // Include weights to calculate average points.
        double xSum = 0;
        double ySum = 0;
        double zSum = 0;

        for (int i = 0; i < cartesianPoints.size(); i++) {
            CartesianPoint point = cartesianPoints.get(i);
            Double weight = weights.get(i);

            xSum += point.getX() * weight;
            ySum += point.getY() * weight;
            zSum += point.getZ() * weight;
        }

        double xAverage = xSum / weightsSum;
        double yAverage = ySum / weightsSum;
        double zAverage = zSum / weightsSum;

        // Convert back to polar from cartesian coordinates.

        double longitude = Math.atan2(yAverage, xAverage);
        double hypotenuse = Math.sqrt(Math.pow(xAverage, 2) + Math.pow(yAverage, 2));
        double latitude = Math.atan2(zAverage, hypotenuse);

        // Convert back to degrees from radians.

        UserLocation location = new UserLocation();
        location.setLatitude(Math.toDegrees(latitude));
        location.setLongitude(Math.toDegrees(longitude));

        return location;
    }

    /**
     * Estimates the user location from a list of locations and a specific date
     *
     * @param user
     * @param locations
     * @param date
     * @return The estimates user location
     */
    public UserLocation getEstimatedLocationForDate(User user, List<UserLocation> locations, Date date) {
        if (locations == null || locations.size() == 0) {
            return null;
        }

        List<Double> weights = new ArrayList<>(locations.size());

        weights.addAll(Collections.nCopies(locations.size(), 1D));

        return getWeightedGeographicalMidpoint(locations, weights);
    }

    /**
     * @param locations
     * @param date
     * @return
     */
    public UserLocation getInterpolatedUserLocation(List<UserLocation> locations, Date date) {
        if (locations.size() < 3) {
            return null;
        }

        Ordering<UserLocation> orderingLocationsByDate = new Ordering<UserLocation>() {
            @Override
            public int compare(UserLocation left, UserLocation right) {
                return Longs.compare(left.getDate().getTime(), right.getDate().getTime());
            }
        };

        List<UserLocation> locationsOrdered = orderingLocationsByDate.sortedCopy(locations);

        SplineInterpolator latitudeInterpolator = new SplineInterpolator();
        SplineInterpolator longitudeInterpolator = new SplineInterpolator();

        double[] latitudes = new double[locationsOrdered.size()];
        double[] longitudes = new double[locationsOrdered.size()];
        double[] time = new double[locationsOrdered.size()];

        for (int i = 0; i < locationsOrdered.size(); i++) {
            UserLocation location = locationsOrdered.get(i);

            latitudes[i] = location.getLatitude();
            longitudes[i] = location.getLongitude();
            time[i] = location.getDate().getTime();

            if (i > 0 && time[i - 1] == time[i]) {
                time[i] = location.getDate().getTime() + 1;
            }
        }

        Set<Double> times = Sets.newHashSet();
        for (double t : time) {
            times.add(t);
        }

        PolynomialSplineFunction latitudeInterpolation = latitudeInterpolator.interpolate(time, latitudes);
        PolynomialSplineFunction longitudeInterpolation = longitudeInterpolator.interpolate(time, longitudes);

        double firstKnot = time[0];
        double lastKnot = time[time.length - 1];
        double stepSize = (lastKnot - firstKnot) / 30;

        // It's not possible to interpolate a value that isn't in the range
        // Return last location if the value is after the last position
        if (date.getTime() > lastKnot) {
            return locationsOrdered.get(locationsOrdered.size() - 1);
        }

        // Return fist location if the value is before the first position
        if (date.getTime() < firstKnot) {
            return locationsOrdered.get(0);
        }

        double latitudeValue = latitudeInterpolation.value(date.getTime());
        double longitudeValue = longitudeInterpolation.value(date.getTime());

        if (debug) {
            double[] timeCoordinateValues = new double[time.length];
            double[] timeInterpolationValues = new double[31];
            double[] latitudeInterpolationValues = new double[31];
            double[] longitudeInterpolationValues = new double[31];

            int index = 0;
            int index2 = 0;
            for (double i = firstKnot; i <= lastKnot; i += stepSize) {
                if (times.contains(i)) {
                    timeCoordinateValues[index2] = index;
                    index2++;
                }
                timeInterpolationValues[index] = index;
                latitudeInterpolationValues[index] = latitudeInterpolation.value(i);
                longitudeInterpolationValues[index] = longitudeInterpolation.value(i);
                index++;
            }

            DrawGraph.draw("latitude", timeInterpolationValues, latitudeInterpolationValues, timeCoordinateValues,
                    latitudes);
            DrawGraph.draw("longitude", timeInterpolationValues, longitudeInterpolationValues, timeCoordinateValues,
                    longitudes);
        }

        UserLocation locationClosestInTime = locations.get(0);
        int minutesDiff = getMinutesBetweenDates(date, locationClosestInTime.getDate());

        UserLocation estimatedLocation = new UserLocation();
        estimatedLocation.setLatitude(latitudeValue);
        estimatedLocation.setLongitude(longitudeValue);
        estimatedLocation.setAccuracy(minutesDiff * locationClosestInTime.getAccuracy());

        return estimatedLocation;
    }

    /**
     * Return the minutes between two dates
     *
     * @param d1
     * @param d2
     * @return
     */
    private int getMinutesBetweenDates(Date d1, Date d2) {
        DateTime dt1 = new DateTime(d1);
        DateTime dt2 = new DateTime(d2);

        return Math.abs(Minutes.minutesBetween(dt1, dt2).getMinutes());
    }

    /**
     * Finds location events for the closest locations based on date.
     *
     * @param locations
     * @param date
     * @param limit
     * @return List with locations
     */
    public List<UserLocation> findCloseInTimeLocations(List<UserLocation> locations, final Date date, int limit) {
        Ordering<UserLocation> locationsClosestToDateOrdering = new Ordering<UserLocation>() {
            @Override
            public int compare(UserLocation left, UserLocation right) {
                long leftDiff = Math.abs(left.getDate().getTime() - date.getTime());
                long rightDiff = Math.abs(right.getDate().getTime() - date.getTime());
                return Longs.compare(leftDiff, rightDiff);
            }
        };

        return locationsClosestToDateOrdering.leastOf(locations, limit);
    }
}
