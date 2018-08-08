package se.tink.backend.queue;

import com.google.common.util.concurrent.Service;

import java.time.Instant;

public interface QueueConsumer extends Service {
    interface QueueConsumerHandler<T extends Object> {
        void handle(T message, Instant timestamp);
    }
}
