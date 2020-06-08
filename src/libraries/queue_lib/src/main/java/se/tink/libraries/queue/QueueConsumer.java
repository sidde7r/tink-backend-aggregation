package se.tink.libraries.queue;

import io.dropwizard.lifecycle.Managed;

public interface QueueConsumer extends Managed {
    void consume(String message) throws Exception;
}
