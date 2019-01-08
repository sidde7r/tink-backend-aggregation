package se.tink.backend.libraries.queue;

public interface QueueProducer {

    void send(Object t);

    boolean isAvailable();

}
