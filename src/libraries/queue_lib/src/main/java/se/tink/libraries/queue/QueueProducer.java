package se.tink.libraries.queue;

public interface QueueProducer {

    void send(Object t);

    boolean isAvailable();
}
