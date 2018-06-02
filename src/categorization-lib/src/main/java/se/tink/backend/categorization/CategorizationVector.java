package se.tink.backend.categorization;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The categorization vector is used to describe the probability of different categories and how important/reliable this
 * distribution is (by setting a weight to it).
 */
public class CategorizationVector {
    
    // Default value for the probability span when getting the most probable categories. (NB! Highly arbitrary!) 
    private final static double DEFAULT_EPSILON = 0.002d;
    
    /**
     * Get the sum of the values in the passed distribution vector.
     * 
     * @param distribution
     * @return
     */
    private static double getDistributionVectorSum(Map<String, Double> distribution) {
        double sum = 0;
        for (Double distributionWeight : distribution.values()) {
            sum += distributionWeight;
        }
        return sum;
    }

    /**
     * Merge two CategorizationVector objects.
     * 
     * @param vector1
     * @param vector2
     * @return
     */
    public static CategorizationVector merge(CategorizationVector vector1, CategorizationVector vector2) {
        return merge(Arrays.asList(vector1, vector2));
    }

    /**
     * Merge a list of CategorizationVector objects. The weights are added and the distribution values are normalized
     * and weighed before summarized.
     * 
     * @param vectors
     * @return
     */
    public static CategorizationVector merge(Iterable<CategorizationVector> vectors) {
        double weight = 0;
        Map<String, Double> distribution = Maps.newHashMap();

        for (CategorizationVector vector : vectors) {
            if (vector == null) {
                continue;
            }

            Map<String, Double> tmpDistribution = vector.getDistribution();

            // Ignore empty distributions (interpreted as fully indifferent, casting no vote)
            if (tmpDistribution.size() == 0) {
                continue;
            }

            weight += vector.getWeight();

            normalize(tmpDistribution);

            for (Entry<String, Double> x : tmpDistribution.entrySet()) {
                double value = 0;
                String key = x.getKey();

                if (distribution.containsKey(key)) {
                    value = distribution.get(key);
                }

                value += x.getValue() * vector.getWeight();

                distribution.put(key, value);
            }
        }

        normalize(distribution);

        return new CategorizationVector(weight, distribution);
    }

    /**
     * Normalize a distribution map (in-place)
     * 
     * @param distribution
     */
    public static void normalize(Map<String, Double> distribution) {
        double totalWeight = getDistributionVectorSum(distribution);
        for (String key : distribution.keySet()) {
            distribution.put(key, distribution.get(key) / totalWeight);
        }
    }

    private Map<String, Double> distribution;

    private double weight = 1;

    /**
     * Default constructor.
     */
    public CategorizationVector() {
        this.distribution = Maps.newHashMap();
    }

    /**
     * Constructor that allows you to set the vector weight directly (without calling setWeight separately).
     * 
     * @param weight
     */
    public CategorizationVector(double weight) {
        this();
        setWeight(weight);
    }

    /**
     * Constructor to use when you want to set a full distribution immediately (and do it in one call).
     * 
     * @param weight
     * @param distribution
     */
    public CategorizationVector(double weight, Map<String, Double> distribution) {
        this();
        setWeight(weight);
        setDistribution(distribution);
    }

    /**
     * Constructor to use when you want to set only one category value (and do it in one call).
     * 
     * @param weight
     * @param categoryCode
     * @param value
     */
    public CategorizationVector(double weight, String categoryCode, double value) {
        this();
        setWeight(weight);
        setDistribution(categoryCode, value);
    }

    /**
     * @return
     */
    public String distributionToString() {
        return distribution.entrySet().stream()
                .map(entry -> String.format("\"%s\":%.4f", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(","));
    }

    /**
     * @return
     */
    public Map<String, Double> getDistribution() {
        return distribution;
    }

    /**
     * Get the sum of the values in the distribution vector.
     * 
     * @return
     */
    private double getDistributionVectorSum() {
        return getDistributionVectorSum(distribution);
    }

    /**
     * Get the most probable categories (i.e. the categories within a default distance from the largest value).
     * 
     * @return
     */
    public Map<String, Double> getMostProbable() {
        return getMostProbable(DEFAULT_EPSILON);
    }
    
    /**
     * Get the most probable categories (i.e. the categories within the distance of epsilon from the largest value).
     * 
     * @return
     */
    public Map<String, Double> getMostProbable(final Double epsilon) {
        Map<String, Double> probabilities = getProbabilities();
        Map<String, Double> mostProbable = Maps.newHashMap();

        if (probabilities.size() > 0) {
            final Double maxValue = Collections.max(probabilities.values()) - epsilon;

            mostProbable = Maps.filterValues(probabilities, value -> value >= maxValue);
        }

        return mostProbable;
    }

    /**
     * Generate a normalized copy (summing to 1) of the distribution map.
     * 
     * @return
     */
    public Map<String, Double> getProbabilities() {
        Map<String, Double> probability = Maps.newHashMap();
        double totalWeight = getDistributionVectorSum();

        if (totalWeight > 0) {
            for (String key : distribution.keySet()) {
                probability.put(key, distribution.get(key) / totalWeight);
            }
        }

        return probability;
    }

    /**
     * Get the weight (importance) of the vector.
     * 
     * @return
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Set the internal category distribution. Overwrites all distribution values already set.
     * 
     * @param distribution
     */
    public void setDistribution(Map<String, Double> distribution) {
        this.distribution = distribution;
    }

    /**
     * Set the distribution value for a category (code). Overwrites previous value if already set.
     * 
     * @param categoryCode
     * @param value
     */
    public void setDistribution(String categoryCode, double value) {
        this.distribution.put(categoryCode, value);
    }

    /**
     * Set the weight (importance) of the vector.
     * 
     * @param weight
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CategorizationVector that = (CategorizationVector) o;
        return Double.compare(that.weight, weight) == 0
                && Objects.equals(distribution, that.distribution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(distribution, weight);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("weight", weight)
                .add("distribution", distribution)
                .toString();
    }
}
