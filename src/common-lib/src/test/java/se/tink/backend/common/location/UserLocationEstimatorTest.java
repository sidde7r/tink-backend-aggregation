package se.tink.backend.common.location;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.core.UserLocation;
import se.tink.backend.utils.LogUtils;

public class UserLocationEstimatorTest {

    private static final LogUtils log = new LogUtils(UserLocationEstimatorDateTest.class);

    private UserLocation createUserLocation(Date date, double lat, double lon) {
        UserLocation location = new UserLocation();
        location.setDate(date);
        location.setLatitude(lat);
        location.setLongitude(lon);
        return location;
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNoWLocationsAreUsed() {
        UserLocationEstimator locationEstimator = new UserLocationEstimator();
        locationEstimator.getWeightedGeographicalMidpoint(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNoWeightsAreUsed() {
        UserLocationEstimator locationEstimator = new UserLocationEstimator();

        List<UserLocation> locations = Lists.newArrayList();
        locations.add(createUserLocation(null, 57.890714064402125, 16.402799927245688));

        locationEstimator.getWeightedGeographicalMidpoint(locations, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenWeightsDoesNotMatchLocations() {
        UserLocationEstimator locationEstimator = new UserLocationEstimator();

        List<UserLocation> locations = Lists.newArrayList();
        locations.add(createUserLocation(null, 57.890714064402125, 16.402799927245688));
        locationEstimator.getWeightedGeographicalMidpoint(locations, new ArrayList<Double>());
    }

    @Test
    public void shouldCalculateWeightedGeographicalMidpoint() {
        UserLocationEstimator locationEstimator = new UserLocationEstimator();

        List<UserLocation> locations = Lists.newArrayList();
        locations.add(createUserLocation(null, 57.890714064402125, 16.402799927245688));
        locations.add(createUserLocation(null, 57.89221797897739, 16.40127775117617));
        locations.add(createUserLocation(null, 57.8882580674845, 16.367066685071226));

        List<Double> weights = Lists.newArrayList(new Double(0.0005), new Double(0.0003), new Double(0.0002));

        UserLocation midPoint = locationEstimator.getWeightedGeographicalMidpoint(locations, weights);

        System.out.println("Midpoint is: " + midPoint.getLatitude() + ", " + midPoint.getLongitude());

        // Result taken from http://www.geomidpoint.com/
        Assert.assertEquals(57.890674818386024, midPoint.getLatitude(), 0);
        Assert.assertEquals(16.395196165595095, midPoint.getLongitude(), 0);
    }

    @Test
    public void shouldFindCloseInTimeLocations() {
        List<UserLocation> locations = Lists.newArrayList();
        Date now = new Date();
        locations.add(createUserLocation(DateUtils.addHours(now, -4), 0, 0));
        locations.add(createUserLocation(DateUtils.addHours(now, -2), 0, 0));
        locations.add(createUserLocation(DateUtils.addHours(now, 1), 0, 0));
        locations.add(createUserLocation(DateUtils.addHours(now, 19), 0, 0));

        UserLocationEstimator locationEstimator = new UserLocationEstimator();
        List<UserLocation> closeInTimeLocations = locationEstimator.findCloseInTimeLocations(locations, now, 10);
        Assert.assertEquals(DateUtils.addHours(now, 1).getTime(), closeInTimeLocations.get(0).getDate().getTime());
        Assert.assertEquals(DateUtils.addHours(now, -2).getTime(), closeInTimeLocations.get(1).getDate().getTime());
        Assert.assertEquals(DateUtils.addHours(now, -4).getTime(), closeInTimeLocations.get(2).getDate().getTime());
    }

    @Test
    public void shouldFindCloseIntTimeLocationsWithLimit() {
        List<UserLocation> locations = Lists.newArrayList();
        Date now = new Date();
        locations.add(createUserLocation(DateUtils.addHours(now, -4), 0, 0));
        locations.add(createUserLocation(DateUtils.addHours(now, -2), 0, 0));
        locations.add(createUserLocation(DateUtils.addHours(now, 1), 0, 0));
        locations.add(createUserLocation(DateUtils.addHours(now, 19), 0, 0));

        UserLocationEstimator locationEstimator = new UserLocationEstimator();
        List<UserLocation> closeInTimeLocations = locationEstimator.findCloseInTimeLocations(locations, now, 2);
        Assert.assertEquals(DateUtils.addHours(now, 1).getTime(), closeInTimeLocations.get(0).getDate().getTime());
        Assert.assertEquals(DateUtils.addHours(now, -2).getTime(), closeInTimeLocations.get(1).getDate().getTime());
    }

    @Test
    public void shouldCalculateInterpolationForLocations() {
        List<UserLocation> locations = Lists.newArrayList();
        Date now = new Date();
        locations.add(createUserLocation(DateUtils.addHours(now, -4), 56.890714064402125, 15.402799927245688));
        locations.add(createUserLocation(DateUtils.addHours(now, -2), 57.89221797897739, 16.40127775117617));
        locations.add(createUserLocation(DateUtils.addHours(now, 1), 59.8882580674845, 16.367066685071226));

        UserLocationEstimator locationEstimator = new UserLocationEstimator();
        UserLocation location = locationEstimator.getInterpolatedUserLocation(locations, now);

        log.info("Interpolated point is: " + location.getLatitude() + ", " + location.getLongitude());

        Assert.assertEquals(59.17901944094709, location.getLatitude(), 0);
        Assert.assertEquals(16.51464173395071, location.getLongitude(), 0);
    }

    @Test
    public void shouldReturnLastKnownPositionWhenDateIsInThePast() {
        List<UserLocation> locations = Lists.newArrayList();
        Date now = new Date();
        locations.add(createUserLocation(DateUtils.addHours(now, -4), 56.890714064402125, 15.402799927245688));
        locations.add(createUserLocation(DateUtils.addHours(now, -2), 57.89221797897739, 16.40127775117617));
        locations.add(createUserLocation(DateUtils.addHours(now, -1), 59.8882580674845, 16.367066685071226));

        UserLocationEstimator locationEstimator = new UserLocationEstimator();
        UserLocation location = locationEstimator.getInterpolatedUserLocation(locations, now);

        log.info("Interpolated point is: " + location.getLatitude() + ", " + location.getLongitude());

        Assert.assertEquals(59.8882580674845, location.getLatitude(), 0);
        Assert.assertEquals(16.367066685071226, location.getLongitude(), 0);
    }

    @Test
    public void shouldReturnFirstKnownPositionWhenDateIsInTheFuture() {
        List<UserLocation> locations = Lists.newArrayList();
        Date now = new Date();
        locations.add(createUserLocation(DateUtils.addHours(now, 1), 56.890714064402125, 15.402799927245688));
        locations.add(createUserLocation(DateUtils.addHours(now, 2), 57.89221797897739, 16.40127775117617));
        locations.add(createUserLocation(DateUtils.addHours(now, 3), 59.8882580674845, 16.367066685071226));

        UserLocationEstimator locationEstimator = new UserLocationEstimator();
        UserLocation location = locationEstimator.getInterpolatedUserLocation(locations, now);

        log.info("Interpolated point is: " + location.getLatitude() + ", " + location.getLongitude());

        Assert.assertEquals(56.890714064402125, location.getLatitude(), 0);
        Assert.assertEquals(15.402799927245688, location.getLongitude(), 0);
    }

}
