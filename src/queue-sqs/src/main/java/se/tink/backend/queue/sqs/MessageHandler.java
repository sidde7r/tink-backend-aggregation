package se.tink.backend.queue.sqs;

import java.io.IOException;
import se.tink.backend.queue.QueuableJob;

public interface MessageHandler<T> {
    QueuableJob handle(String message) throws IOException;
}
