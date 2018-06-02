package se.tink.backend.common.tasks.kafka;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import java.util.Properties;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.xerial.snappy.Snappy;
import se.tink.backend.common.config.TasksQueueConfiguration;
import se.tink.backend.common.tasks.interfaces.TaskSubmitter;
import se.tink.backend.system.tasks.Task;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.Histogram;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class KafkaTaskSubmitter implements TaskSubmitter {
    private static final LogUtils log = new LogUtils(KafkaTaskSubmitter.class);
    private static final int KILOBYTE = 1024;
    private static final int MEGABYTE = 1024 * KILOBYTE;
    private static final ImmutableList<Integer> RECORD_SIZE_BUCKETS = ImmutableList.of(
            0,
            100 * KILOBYTE,
            200 * KILOBYTE,
            400 * KILOBYTE,
            800 * KILOBYTE,
            1600 * KILOBYTE,
            3200 * KILOBYTE,
            6400 * KILOBYTE,
            12800 * KILOBYTE,
            20 * MEGABYTE
    );

    private final boolean enabled;

    private KafkaProducer<Object, byte[]> producer;
    private Histogram recordSizeHistogram;
    private final TasksQueueConfiguration configuration;
    private final MetricRegistry metricRegistry;

    @Inject
    public KafkaTaskSubmitter(MetricRegistry metricRegistry, TasksQueueConfiguration queueConfiguration) {
        this.metricRegistry = metricRegistry;
        this.recordSizeHistogram = metricRegistry.histogram(MetricId.newId("kafka_record_size"), RECORD_SIZE_BUCKETS);
        this.configuration = queueConfiguration;
        this.enabled = TasksQueueConfiguration.SHOULD_RUN.contains(configuration.getMode());
    }

    @Override
    public Future<Void> submit(Task<?> task) {

        byte[] bytePayload;

        try {
            bytePayload = Snappy.compress(SerializationUtils.serializeToBinary(task));
            recordSizeHistogram.update(bytePayload.length);
        } catch (Exception e) {
            log.error("Could not serialize task", e);
            return Futures.immediateFailedFuture(e);
        }

        Future<RecordMetadata> future = producer
                .send(new ProducerRecord<>(task.getTopic(), task.getPartitionKey(), bytePayload),
                        (data, e) -> {
                            if (data != null) {
                                log.debug(String.format(
                                        "Successfully sent task to kafka. Topic:'%s' Partition:'%d' Offset:'%d'",
                                        data.topic(), data.partition(), data.offset()));
                            } else {
                                log.error("Could not send data to kafka", e);
                            }
                        });

        return Futures.lazyTransform(future, Functions.<Void>constant(null));
    }

    @PostConstruct
    public void start() throws Exception {
        if (!enabled) {
            return;
        }

        Properties properties = new Properties();
        properties.put("acks", "all");
        properties.put("batch.size", 16384);
        properties.put("bootstrap.servers", configuration.getHosts());
        properties.put("buffer.memory", 33554432);
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("linger.ms", 1);
        properties.put("max.request.size", 104857600);
        properties.put("retries", 4);
        properties.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        producer = new KafkaProducer<>(properties);

        KafkaMetricsUtils.registerMetrics(KafkaTaskSubmitter.class, metricRegistry, producer.metrics());
    }

    @PreDestroy
    public void stop() {
        if (!enabled) {
            return;
        }

        KafkaMetricsUtils.removeMetrics(KafkaTaskSubmitter.class, metricRegistry, producer.metrics());

        producer.close();
    }
}
