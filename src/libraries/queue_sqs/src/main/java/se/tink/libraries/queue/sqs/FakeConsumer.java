package se.tink.libraries.queue.sqs;

import se.tink.libraries.queue.QueueConsumer;

public class FakeConsumer implements QueueConsumer {

    @Override
    public void consume(String message) throws Exception {}

    @Override
    public void start() throws Exception {}

    @Override
    public void stop() throws Exception {}
}
