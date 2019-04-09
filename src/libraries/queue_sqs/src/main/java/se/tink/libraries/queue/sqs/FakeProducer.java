package se.tink.libraries.queue.sqs;

import se.tink.libraries.queue.QueueProducer;

public class FakeProducer implements QueueProducer {

    @Override
    public void send(Object t) {}

    @Override
    public boolean isAvailable() {
        return false;
    }
}
