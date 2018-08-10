package se.tink.backend.queue.sqs;

import java.io.IOException;

public interface MessageHandler<T> {
    void handle(String message) throws IOException;
}
