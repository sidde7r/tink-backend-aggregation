package se.tink.backend.aggregation.agents.creditcards.supremecard.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LogRequest {
    @JsonProperty("event")
    private String event;

    public LogRequest(String event) {
        this.event = event;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
