package se.tink.libraries.queue;

public interface QueueProducer {

    void send(Object t);

    void requeue(String encodedMessageBody);

    boolean isAvailable();
}
