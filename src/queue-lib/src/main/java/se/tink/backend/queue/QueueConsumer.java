package se.tink.backend.queue;

public interface QueueConsumer {
    void consume(String message) throws Exception;
}
