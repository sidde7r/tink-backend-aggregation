package se.tink.backend.libraries.queue.sqs;

import java.io.IOException;

public interface EncodingHandler<T> {
    String encode(T t) throws IOException;
    T decode(String message) throws IOException;
}
