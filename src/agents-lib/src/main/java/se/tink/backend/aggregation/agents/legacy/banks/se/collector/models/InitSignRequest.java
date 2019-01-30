package se.tink.backend.aggregation.agents.banks.se.collector.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class InitSignRequest extends InitBankIdRequest {
    @JsonProperty("user_visible_data")
    private final String message;
    @JsonProperty("user_nonvisible_data")
    private final String secretMessage = "string";

    public InitSignRequest(String username, String message) {
        super(username);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getSecretMessage() {
        return secretMessage;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("personalNumber", getSsn())
                .add("userVisibleData", message)
                .add("userNonVisibleDate", secretMessage)
                .toString();
    }
}
