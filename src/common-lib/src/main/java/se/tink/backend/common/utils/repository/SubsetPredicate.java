package se.tink.backend.common.utils.repository;

import com.google.common.base.Preconditions;
import java.util.Random;
import java.util.function.Predicate;

public class SubsetPredicate<T> implements Predicate<T> {
    private static final Random random = new Random();

    private final double pickProbability;
    private final int numberOfRequiredEntities;
    private int currentCount = 0;

    public SubsetPredicate(double subsetSizeRatio, int totalSize) {
        Preconditions.checkArgument(subsetSizeRatio > 0.0 && subsetSizeRatio <= 1.0, "subsetSizeRatio must be");
        this.numberOfRequiredEntities = (int) (subsetSizeRatio * (double) totalSize);
        this.pickProbability = subsetSizeRatio;
    }

    @Override
    public boolean test(T t) {
        if (currentCount < numberOfRequiredEntities) {
            if (random.nextDouble() < this.pickProbability) {
                ++currentCount;
                return true;
            }
        }
        return false;
    }

    public int count() {
        return currentCount;
    }
}
