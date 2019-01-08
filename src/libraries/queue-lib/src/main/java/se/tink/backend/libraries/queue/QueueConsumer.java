package se.tink.backend.libraries.queue;

public interface QueueConsumer {
    void consume(String message) throws Exception;
}
