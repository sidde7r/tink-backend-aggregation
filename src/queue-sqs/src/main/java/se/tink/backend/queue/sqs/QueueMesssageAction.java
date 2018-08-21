package se.tink.backend.queue.sqs;

import java.io.IOException;

public interface QueueMesssageAction<T> {
    void handle(String message) throws IOException;
}
