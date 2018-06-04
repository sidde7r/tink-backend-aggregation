package se.tink.backend.common;

import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import java.util.ArrayList;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class SortingTest {

    @Test
    public void testOrdering() {

        int NUMBER_OF_ITERATIONS = 10000;
        Random random = new Random(0);
        
        for (int i = 0 ;i<NUMBER_OF_ITERATIONS;i++) {
            testOrderingLimitingBehaviour(random);
        }

    }

    // Test that make sure that limitting a fully sorted list is the same as doing greatestOf/leastOf. Verifies that
    // https://github.com/tink-ab/tink-backend/pull/3074 is correct.
    private void testOrderingLimitingBehaviour(Random random) {
        int SORT_SIZE = 4;
        ArrayList<Integer> list = Lists.newArrayListWithCapacity(SORT_SIZE);
        for (int i =0;i<SORT_SIZE;i++) {
            list.add(random.nextInt());
        }

        int subset_size = random.nextInt(2 * SORT_SIZE + 1); // Will occasionally be both smaller and larger than
                                                             // SORT_SIZE.
        
        Ordering<Comparable<Integer>> ordering = Ordering.natural();
        Assert.assertEquals(ImmutableList.copyOf(Iterables.limit(ordering.sortedCopy(list), subset_size)),
                ImmutableList.copyOf(ordering.leastOf(list, subset_size)));
        Assert.assertEquals(ImmutableList.copyOf(Iterables.limit(ordering.reverse().sortedCopy(list), subset_size)),
                ImmutableList.copyOf(ordering.greatestOf(list, subset_size)));
    }

}
