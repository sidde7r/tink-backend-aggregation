package se.tink.backend.queue;

public interface QueueProducer {
    void start();

    void stop();

    void send(Object t);

    boolean isAvailable();
}
