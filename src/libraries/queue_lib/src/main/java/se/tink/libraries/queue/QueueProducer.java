package se.tink.libraries.queue;

public interface QueueProducer {

    void send(Object t);

    void requeue(String encodedMessageBody);

    void requeueRateLimit(String encodedMessageBody);

    boolean isAvailable();
}
