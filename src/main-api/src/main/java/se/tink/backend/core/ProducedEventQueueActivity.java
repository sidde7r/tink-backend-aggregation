package se.tink.backend.core;

import java.util.Date;
import java.util.UUID;

/**
 * Represents an activity which we have generated and put on the event queue (Firehose). By storing these we know which
 * activities/insights we have already put on the queue, and thereby we will not send them twice to any queue consumers.
 */
public class ProducedEventQueueActivity {
    private UUID userId;

    private String activityKey;

    private Date generated;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getActivityKey() {
        return activityKey;
    }

    public void setActivityKey(String activityKey) {
        this.activityKey = activityKey;
    }

    public Date getGenerated() {
        return generated;
    }

    public void setGenerated(Date generated) {
        this.generated = generated;
    }
}
