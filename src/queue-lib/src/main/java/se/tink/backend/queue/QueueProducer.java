package se.tink.backend.queue;

public interface QueueProducer {

    void send(Object t);

    boolean isAvailable();

    void requeue(String t);

}
