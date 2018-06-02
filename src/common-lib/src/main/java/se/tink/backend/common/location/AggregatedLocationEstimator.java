package se.tink.backend.common.location;

import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.apache.commons.math3.util.Pair;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;

public class AggregatedLocationEstimator {

    private static final LogUtils log = new LogUtils(AggregatedLocationEstimator.class);
    private List<CityEstimator> estimators;
    private List<Pair<LocationGuessType, Float>> weights;

    public AggregatedLocationEstimator() {
        estimators = Lists.newArrayList();
        weights = Lists.newArrayList();
    }

    public AggregatedLocationEstimator addLocationEstimator(CityEstimator estimator, float weight) {
        estimators.add(estimator);
        weights.add(new Pair<>(estimator.getType(), weight));
        return this;
    }

    public void removeLocationEstimator(CityEstimator estimator) {
        estimators.remove(estimator);
        weights.remove(getIndex(estimator.getType()));
    }

    public CityLocationGuess getMostProbableLocation(User user, Date date) {
        return getMostProbable(getLocationProbabilities(user, date));
    }

    public List<CityLocationGuess> getLocationProbabilities(User user, Date date) {

        if (estimators.size() == 0 || sum(weights) != 1.0f) {
            log.error(debugWeights());
            throw new IllegalStateException("No estimators added or weights doesn't sum to 1.0");
        }

        Stopwatch watch = Stopwatch.createStarted();

        List<CityLocationGuess> allGuesses = Lists.newArrayList();

        for(CityEstimator estimator : estimators) {
            if (getWeight(estimator.getType()) > 0) {

                List<CityLocationGuess> guesses = estimator.estimate(user, date);
                allGuesses.addAll (guesses);
            }
        }

        Set<String> uniqueCities = Sets.newHashSet(Iterables.transform(allGuesses, GUESS_TO_CITY));

        List<CityLocationGuess> aggregated = Lists.newArrayList();
        for(String city : uniqueCities) {

            Iterable<CityLocationGuess> iterable = Iterables.filter(allGuesses, new CityPredicate(city));
            aggregated.add(createAggregatedGuess(iterable));
        }

        log.debug("getLocationProbabilities took " + watch.stop().elapsed(TimeUnit.MILLISECONDS) + "ms");
        return aggregated;
    }

    private String debugWeights() {
        StringBuilder sb = new StringBuilder();
        sb.append("Weights: [ ");
        for(Pair<LocationGuessType, Float> pair : weights) {
            sb.append("{ ").append(pair.getFirst()).append(": ").append(pair.getSecond()).append(" }");
        }
        sb.append(" ]");
        return sb.toString();
    }

    private CityLocationGuess createAggregatedGuess(Iterable<CityLocationGuess> iterable) {

        if (Iterables.size(iterable) == 0) {
            return null;
        }

        CityLocationGuess aggregated = new CityLocationGuess(LocationGuessType.AGGREGATED);
        aggregated.setCity(Iterables.get(iterable, 0).getCity());
        float probability = 0.0f;
        for(CityLocationGuess guess : iterable) {
            float weight = getWeight(guess.getType());
            probability += guess.getProbability() * weight;
        }

        aggregated.setProbability(probability);

        return aggregated;
    }

    private float getWeight(final LocationGuessType type) {
        return weights.get(getIndex(type)).getSecond();
    }

    private int getIndex(final LocationGuessType type) {
        Pair<LocationGuessType, Float> pair = Iterables.find(weights, pair1 -> pair1.getFirst() == type);
        return weights.indexOf(pair);
    }

    private float sum(List<Pair<LocationGuessType, Float>> floats) {
        float sum = 0.0f;
        for(Pair<LocationGuessType, Float> p : floats) {
            sum += p.getSecond();
        }
        return sum;
    }

    private CityLocationGuess getMostProbable(List<CityLocationGuess> guesses) {
        if (guesses == null || guesses.size() == 0) {
            return null;
        }
        List<CityLocationGuess> sorted = LOCATION_GUESS_PROBABILITY.sortedCopy(guesses);
        return sorted.get(0);
    }

    private static final Ordering<LocationGuess> LOCATION_GUESS_PROBABILITY = new Ordering<LocationGuess>() {
        @Override
        public int compare(@Nullable LocationGuess g1, @Nullable LocationGuess g2) {
            return -1 * Float.compare(g1.getProbability(), g2.getProbability());
        }
    };

    public static Function<CityLocationGuess, String> GUESS_TO_CITY = new Function<CityLocationGuess, String>() {
        @Nullable
        @Override
        public String apply(@Nullable CityLocationGuess g) {
            return g.getCity();
        }
    };

}
