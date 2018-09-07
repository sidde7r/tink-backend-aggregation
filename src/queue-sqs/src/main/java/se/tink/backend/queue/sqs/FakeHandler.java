package se.tink.backend.queue.sqs;

import java.io.IOException;

public class FakeHandler implements QueueMessageAction {
    @Override
    public void handle(String message) throws IOException {
    }
}
