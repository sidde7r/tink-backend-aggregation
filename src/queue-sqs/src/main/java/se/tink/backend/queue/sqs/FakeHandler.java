package se.tink.backend.queue.sqs;

import java.io.IOException;
import se.tink.backend.queue.QueuableJob;

public class FakeHandler implements MessageHandler {
    @Override
    public QueuableJob handle(String message) throws IOException {
        return null;
    }
}
