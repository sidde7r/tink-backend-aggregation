package se.tink.backend.libraries.queue.sqs;

import java.io.IOException;

public interface QueueMessageAction<T> {
    void handle(String message) throws IOException;
}
