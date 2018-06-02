package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FirehoseConfiguration {
    @JsonProperty
    private boolean consumerStartFromLatestMessage = true;

    @JsonProperty
    private int maxPollMessages = 1;

    public boolean shouldConsumerStartFromLatestMessage() {
        return consumerStartFromLatestMessage;
    }

    public int getMaxPollMessages() {
        return maxPollMessages;
    }
}
