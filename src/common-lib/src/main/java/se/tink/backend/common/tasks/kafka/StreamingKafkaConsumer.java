package se.tink.backend.common.tasks.kafka;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import io.dropwizard.lifecycle.Managed;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.common.config.TasksQueueConfiguration;
import se.tink.backend.common.retry.RetryerBuilder;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.tasks.interfaces.GenericTaskHandler;

public class StreamingKafkaConsumer implements Managed {
    private static final LogUtils log = new LogUtils(StreamingKafkaConsumer.class);
    private final Properties streamsProperties;
    private final List<String> topics;
    private final GenericTaskHandler messageHandler;
    private final Retryer<Void> retryer;
    private final Counter abortedStreamingCounter;
    private final int RETRY_MULTIPLIER = 1000;
    private final int RETRY_MAX_TIME = 300;
    private KafkaStreams streams;

    public StreamingKafkaConsumer(TasksQueueConfiguration queueConfiguration, List<String> topics,
            GenericTaskHandler messageHandler, MetricRegistry metricRegistry) {
        this.topics = topics;
        this.messageHandler = messageHandler;
        streamsProperties = new Properties();
        streamsProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, queueConfiguration.getGroupId());
        streamsProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, queueConfiguration.getHosts());
        streamsProperties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsProperties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, queueConfiguration.getWorkers());
        streamsProperties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, queueConfiguration.getConsumerMaxPollRecords());
        abortedStreamingCounter = metricRegistry.meter(MetricId.newId("aborted_kafka_streaming"));
        this.retryer = RetryerBuilder.<Void>newBuilder(log, "kafka-consumer")
                .retryIfException()
                .withStopStrategy(StopStrategies.stopAfterAttempt(queueConfiguration.getNumRetries()))
                .withWaitStrategy(WaitStrategies.fibonacciWait(RETRY_MULTIPLIER, RETRY_MAX_TIME, TimeUnit.SECONDS))
                .build();
    }

    @Override
    public void start() {
        KStreamBuilder streamBuilder = new KStreamBuilder();
        streamBuilder.<String, byte[]>stream(topics.toArray(new String[topics.size()]))
                .foreach(this::handle);
        streams = new KafkaStreams(streamBuilder, streamsProperties);
        streams.start();
        log.debug("Started streaming Kafka consumer");
    }

    public void handle(String key, byte[] message) {
        log.trace("Processing message for key {}", key);
        try {
            retryer.call(() -> {
                messageHandler.handle(message);
                return null;
            });
        } catch (ExecutionException | RetryException e) {
            abortedStreamingCounter.inc();
            log.error("Cannot process message for key {}. The consumer will not continue. Processing will be resumed "
                    + "after node restart.", key, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        streams.close();
        log.debug("Shut down streaming Kafka consumer");
    }
}
