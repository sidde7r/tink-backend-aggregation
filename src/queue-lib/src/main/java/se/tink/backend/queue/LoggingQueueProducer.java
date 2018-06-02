package se.tink.backend.queue;

import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingQueueProducer implements QueueProducer {

    private static final Logger log = LoggerFactory.getLogger(LoggingQueueProducer.class);

    @Override
    public void start() {
        // Deliverately left empty.
    }

    @Override
    public void stop() {
        // Deliverately left empty.
    }

    @Override
    public void send(String topic, String partitionId, Message message) {
        log.debug(String.format("[topic:%s partitionId:%s] No valid queue producer instantiated, not sending message.",
                topic, partitionId));
    }
}
