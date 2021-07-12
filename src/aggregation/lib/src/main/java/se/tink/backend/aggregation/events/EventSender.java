package se.tink.backend.aggregation.events;

import com.google.inject.Inject;
import com.google.protobuf.Message;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.events.api.EventSubmitter;
import se.tink.libraries.events.guice.EventSubmitterProvider;

public class EventSender {

    private static final Logger log = LoggerFactory.getLogger(EventSender.class);
    private EventSubmitter eventSubmitter;

    @Inject
    public EventSender(EventSubmitterProvider eventSubmitterProvider) {
        try {
            this.eventSubmitter = eventSubmitterProvider.get();
        } catch (Exception e) {
            log.warn("Could not create eventSubmitter. Cause {}", ExceptionUtils.getStackTrace(e));
        }
    }

    public void sendMessages(List<Message> messages) {
        try {
            if (messages.isEmpty()) {
                return;
            }
            eventSubmitter.submit(messages);
        } catch (Exception e) {
            log.warn("Failed to send batch message. Cause: {}", ExceptionUtils.getStackTrace(e));
        }
    }
}
