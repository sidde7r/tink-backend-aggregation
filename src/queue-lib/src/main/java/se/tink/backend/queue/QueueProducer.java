package se.tink.backend.queue;

import com.google.protobuf.Message;

public interface QueueProducer {
    void start();

    void stop();

    void send(String topic, String partitionId, Message message);
}
