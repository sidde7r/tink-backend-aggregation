package se.tink.backend.queue;

import com.google.common.util.concurrent.Service;
import com.google.protobuf.Message;
import java.time.Instant;

public interface QueueConsumer extends Service {
    interface QueueConsumerHandler<T extends Message> {
        void handle(T message, Instant timestamp);
    }
}
