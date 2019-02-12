package se.tink.libraries.queue;

public interface QueueConsumer {
    void consume(String message) throws Exception;
}
