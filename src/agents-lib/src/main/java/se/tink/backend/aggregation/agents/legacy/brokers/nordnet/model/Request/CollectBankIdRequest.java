package se.tink.backend.aggregation.agents.brokers.nordnet.model.Request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CollectBankIdRequest {
    @JsonProperty("orderRef")
    private final String ticket;

    public CollectBankIdRequest(String ticket) {
        this.ticket = ticket;
    }

    public String getTicket() {
        return ticket;
    }
}
