package se.tink.backend.common.location;


import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.common.location.facebook.FacebookBasedCityEstimator;
import se.tink.backend.common.location.transaction.TransactionBasedCityEstimator;
import se.tink.backend.core.User;


import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AggregatedLocationEstimatorTest {


    TransactionBasedCityEstimator transactional;
    FacebookBasedCityEstimator facebook;

    @Before
    public void setUp() {
        transactional = mock(TransactionBasedCityEstimator.class);
        facebook = mock(FacebookBasedCityEstimator.class);

        when(transactional.getType()).thenReturn(LocationGuessType.TRANSACTIONAL);
        when(facebook.getType()).thenReturn(LocationGuessType.FACEBOOK_HOME);
    }

    @Test(expected = IllegalStateException.class)
    public void testNoEstimators() {
        AggregatedLocationEstimator estimator = new AggregatedLocationEstimator();
        estimator.getMostProbableLocation(null, null);
    }

    @Test(expected = IllegalStateException.class)
    public void testWeigths0() {
        AggregatedLocationEstimator estimator = new AggregatedLocationEstimator();
        estimator.addLocationEstimator(transactional, 0);
        estimator.getMostProbableLocation(null, null);
    }

    @Test(expected = IllegalStateException.class)
    public void testWeigths05() {
        AggregatedLocationEstimator estimator = new AggregatedLocationEstimator();
        estimator.addLocationEstimator(transactional, 0.4f);
        estimator.addLocationEstimator(facebook, 0.1f);
        estimator.getMostProbableLocation(null, null);
    }

    @Test(expected = IllegalStateException.class)
    public void testWeigthsAbove1() {
        AggregatedLocationEstimator estimator = new AggregatedLocationEstimator();
        estimator.addLocationEstimator(transactional, 1.4f);
        estimator.addLocationEstimator(facebook, 0.1f);
        estimator.getMostProbableLocation(null, null);
    }

    @Test
    public void test100pStockholmWithOnlyTransactional () {
        Date date = new Date(21345677654L);
        AggregatedLocationEstimator estimator = new AggregatedLocationEstimator();
        estimator.addLocationEstimator(transactional, 1);
        when(transactional.estimate(any(User.class), eq(date))).thenReturn(Lists.newArrayList(
                create(LocationGuessType.TRANSACTIONAL, "Stockholm", 1)
        ));

        List<CityLocationGuess> guesses = estimator.getLocationProbabilities(new User(), date);
        assertEquals(1, guesses.size());
        LocationTestUtils.verifyCityLocationGuess(guesses.get(0), "Stockholm", 1, LocationGuessType.AGGREGATED);
    }

    @Test
    public void test100pStockholmWith50Transactional50Facebook () {
        Date date = new Date(21345677654L);
        AggregatedLocationEstimator estimator = new AggregatedLocationEstimator();
        estimator.addLocationEstimator(transactional, 0.5f);
        estimator.addLocationEstimator(facebook, 0.5f);

        when(transactional.estimate(any(User.class), eq(date))).thenReturn(Lists.newArrayList(
                create(LocationGuessType.TRANSACTIONAL, "Stockholm", 1)
        ));
        when(facebook.estimate(any(User.class), eq(date))).thenReturn(Lists.newArrayList(
                create(LocationGuessType.FACEBOOK_HOME, "Stockholm", 1)
        ));

        List<CityLocationGuess> guesses = estimator.getLocationProbabilities(new User(), date);
        assertEquals(1, guesses.size());
        LocationTestUtils.verifyCityLocationGuess(guesses.get(0), "Stockholm", 1, LocationGuessType.AGGREGATED);
    }

    @Test
    public void test75pStockholmWith50Transactional50Facebook () {
        Date date = new Date(21345677654L);
        AggregatedLocationEstimator estimator = new AggregatedLocationEstimator();
        estimator.addLocationEstimator(transactional, 0.5f);
        estimator.addLocationEstimator(facebook, 0.5f);

        when(transactional.estimate(any(User.class), eq(date))).thenReturn(Lists.newArrayList(
                create(LocationGuessType.TRANSACTIONAL, "Stockholm", 0.5f)
        ));
        when(facebook.estimate(any(User.class), eq(date))).thenReturn(Lists.newArrayList(
                create(LocationGuessType.FACEBOOK_HOME, "Stockholm", 1)
        ));

        List<CityLocationGuess> guesses = estimator.getLocationProbabilities(new User(), date);
        assertEquals(1, guesses.size());
        LocationTestUtils.verifyCityLocationGuess(guesses.get(0), "Stockholm", 3/4f, LocationGuessType.AGGREGATED);
    }

    @Test
    public void test75pStockholm25pMalmoWith50Transactional50Facebook () {
        Date date = new Date(21345677654L);
        AggregatedLocationEstimator estimator = new AggregatedLocationEstimator();
        estimator.addLocationEstimator(transactional, 0.5f);
        estimator.addLocationEstimator(facebook, 0.5f);

        when(transactional.estimate(any(User.class), eq(date))).thenReturn(Lists.newArrayList(
                create(LocationGuessType.TRANSACTIONAL, "Stockholm", 0.5f),
                create(LocationGuessType.TRANSACTIONAL, "Malmö", 0.5f)
        ));
        when(facebook.estimate(any(User.class), eq(date))).thenReturn(Lists.newArrayList(
                create(LocationGuessType.FACEBOOK_HOME, "Stockholm", 1)
        ));

        List<CityLocationGuess> guesses = LocationTestUtils.ORDERING_CITY_GUESS.sortedCopy(
                estimator.getLocationProbabilities(new User(), date));
        assertEquals(2, guesses.size());
        LocationTestUtils.verifyCityLocationGuess(guesses.get(0), "Malmö", 1/4f, LocationGuessType.AGGREGATED);
        LocationTestUtils.verifyCityLocationGuess(guesses.get(1), "Stockholm", 3/4f, LocationGuessType.AGGREGATED);
    }

    @Test
     public void testMostProbableIsMalmo () {
        Date date = new Date(21345677654L);
        AggregatedLocationEstimator estimator = new AggregatedLocationEstimator();
        estimator.addLocationEstimator(transactional, 0.8f);
        estimator.addLocationEstimator(facebook, 0.2f);

        when(transactional.estimate(any(User.class), eq(date))).thenReturn(Lists.newArrayList(
                create(LocationGuessType.TRANSACTIONAL, "Malmö", 0.4f),
                create(LocationGuessType.TRANSACTIONAL, "Uppsala", 0.3f),
                create(LocationGuessType.TRANSACTIONAL, "Göteborg", 0.3f)
        ));
        when(facebook.estimate(any(User.class), eq(date))).thenReturn(Lists.newArrayList(
                create(LocationGuessType.FACEBOOK_HOME, "Stockholm", 1)
        ));

        CityLocationGuess guess = estimator.getMostProbableLocation(new User(), date);
        LocationTestUtils.verifyCityLocationGuess(guess, "Malmö", 0.32f, LocationGuessType.AGGREGATED);
    }

    @Test
    public void testMostProbableIsStockholm () {
        Date date = new Date(21345677654L);
        AggregatedLocationEstimator estimator = new AggregatedLocationEstimator();
        estimator.addLocationEstimator(transactional, 0.7f);
        estimator.addLocationEstimator(facebook, 0.3f);

        when(transactional.estimate(any(User.class), eq(date))).thenReturn(Lists.newArrayList(
                create(LocationGuessType.TRANSACTIONAL, "Malmö", 0.4f),
                create(LocationGuessType.TRANSACTIONAL, "Uppsala", 0.3f),
                create(LocationGuessType.TRANSACTIONAL, "Göteborg", 0.3f)
        ));
        when(facebook.estimate(any(User.class), eq(date))).thenReturn(Lists.newArrayList(
                create(LocationGuessType.FACEBOOK_HOME, "Stockholm", 1)
        ));

        CityLocationGuess guess = estimator.getMostProbableLocation(new User(), date);
        LocationTestUtils.verifyCityLocationGuess(guess, "Stockholm", 0.30f, LocationGuessType.AGGREGATED);
    }

    private static CityLocationGuess create(LocationGuessType type, String city, float probability) {
        CityLocationGuess g = new CityLocationGuess(type);

        g.setCity(city);
        g.setProbability(probability);
        return g;
    }
}
