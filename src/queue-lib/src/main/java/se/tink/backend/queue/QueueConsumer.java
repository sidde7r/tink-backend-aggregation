package se.tink.backend.queue;

import java.io.IOException;

public interface QueueConsumer {
    QueuableJob consume(String message) throws IOException;
}
