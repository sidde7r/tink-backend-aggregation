package se.tink.backend.queue.kafka;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.concurrency.TypedThreadPoolBuilder;
import se.tink.backend.common.utils.ExecutorServiceUtils;
import se.tink.backend.queue.QueueConsumer;
import se.tink.libraries.metrics.MetricRegistry;

public class KafkaQueueConsumer extends AbstractExecutionThreadService implements QueueConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaQueueConsumer.class);
    private static final ThreadFactory firehoseConsumerExecutorThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("firehose-consumer-thread-%d")
            .build();

    private final KafkaConsumer<String, byte[]> consumer;
    private final ListMultimap<String, KafkaQueueConsumerSubscriber> subscribers = LinkedListMultimap.create();
    private final MetricRegistry metricRegistry;
    private ListenableThreadPoolExecutor<Runnable> messageSenderExecutorService = null;
    private final boolean startFromLatestMessage;

    @Inject
    public KafkaQueueConsumer(MetricRegistry metricRegistry, List<KafkaQueueConsumerSubscriber> subscribers,
            KafkaQueueConsumerProperties consumerProperties) {
        this.metricRegistry = metricRegistry;
        this.startFromLatestMessage = consumerProperties.shouldStartFromLastMessage();

        Properties properties = new Properties();
        properties.put("auto.commit.interval.ms", "5000");
        properties.put("enable.auto.commit", "true");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("max.partition.fetch.bytes", 20971520);
        properties.put("request.timeout.ms", 60000);
        properties.put("session.timeout.ms", 59000);
        properties.put("heartbeat.interval.ms", 4000);
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        properties.put("bootstrap.servers", consumerProperties.getHosts());
        properties.put("group.id", consumerProperties.getGroupId());

        if (consumerProperties.getMaxPollRecords().isPresent()) {
            properties.put("max.poll.records", consumerProperties.getMaxPollRecords().get());
        }

        consumer = new KafkaConsumer<>(properties);
        subscribers.forEach(this::subscribe);
    }

    @PostConstruct
    public void start() {
        startAsync();
    }

    @Override
    protected void startUp() throws Exception {
        super.startUp();
        initExecutor();
    }

    @PreDestroy
    @Override
    protected void shutDown() throws Exception {
        super.shutDown();
        stopExecutor();
    }

    @Override
    protected void run() throws Exception {
        consumer.subscribe(subscribers.keySet());

        // We are requested to start processing from the last message in the log.
        if (startFromLatestMessage) {
            // Poll the first message so that we get topic partitions assigned. Calling `consumer.assignment()` without
            // first doing a poll will not return the assigned partitions.
            pollAndProcess(0);

            log.info("Seeking partitions to end and continue processing.");
            consumer.seekToEnd(consumer.assignment());
        }

        try {
            while (isRunning()) {
                pollAndProcess(3000);
            }
        } finally {
            consumer.close();
        }
    }

    private void pollAndProcess(long timeout) {
        ConsumerRecords<String, byte[]> records = consumer.poll(timeout);

        for (final ConsumerRecord<String, byte[]> record : records) {
            if (!subscribers.containsKey(record.topic())) {
                log.warn("No subscription for topic: " + record.topic());
                continue;
            }

            List<KafkaQueueConsumerSubscriber> topicSubscribers = subscribers.get(record.topic());

            for (final KafkaQueueConsumerSubscriber subscriber : topicSubscribers) {
                messageSenderExecutorService.execute(() -> {
                    try {
                        subscriber.parse(record.value(), record.timestamp());
                    } catch (Exception e) {
                        log.error("Could not parse data from Kafka", e);
                    }
                });
            }
        }
    }

    public <T extends Message> void subscribe(KafkaQueueConsumerSubscriber<T> subscriber) {
        subscribers.put(subscriber.topic, subscriber);
    }

    public void initExecutor() {
        messageSenderExecutorService = ListenableThreadPoolExecutor.builder(
                Queues.newLinkedBlockingQueue(),
                new TypedThreadPoolBuilder(10, firehoseConsumerExecutorThreadFactory))
                .withMetric(metricRegistry, "firehose_consumer_executor_service")
                .build();
    }

    private void stopExecutor() {
        if (messageSenderExecutorService != null) {
            ExecutorServiceUtils
                    .shutdownExecutor("KafkaQueueConsumer#executorService", messageSenderExecutorService,
                            20, TimeUnit.SECONDS);

            messageSenderExecutorService = null;
        }
    }

    public static class KafkaQueueConsumerSubscriber<T extends Message> {
        private final QueueConsumerHandler<T> handler;
        private final Parser<T> parser;
        private final String topic;

        public KafkaQueueConsumerSubscriber(String topic, Parser<T> parser, QueueConsumerHandler<T> handler) {
            this.topic = topic;
            this.parser = parser;
            this.handler = handler;
        }

        public void parse(byte[] data, long created) throws Exception {
            handler.handle(parser.parseFrom(data), Instant.ofEpochMilli(created));
        }
    }
}
