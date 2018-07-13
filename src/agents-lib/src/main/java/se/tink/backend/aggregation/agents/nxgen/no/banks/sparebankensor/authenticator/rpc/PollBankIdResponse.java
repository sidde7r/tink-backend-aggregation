package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PollBankIdResponse {
    private String status;

    public String getStatus() {
        return status;
    }
}
