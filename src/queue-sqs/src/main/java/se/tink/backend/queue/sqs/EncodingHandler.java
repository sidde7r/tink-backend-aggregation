package se.tink.backend.queue.sqs;

public interface EncodingHandler<T> {
    byte[] encode(T t);
    T decode(byte[] message);
}
