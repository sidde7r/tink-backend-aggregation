package se.tink.backend.queue.kafka;

import com.google.protobuf.Message;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.queue.QueueProducer;

public class KafkaQueueProducer implements QueueProducer {
    private static final Logger log = LoggerFactory.getLogger(KafkaQueueProducer.class);

    private final String servers;
    private final KafkaProducer<String, byte[]> producer;

    public KafkaQueueProducer(String servers) {
        this.servers = servers;

        Properties properties = new Properties();
        properties.put("acks", "all");
        properties.put("batch.size", 16384);
        properties.put("bootstrap.servers", servers);
        properties.put("buffer.memory", 33554432);
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("linger.ms", 1);
        properties.put("max.request.size", 20971520);
        properties.put("retries", 4);
        properties.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        producer = new KafkaProducer<>(properties);
    }

    @PostConstruct
    @Override
    public void start() {

    }

    @PreDestroy
    @Override
    public void stop() {
        producer.close();
    }

    @Override
    public void send(String topic, String partitionId, Message message) {
        producer.send(new ProducerRecord<>(topic, partitionId, message.toByteArray()), (record, e) -> {
            if (record != null) {
                if (log.isTraceEnabled()) {
                    log.trace(
                            String.format("Successfully sent task to kafka. Topic:'%s' Partition:'%d' Offset:'%d'",
                                    record.topic(), record.partition(), record.offset()));
                }
            } else {
                log.error("Could not send data to queue", e);
            }
        });
    }
}
