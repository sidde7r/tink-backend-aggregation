package se.tink.backend.grpc.v1.streaming;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import se.tink.backend.common.concurrency.TwoLevelExpiredConcurrentCache;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.queue.QueueConsumer;
import se.tink.backend.utils.LogUtils;
import se.tink.grpc.v1.rpc.StreamingResponse;
import se.tink.libraries.metrics.Gauge;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class StreamingQueueConsumerHandler implements QueueConsumer.QueueConsumerHandler<FirehoseMessage> {
    private static final LogUtils log = new LogUtils(StreamingQueueConsumerHandler.class);
    private final BiConsumer<String, StreamingResponseHandler> evictionListener;
    private final MetricRegistry metricRegistry;
    private TwoLevelExpiredConcurrentCache<StreamingResponseHandler> subscribersCache;

    private static final MetricId STEAMING_QUEUE_METRIC = MetricId.newId("streaming_queue_consumer");
    private static final MetricId STEAMING_QUEUE_SIZE = STEAMING_QUEUE_METRIC.label("metric", "size");
    private static final MetricId STEAMING_QUEUE_HIT_COUNT = STEAMING_QUEUE_METRIC.label("metric", "hit_count");
    private static final MetricId STEAMING_QUEUE_MISS_COUNT = STEAMING_QUEUE_METRIC.label("metric", "miss_count");

    public StreamingQueueConsumerHandler(BiConsumer<String, StreamingResponseHandler> evictionListener,
            MetricRegistry metricRegistry) {
        this.evictionListener = evictionListener;
        this.metricRegistry = metricRegistry;
    }

    public void register(StreamingResponseHandler handler) {
        subscribersCache.put(handler.getUserId(), handler.getDeviceId(), handler);
        log.info(handler.getUserId(), "Register device for streaming: " + handler.getDeviceId());
    }

    public void updateExpiration(String userId, String deviceId) {
        Optional<StreamingResponseHandler> handler = subscribersCache.updateExpiration(userId, deviceId);
        if (handler.isPresent()) {
            handler.get().sendKeepAliveMessage();
            log.trace(userId, "Keep alive streaming for device: " + deviceId);
        }
    }

    @Override
    public void handle(FirehoseMessage message, Instant timestamp) {
        Collection<StreamingResponseHandler> handlers = subscribersCache.get(message.getUserId()).values();

        if (handlers.isEmpty()) {
            log.trace(message.getUserId(), "User doesn't have subscription for streaming");
        }

        for (StreamingResponseHandler handler : handlers) {
            if (handler.isCancelled()) {
                remove(handler);
            } else {
                handler.handle(message, timestamp);
            }
        }
    }

    public void sendContext(String userId, StreamingResponse context) {
        subscribersCache.get(userId).values().forEach(c -> c.sendContext(context));
    }

    @PostConstruct
    private void start() {
        this.subscribersCache = new TwoLevelExpiredConcurrentCache<>(evictionListener);

        metricRegistry.registerSingleton(STEAMING_QUEUE_SIZE, new Gauge() {
            @Override
            public double getValue() {
                return subscribersCache.size();
            }
        });

        metricRegistry.registerSingleton(STEAMING_QUEUE_HIT_COUNT, new Gauge() {
            @Override
            public double getValue() {
                return subscribersCache.stats().hitCount();
            }
        });

        metricRegistry.registerSingleton(STEAMING_QUEUE_MISS_COUNT, new Gauge() {
            @Override
            public double getValue() {
                return subscribersCache.stats().missCount();
            }
        });
    }

    @PreDestroy
    private void shutdown() {
        subscribersCache.shutdown();
    }

    public Optional<StreamingResponseHandler> remove(String userId, String deviceId) {
        return subscribersCache.remove(userId, deviceId);
    }

    public Optional<StreamingResponseHandler> remove(StreamingResponseHandler handler) {
        return subscribersCache.remove(handler.getUserId(), handler.getDeviceId());
    }
}
