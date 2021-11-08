package se.tink.backend.aggregation.workers.commands;

import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventListSplitter {

    private static final Logger log = LoggerFactory.getLogger(EventListSplitter.class);

    private final long allowedListSize;

    public EventListSplitter(long allowedListSize) {
        this.allowedListSize = allowedListSize;
    }

    public List<List<Message>> splitMessageListIntoChunks(List<Message> messages) {
        List<List<Message>> result = new ArrayList<>();

        long totalSize =
                messages.stream().map(MessageLite::getSerializedSize).reduce(0, Integer::sum);
        log.info("Total event list size is {}", totalSize);
        if (totalSize >= allowedListSize) {
            List<Message> subBatch = new ArrayList<>();
            long subBatchSize = 0;
            for (Message message : messages) {
                int currentMessageSize = message.getSerializedSize();
                if (currentMessageSize > allowedListSize) {
                    log.warn(
                            "There is a very big event with size {} ignoring the event",
                            currentMessageSize);
                } else if (subBatchSize + currentMessageSize < allowedListSize) {
                    subBatch.add(message);
                    subBatchSize += currentMessageSize;
                } else {
                    result.add(subBatch);
                    subBatchSize = currentMessageSize;
                    subBatch = new ArrayList<>();
                    subBatch.add(message);
                }
            }
            if (!subBatch.isEmpty()) {
                result.add(subBatch);
            }
        } else {
            result.add(messages);
        }
        return result;
    }
}
