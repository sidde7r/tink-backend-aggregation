package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PollBankIdResponse {
    private String status;
    private String message;

    public String getStatus() {
        return status;
    }
}
