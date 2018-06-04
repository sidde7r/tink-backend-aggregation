package se.tink.backend.common.concurrency;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.libraries.metrics.IncrementDecrementGauge;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class ElementMonitoredQueueTest {

    private static final int OTHER_PRIORITY = 42;

    private static final Runnable STUB_RUNNABLE = () -> {
        // Deliberately left empty.
    };

    private static PrioritizedRunnable constructPrioritizedRunnable(int priority) {
        return new PrioritizedRunnable(priority, STUB_RUNNABLE);
    }

    private MetricId baseName = ElementMonitoredQueue.BASE_NAME_STUB.label("name", "test_queue");
    private MetricRegistry metricRegistry;

    private ElementMonitoredQueue<PrioritizedRunnable> queue;

    private void assertAllZero() {
        Assert.assertEquals(0.0, getLowPriorityGauge(), 0.0);
        Assert.assertEquals(0.0, getHighPriorityGauge(), 0.0);
        Assert.assertEquals(0.0, getOtherPriorityGauge(), 0.0);
    }

    private double getGauge(String priority) {
        IncrementDecrementGauge gauge = metricRegistry.incrementDecrementGauge(baseName.label("priority", priority));
        if (gauge == null) {
            return 0;
        }
        return gauge.getValue().doubleValue();
    }

    private double getHighPriorityGauge() {
        return getGauge(ElementMonitoredQueue.PrioritizedRunnableLabelExtractor.HIGH_PRIORITY_METRIC_NAME);
    }

    private double getLowPriorityGauge() {
        return getGauge(ElementMonitoredQueue.PrioritizedRunnableLabelExtractor.LOW_PRIORITY_METRIC_NAME);
    }

    private double getOtherPriorityGauge() {
        return getGauge(ElementMonitoredQueue.PrioritizedRunnableLabelExtractor.OTHER_PRIORITY_METRIC_NAME);
    }

    @Before
    public void setUp() {
        this.metricRegistry = new MetricRegistry();
        this.queue = new ElementMonitoredQueue(new ArrayBlockingQueue<Runnable>(20),
                new ElementMonitoredQueue.PrioritizedRunnableLabelExtractor(), metricRegistry, "test_queue",
                new MetricId.MetricLabels());

        assertAllZero();
    }

    @Test
    public void testHighPrio() {
        assertAllZero();

        queue.add(constructPrioritizedRunnable(PrioritizedRunnable.HIGH_PRIORITY));

        Assert.assertEquals(0.0, getLowPriorityGauge(), 0.0);
        Assert.assertEquals(1.0, getHighPriorityGauge(), 0.0);
        Assert.assertEquals(0.0, getOtherPriorityGauge(), 0.0);

        queue.poll();

        assertAllZero();
    }

    @Test
    public void testInitialValue() {
        assertAllZero();
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(queue.isEmpty());
        queue.add(constructPrioritizedRunnable(PrioritizedRunnable.HIGH_PRIORITY));
        Assert.assertFalse(queue.isEmpty());
        queue.poll();
        Assert.assertTrue(queue.isEmpty());
    }

    @Test
    public void testIterator() {
        Assert.assertEquals(0, queue.size());
        PrioritizedRunnable runnable = constructPrioritizedRunnable(PrioritizedRunnable.HIGH_PRIORITY);
        boolean offer = queue.offer(runnable);
        Assert.assertEquals(1, queue.size());

        Iterator<PrioritizedRunnable> it = queue.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertTrue(it.next() == runnable);
        Assert.assertFalse(it.hasNext());
        Assert.assertEquals(1, queue.size());

        it.remove();
        Assert.assertEquals(0, queue.size());

        assertAllZero();
    }

    @Test
    public void testLowPrio() {
        assertAllZero();

        queue.add(constructPrioritizedRunnable(PrioritizedRunnable.LOW_PRIORITY));

        Assert.assertEquals(1.0, getLowPriorityGauge(), 0.0);
        Assert.assertEquals(0.0, getHighPriorityGauge(), 0.0);
        Assert.assertEquals(0.0, getOtherPriorityGauge(), 0.0);

        queue.poll();

        assertAllZero();
    }

    @Test
    public void testOffer() {
        Assert.assertEquals(0, queue.size());
        queue.offer(constructPrioritizedRunnable(PrioritizedRunnable.HIGH_PRIORITY));
        Assert.assertEquals(1, queue.size());
    }

    @Test
    public void testOtherPrio() {
        assertAllZero();

        queue.add(constructPrioritizedRunnable(ElementMonitoredQueueTest.OTHER_PRIORITY));

        Assert.assertEquals(0.0, getLowPriorityGauge(), 0.0);
        Assert.assertEquals(0.0, getHighPriorityGauge(), 0.0);
        Assert.assertEquals(1.0, getOtherPriorityGauge(), 0.0);

        queue.poll();

        assertAllZero();
    }

    @Test
    public void testOtherPriority() {
        // Just making sure the constant isn't high or low priority.
        Assert.assertNotEquals(ElementMonitoredQueueTest.OTHER_PRIORITY, PrioritizedRunnable.HIGH_PRIORITY);
        Assert.assertNotEquals(ElementMonitoredQueueTest.OTHER_PRIORITY, PrioritizedRunnable.LOW_PRIORITY);
    }

    @Test
    public void testPeek() {
        Assert.assertTrue(queue.isEmpty());
        queue.add(constructPrioritizedRunnable(PrioritizedRunnable.HIGH_PRIORITY));
        Assert.assertFalse(queue.isEmpty());
        queue.peek();
        Assert.assertFalse(queue.isEmpty());
    }

    @Test
    public void testSize() {
        Assert.assertEquals(0, queue.size());
        queue.add(constructPrioritizedRunnable(PrioritizedRunnable.HIGH_PRIORITY));
        Assert.assertEquals(1, queue.size());
        queue.poll();
        Assert.assertEquals(0, queue.size());
    }

    @Test
    public void testIteratorRemoval() {
        assertAllZero();

        queue.add(constructPrioritizedRunnable(OTHER_PRIORITY));
        queue.add(constructPrioritizedRunnable(OTHER_PRIORITY));
        Iterator<PrioritizedRunnable> it = queue.iterator();
        Assert.assertEquals(2.0, getOtherPriorityGauge(), 0.0);

        boolean thrown = false;
        try {
            it.remove();
        } catch (IllegalStateException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);

        Assert.assertEquals(2.0, getOtherPriorityGauge(), 0.0);

        it.next();
        it.remove();
        Assert.assertEquals(1.0, getOtherPriorityGauge(), 0.0);

        thrown = false;
        try {
            it.remove();
        } catch (IllegalStateException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
        Assert.assertEquals(1.0, getOtherPriorityGauge(), 0.0);

        it.next();
        it.remove();
        Assert.assertEquals(0.0, getOtherPriorityGauge(), 0.0);
    }

}
