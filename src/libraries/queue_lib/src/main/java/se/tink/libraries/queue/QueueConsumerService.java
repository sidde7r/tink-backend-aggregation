package se.tink.libraries.queue;

import io.dropwizard.lifecycle.Managed;

public interface QueueConsumerService extends Managed {
    void consume(String message) throws Exception;
}
