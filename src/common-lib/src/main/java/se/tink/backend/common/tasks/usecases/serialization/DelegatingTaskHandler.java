package se.tink.backend.common.tasks.usecases.serialization;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.eventbus.EventBus;
import java.util.List;
import org.xerial.snappy.Snappy;
import se.tink.libraries.metrics.Histogram;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.backend.common.tasks.interfaces.GenericTaskHandler;
import se.tink.backend.common.tasks.interfaces.TaskHandler;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.system.tasks.Task;
import se.tink.backend.utils.LogUtils;

/**
 * A generic task handler that 1) deserializes a job payload and 2) delegates handling of the task to the right
 * {@link TaskHandler}.
 * <p>
 * Food for thought: Could/Should this be using an {@link EventBus} for delegation? Should we allow multiple
 * subscribers?
 * </p>
 */
public class DelegatingTaskHandler implements GenericTaskHandler {

    private static final LogUtils log = new LogUtils(DelegatingTaskHandler.class);

    private final ImmutableMap<Class<? extends Task>, TaskHandler<? extends Task>> handlers;
    private final Histogram payloadSizeHistogram;
    private static final MetricId HANDLE_TIMER_METRIC_NAME = MetricId.newId("generic_task_handler_handle");
    private static final MetricId PAYLOAD_SIZE_METRIC_NAME = MetricId.newId("generic_task_handler_payload_size");
    private final Timer handleTimer;

    /**
     * Constructor.
     * 
     * @param handlers
     *            the task handlers that register jobs to handle.
     * @throws IllegalArgumentException
     *             if two handlers handles that same class.
     */
    public DelegatingTaskHandler(MetricRegistry metricRegistry, List<? extends TaskHandler<? extends Task>> handlers)
            throws IllegalArgumentException {

        Builder<Class<? extends Task>, TaskHandler<? extends Task>> mapBuilder = ImmutableMap.builder();

        for (TaskHandler<? extends Task> handler : handlers) {
            registerHandler(mapBuilder, handler);
        }

        this.payloadSizeHistogram = metricRegistry.histogram(PAYLOAD_SIZE_METRIC_NAME);
        this.handleTimer = metricRegistry.timer(HANDLE_TIMER_METRIC_NAME);

        this.handlers = mapBuilder.build();
    }

    private void registerHandler(Builder<Class<? extends Task>, TaskHandler<? extends Task>> mapBuilder,
            TaskHandler<? extends Task> handler) {

        for (Class<? extends Task> handledClass : handler.handles()) {
            registerHandler(mapBuilder, handler, handledClass);
        }
    }

    private void registerHandler(Builder<Class<? extends Task>, TaskHandler<? extends Task>> mapBuilder,
            TaskHandler<? extends Task> handler, Class<? extends Task> handledClass) {

        // Classes that probably shouldn't be handler classes.
        Preconditions.checkArgument(!handledClass.isAnnotation());
        Preconditions.checkArgument(!handledClass.isMemberClass());

        mapBuilder.put(handledClass, handler);
    }

    @Override
    public void handle(byte[] payload) {
        if (payload != null) {
            this.payloadSizeHistogram.update(payload.length);
        }

        Task deserializedPayload = null;

        try {
            deserializedPayload = SerializationUtils.deserializeFromBinary(Snappy.uncompress(payload), Task.class);
        } catch (Exception e) {
            log.error("Could not deserialize task", e);
        }

        Preconditions.checkNotNull(deserializedPayload);

        TaskHandler<? extends Task> handler = handlers.get(deserializedPayload.getClass());

        // Handle unmatched handler.

        if (handler == null) {
            log.error(String.format("Unable to find a handler job with class %s", deserializedPayload.getClass()));
        }

        Preconditions.checkNotNull(handler);

        Timer.Context handleTimerContext = handleTimer.time();

        // Unwrap the job payload and delegate to the runner.
        try {
            handler.handle(deserializedPayload);
        } finally {
            handleTimerContext.stop();
        }
    }
}
