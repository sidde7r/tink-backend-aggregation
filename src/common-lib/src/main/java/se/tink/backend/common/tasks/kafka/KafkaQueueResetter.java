package se.tink.backend.common.tasks.kafka;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import se.tink.backend.system.rpc.ReplayQueueRequest;

public class KafkaQueueResetter {

    private ArrayBlockingQueue<ReplayQueueRequest> queue = new ArrayBlockingQueue<>(1);

    public Optional<ReplayQueueRequest> poll() {
        return Optional.ofNullable(queue.poll());
    }

    public boolean register(ReplayQueueRequest request) {
        return queue.offer(request);
    }
}
