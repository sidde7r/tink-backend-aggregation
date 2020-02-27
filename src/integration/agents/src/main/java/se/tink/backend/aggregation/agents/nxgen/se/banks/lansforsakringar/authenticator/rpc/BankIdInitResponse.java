package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdInitResponse {
    private String reference;

    public String getReference() {
        return reference;
    }
}
