package se.tink.backend.queue.sqs;

import se.tink.backend.queue.QueueProducer;

public class FakeProducer implements QueueProducer {

    @Override
    public void send(Object t) {

    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void requeue(String t) {

    }
}
