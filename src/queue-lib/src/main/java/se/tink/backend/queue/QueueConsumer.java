package se.tink.backend.queue;

import com.google.common.util.concurrent.Service;

import java.io.IOException;
import java.time.Instant;

public interface QueueConsumer {
    void consume(String message) throws IOException;
}
