package se.tink.backend.common.location;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.User;
import se.tink.backend.core.UserLocation;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class UserLocationEstimatorDateTest {

    private Splitter splitter = Splitter.on("\t");
    private DecimalFormat dFCoordinates = new DecimalFormat("#.#######");
    private DecimalFormat dFDistance = new DecimalFormat("#.#");
    private static final LogUtils log = new LogUtils(UserLocationEstimatorDateTest.class);

    private List<UserLocation> getUserTestLocations() throws IOException {
        File inFile = new File("data/test/location_data.txt");
        List<String> lines = Files.readLines(inFile, Charsets.UTF_8);
        Iterable<UserLocation> userLocations = Iterables.transform(lines, line -> {
            Iterable<String> data = splitter.split(line);
            UserLocation location = new UserLocation();
            location.setUserId(UUID.fromString(Iterables.get(data, 0)));
            location.setId(UUID.fromString(Iterables.get(data, 1)));
            location.setAccuracy(Double.valueOf(Iterables.get(data, 2)));
            try {
                location.setDate(ThreadSafeDateFormat.FORMATTER_SECONDS.parse(Iterables.get(data, 3)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            location.setLatitude(Double.valueOf(Iterables.get(data, 4)));
            location.setLongitude(Double.valueOf(Iterables.get(data, 5)));
            return location;
        });

        return Lists.newArrayList(userLocations);
    }

    @Test
    public void estimateUserLocationForDateTest() throws Exception {
        User user = new User();
        user.setId("testUserId");

        UserLocationEstimator locationEstimator = new UserLocationEstimator();
        String dateString = "2014-10-07 18:20:16";
        Date date = ThreadSafeDateFormat.FORMATTER_SECONDS.parse(dateString);

        UserLocation estimatedLocation = locationEstimator.getEstimatedLocationForDate(user, getUserTestLocations(), date);

        Assert.assertNotNull(estimatedLocation);
        log.info("Estimated location at time : " + dateString + " is: " + estimatedLocation.getLatitude() + ", "
                + estimatedLocation.getLongitude());

        // Result taken from http://www.geomidpoint.com/
        Assert.assertEquals(59.249147288759964, estimatedLocation.getLatitude(), 0);
        Assert.assertEquals(17.96647268765941, estimatedLocation.getLongitude(), 0);
    }



    @Test
    @Ignore
    public void validateLocationEstimator() {
        // Load userLocatins and index by userId.

        ImmutableListMultimap<UUID, UserLocation> userLocationsByUserId = null;
        try {
            File inFile = new File("data/test/users_locations-sample1.txt");
            List<String> lines = Files.readLines(inFile, Charsets.UTF_8);
            userLocationsByUserId = Multimaps.index(Iterables.transform(lines, line -> {
                Iterable<String> data = splitter.split(line);
                UserLocation location = new UserLocation();
                location.setUserId(UUID.fromString(Iterables.get(data, 0)));
                location.setId(UUID.fromString(Iterables.get(data, 1)));
                location.setAccuracy(Double.valueOf(Iterables.get(data, 2)));
                try {
                    location.setDate(ThreadSafeDateFormat.FORMATTER_SECONDS_WITH_TIMEZONE.parse(Iterables.get(data,
                            3)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                location.setLatitude(Double.valueOf(Iterables.get(data, 4)));
                location.setLongitude(Double.valueOf(Iterables.get(data, 5)));
                return location;
            }), UserLocation::getUserId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        UserLocationEstimator locationEstimator = new UserLocationEstimator();
        DescriptiveStatistics errorStatistics = new DescriptiveStatistics();
        DescriptiveStatistics accuracyStatistics = new DescriptiveStatistics();
        int errorOutsideOfAccuracy = 0;

        // Randomly take one location out of the map and try to estimate that location.

        for (UUID userId : userLocationsByUserId.keySet()) {
            List<UserLocation> locations = Lists.newArrayList(userLocationsByUserId.get(userId));

            if (locations.size() == 1) {
                continue;
            }

            System.out.println("NbrOfLocations for user: " + locations.size());

            Collections.shuffle(locations, new Random(123456));
            UserLocation locationToEstimate = locations.get(0);

            User user = new User();
            user.setId(UUIDUtils.toTinkUUID(userId));

            UserLocation estimatedUserLocation = locationEstimator.getEstimatedLocationForDate(user,
                    Lists.newArrayList(Iterables.skip(locations, 1)), locationToEstimate.getDate());

            if (estimatedUserLocation == null) {
                System.out.println("Could not estimate " + locationToEstimate.toString());
            } else {
                System.out.println("Target:           " + dFCoordinates.format(locationToEstimate.getLatitude()) + "\t"
                        + dFCoordinates.format(locationToEstimate.getLongitude()) + "\t"
                        + locationToEstimate.getAccuracy());
                System.out.println("Estimation:       " + dFCoordinates.format(estimatedUserLocation.getLatitude())
                        + "\t" + dFCoordinates.format(estimatedUserLocation.getLongitude()) + "\t"
                        + estimatedUserLocation.getAccuracy());
                double error = DistanceCalculator.calculateDistance(locationToEstimate.getLatitude(),
                        locationToEstimate.getLongitude(), estimatedUserLocation.getLatitude(),
                        estimatedUserLocation.getLongitude());
                double accuracyMinusErros = estimatedUserLocation.getAccuracy() - error;

                System.out.println("Error:            " + dFDistance.format(error) + " m");
                System.out.println("Accuracy - Error: " + dFDistance.format(accuracyMinusErros) + " m");

                errorOutsideOfAccuracy += (accuracyMinusErros > 0 ? 0 : 1);
                accuracyStatistics.addValue(estimatedUserLocation.getAccuracy());
                errorStatistics.addValue(error);
            }
            System.out.println("");
        }
        System.out.println("RESULTS: \n\tUsers: " + errorStatistics.getN() + "\n\tAverage error: "
                + dFDistance.format(errorStatistics.getMean()) + " m \n\tMedian error: "
                + dFDistance.format(errorStatistics.getPercentile(50)) + " m\n\tAccuracy average: "
                + dFDistance.format(accuracyStatistics.getMean()) + " m\n\tAccuracy median: "
                + dFDistance.format(accuracyStatistics.getPercentile(50)) + "\n\tError outside of accuracy: "
                + errorOutsideOfAccuracy);
    }


}
