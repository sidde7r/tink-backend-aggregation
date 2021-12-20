package se.tink.libraries.queue.sqs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.sqs.model.Message;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;

public class SqsConsumerServiceTest {

    private SqsConsumer regularSqsConsumer;
    private SqsConsumer prioritySqsConsumer;
    private AgentsServiceConfiguration agentsServiceConfiguration;

    @Before
    public void setUp() {
        regularSqsConsumer = mock(SqsConsumer.class);
        prioritySqsConsumer = mock(SqsConsumer.class);
        agentsServiceConfiguration = mock(AgentsServiceConfiguration.class);
    }

    @Test
    public void whenUnavailableQueueSqsConsumerShouldNotThrowWhenStarted() {
        when(regularSqsConsumer.isConsumerReady()).thenReturn(false);

        SqsConsumerService consumerService =
                new SqsConsumerService(
                        regularSqsConsumer, prioritySqsConsumer, agentsServiceConfiguration, 0.0f);

        try {
            consumerService.start();
        } catch (Exception e) {
            Assert.fail(
                    "Should not throw exception if trying to start consumer that does not have available Queue");
        }
    }

    @Test
    public void whenUnstartedSqsConsumerShouldNotThrowIfStopped() {
        SqsConsumerService consumerService =
                new SqsConsumerService(
                        regularSqsConsumer, prioritySqsConsumer, agentsServiceConfiguration, 0.0f);

        try {
            consumerService.stop();
        } catch (Exception e) {
            Assert.fail(
                    "Should not throw exception if trying to stop consumer that was not started");
        }
    }

    @Test
    public void shouldNotStartIfRegularQueueRequiredButNotAvailable() throws Exception {
        // given
        when(regularSqsConsumer.isConsumerReady()).thenReturn(false);

        SqsConsumerService consumerService =
                new SqsConsumerService(
                        regularSqsConsumer, prioritySqsConsumer, agentsServiceConfiguration, 0.0f);

        // when
        consumerService.start();

        // then
        assertThat(consumerService.isRunning()).isFalse();
    }

    @Test
    public void shouldStartIfRegularQueueRequiredAndItIsAvailable() throws Exception {
        // given
        when(regularSqsConsumer.isConsumerReady()).thenReturn(true);

        SqsConsumerService consumerService =
                new SqsConsumerService(
                        regularSqsConsumer, prioritySqsConsumer, agentsServiceConfiguration, 0.0f);

        // when
        consumerService.start();

        // then
        assertThat(consumerService.isRunning()).isTrue();
    }

    @Test
    public void shouldNotStartIfBothQueuesRequiredButOnlyOneAvailable() throws Exception {
        // given
        when(agentsServiceConfiguration.isFeatureEnabled("consumeFromPriorityQueue"))
                .thenReturn(true);
        when(regularSqsConsumer.isConsumerReady()).thenReturn(true);
        when(prioritySqsConsumer.isConsumerReady()).thenReturn(false);

        SqsConsumerService consumerService =
                new SqsConsumerService(
                        regularSqsConsumer, prioritySqsConsumer, agentsServiceConfiguration, 0.0f);

        // when
        consumerService.start();

        // then
        assertThat(consumerService.isRunning()).isFalse();
    }

    @Test
    public void shouldStartIfBothRequiredQueuesAvailable() throws Exception {
        // given
        when(agentsServiceConfiguration.isFeatureEnabled("consumeFromPriorityQueue"))
                .thenReturn(true);
        when(regularSqsConsumer.isConsumerReady()).thenReturn(true);
        when(prioritySqsConsumer.isConsumerReady()).thenReturn(true);

        SqsConsumerService consumerService =
                new SqsConsumerService(
                        regularSqsConsumer, prioritySqsConsumer, agentsServiceConfiguration, 0.0f);

        // when
        consumerService.start();

        // then
        assertThat(consumerService.isRunning()).isTrue();
    }

    @Test
    public void shouldConsumeFromBothQueuesFollowingProperOrder() throws Exception {
        // given
        when(agentsServiceConfiguration.isFeatureEnabled("consumeFromPriorityQueue"))
                .thenReturn(true);
        when(regularSqsConsumer.isConsumerReady()).thenReturn(true);
        when(prioritySqsConsumer.isConsumerReady()).thenReturn(true);
        when(prioritySqsConsumer.getMessages())
                .thenReturn(Collections.singletonList(new Message()));
        when(regularSqsConsumer.getMessages()).thenReturn(Collections.singletonList(new Message()));
        InOrder inOrder = Mockito.inOrder(prioritySqsConsumer, regularSqsConsumer);

        SqsConsumerService consumerService =
                new SqsConsumerService(
                        regularSqsConsumer, prioritySqsConsumer, agentsServiceConfiguration, 0.0f);

        // when
        consumerService.consume();

        // then
        inOrder.verify(prioritySqsConsumer, times(1)).consume();
        inOrder.verify(regularSqsConsumer, times(1)).consume();
    }

    @Test
    public void shouldConsumeFromRegularQueueBasedOnMinimumConsumptionPercentage()
            throws Exception {
        final int ITERATIONS = 1000;
        final float REGULAR_QUEUE_MIN_CONSUMPTION = 0.5f;
        // given
        when(agentsServiceConfiguration.isFeatureEnabled("consumeFromPriorityQueue"))
                .thenReturn(true);
        when(regularSqsConsumer.isConsumerReady()).thenReturn(true);
        when(prioritySqsConsumer.isConsumerReady()).thenReturn(true);
        when(prioritySqsConsumer.consume()).thenReturn(true);

        SqsConsumerService consumerService =
                new SqsConsumerService(
                        regularSqsConsumer,
                        prioritySqsConsumer,
                        agentsServiceConfiguration,
                        REGULAR_QUEUE_MIN_CONSUMPTION);

        // when
        for (int i = 0; i < ITERATIONS; i++) {
            consumerService.consume();
        }

        // then
        verify(prioritySqsConsumer, times(ITERATIONS)).consume();

        verify(regularSqsConsumer, atLeast((int) (ITERATIONS * 0.45))).consume();
        verify(regularSqsConsumer, atMost((int) (ITERATIONS * 0.55))).consume();
    }

    @Test
    public void shouldConsumeOnlyFromRegularQueueIfConsumeFromPriorityQueueIsDisabled()
            throws Exception {
        // given
        when(agentsServiceConfiguration.isFeatureEnabled("consumeFromPriorityQueue"))
                .thenReturn(false);
        when(regularSqsConsumer.isConsumerReady()).thenReturn(true);
        when(regularSqsConsumer.getMessages()).thenReturn(Collections.singletonList(new Message()));

        SqsConsumerService consumerService =
                new SqsConsumerService(
                        regularSqsConsumer, prioritySqsConsumer, agentsServiceConfiguration, 0.0f);

        // when
        consumerService.consume();

        // then
        verify(regularSqsConsumer, times(1)).consume();
        verify(prioritySqsConsumer, never()).consume();
    }
}
