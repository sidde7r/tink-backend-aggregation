package se.tink.backend.queue;

import com.google.common.util.concurrent.AbstractService;

public class MockQueueConsumer extends AbstractService implements QueueConsumer {
    @Override
    protected void doStart() {
        notifyStarted();
    }

    @Override
    protected void doStop() {
        notifyStopped();
    }
}
