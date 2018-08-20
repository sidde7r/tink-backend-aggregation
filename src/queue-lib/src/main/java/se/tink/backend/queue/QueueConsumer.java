package se.tink.backend.queue;

public interface QueueConsumer {
    QueuableJob consume(String message) throws Exception;
}
