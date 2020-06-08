package se.tink.libraries.queue.sqs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SqsConsumerTest {

    private SqsQueue queue;

    @Before
    public void setUp() {
        queue = Mockito.mock(SqsQueue.class);
    }

    @Test
    public void whenUnavailableQueueSqsConsumerShouldNotThrowWhenStarted() {
        Mockito.when(queue.isAvailable()).thenReturn(false);

        SqsConsumer consumer = new SqsConsumer(queue, null, null);

        try {
            consumer.start();
        } catch (Exception e) {
            Assert.fail(
                    "Should not throw exception if trying to start consumer that does not have available Queue");
        }
    }

    @Test
    public void whenUnstartedSqsConsumerShouldNotThrowIfStopped() {
        SqsConsumer consumer = new SqsConsumer(queue, null, null);

        try {
            consumer.stop();
        } catch (Exception e) {
            Assert.fail(
                    "Should not throw exception if trying to stop consumer that was not started");
        }
    }
}
