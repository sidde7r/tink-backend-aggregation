package se.tink.backend.queue.kafka;

import java.util.Optional;
import java.util.UUID;

public class KafkaQueueConsumerProperties {
    private String groupId = UUID.randomUUID().toString();
    private boolean startFromLastMessage;
    private int maxPollRecords;
    private String hosts;

    boolean shouldStartFromLastMessage() {
        return startFromLastMessage;
    }

    public void setStartFromLastMessage(Boolean startFromLastMessage) {
        this.startFromLastMessage = startFromLastMessage;
    }

    Optional<Integer> getMaxPollRecords() {
        return maxPollRecords <= 0 ? Optional.empty() : Optional.of(maxPollRecords);
    }

    public void setMaxPollRecords(int maxPollRecords) {
        this.maxPollRecords = maxPollRecords;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    String getGroupId() {
        return groupId;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    String getHosts() {
        return hosts;
    }
}
