package se.tink.backend.common.concurrency;

import com.google.common.util.concurrent.Runnables;
import java.util.PriorityQueue;
import org.junit.Assert;
import org.junit.Test;

public class PrioritizedRunnableComparatorTest {

    private static final Runnable RUNNABLE_STUB = Runnables.doNothing();

    private static final PrioritizedRunnableComparator COMPARATOR = new PrioritizedRunnableComparator();

    @Test
    public void testDifferentPriority() {
        PrioritizedRunnable highPrio = new PrioritizedRunnable(PrioritizedRunnable.HIGH_PRIORITY, RUNNABLE_STUB);
        PrioritizedRunnable lowPrio = new PrioritizedRunnable(PrioritizedRunnable.LOW_PRIORITY, RUNNABLE_STUB);
        Assert.assertTrue(COMPARATOR.compare(lowPrio, highPrio) > 0);
    }

    @Test
    public void testSamePriority() {
        final int samePriority = PrioritizedRunnable.HIGH_PRIORITY;

        PrioritizedRunnable highPrio = new PrioritizedRunnable(samePriority, RUNNABLE_STUB);
        PrioritizedRunnable lowPrio = new PrioritizedRunnable(samePriority, RUNNABLE_STUB);
        Assert.assertTrue(COMPARATOR.compare(lowPrio, highPrio) == 0);
    }

    @Test
    public void testPriorityQueue() {
        PrioritizedRunnable highPrio = new PrioritizedRunnable(PrioritizedRunnable.HIGH_PRIORITY, RUNNABLE_STUB);
        PrioritizedRunnable lowPrio = new PrioritizedRunnable(PrioritizedRunnable.LOW_PRIORITY, RUNNABLE_STUB);

        PriorityQueue<PrioritizedRunnable> queue = new PriorityQueue<>(2, COMPARATOR);
        queue.add(lowPrio);
        queue.add(highPrio);
        Assert.assertTrue(queue.poll() == highPrio);

        queue.clear();

        queue.add(highPrio);
        queue.add(lowPrio);
        Assert.assertTrue(queue.poll() == highPrio);
    }

}
