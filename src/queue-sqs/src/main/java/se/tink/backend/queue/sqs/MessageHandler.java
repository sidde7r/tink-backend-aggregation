package se.tink.backend.queue.sqs;

public interface MessageHandler<T> {
    void handle(byte[] message);
}
