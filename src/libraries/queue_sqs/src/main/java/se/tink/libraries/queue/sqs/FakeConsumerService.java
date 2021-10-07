package se.tink.libraries.queue.sqs;

import se.tink.libraries.queue.QueueConsumerService;

public class FakeConsumerService implements QueueConsumerService {

    @Override
    public void consume(String message) throws Exception {}

    @Override
    public void start() throws Exception {}

    @Override
    public void stop() throws Exception {}
}
