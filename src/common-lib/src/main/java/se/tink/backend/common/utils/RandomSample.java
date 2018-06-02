package se.tink.backend.common.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Utility class to choose N random subsamples from a list.
 * 
 * @param <V>
 *            the item types in the list.
 */
public class RandomSample<V> {
    /**
     * Optimized for choosing the majority of the items.
     */
    private class MajoritySubsetSampler implements Sampler<V> {

        @Override
        public ArrayList<V> pick(int n) {
            final int nToRemove = items.size() - n;
            Set<Integer> toRemove = Sets.newHashSet();
            while (toRemove.size() < nToRemove) {
                toRemove.add(random.nextInt(items.size()));
            }

            ArrayList<V> result = new ArrayList<V>(n);
            for (int i = 0; i < items.size(); i++) {
                if (!toRemove.contains(i)) {
                    result.add(items.get(i));
                }
            }
            return result;
        }

    }

    /**
     * Optimized for choosing smaller subset of the items.
     */
    private class MinoritySubsetSampler implements Sampler<V> {

        @Override
        public ArrayList<V> pick(int n) {
            Set<Integer> toChoose = Sets.newHashSet();
            while (toChoose.size() < n) {
                toChoose.add(random.nextInt(items.size()));
            }

            ArrayList<V> result = new ArrayList<V>(n);
            for (Integer iChoice : toChoose) {
                result.add(items.get(iChoice));
            }
            return result;
        }

    }

    private static interface Sampler<V> {
        ArrayList<V> pick(int n);
    }

    private List<V> items;

    private Random random;

    private RandomSample(List<V> allUserIds, Random random) {
        this.items = allUserIds;
        this.random = random;
    }
    
    /**
     * Returns an unordered subset of the given items.
     * 
     * @param n
     *            the number of items to choose.
     * @return a list containing a non-duplicate subset of the original list of items.
     */
    public List<V> pick(int n) {
        Preconditions.checkArgument(n >= 0, "Can't sample negative number of items.");
        Preconditions.checkArgument(n <= items.size(),
                String.format("Not enough elements (%d) to sample from. Wanted %d.", items.size(), n));

        if (n == 0) {
            // The implementors probably handle this case, but this is faster.
            return Collections.emptyList();
        }
        
        Sampler<V> picker;
        if (n > items.size() / 2) {
            picker = new MajoritySubsetSampler();
        } else {
            picker = new MinoritySubsetSampler();
        }
        return picker.pick(n);
    }

    public static <T> RandomSample<T> from(ArrayList<T> newArrayList) {
        return from(newArrayList, new Random());
    }

    public static <T> RandomSample<T> from(List<T> allUserIds, Random random) {
        return new RandomSample<T>(allUserIds, random);
    }

}
