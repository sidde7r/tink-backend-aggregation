package se.tink.backend.aggregation.agents.banks.sbab.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PollBankIdResponse {
    private String status;
    private String message;

    public String getStatus() {
        return status;
    }
}
