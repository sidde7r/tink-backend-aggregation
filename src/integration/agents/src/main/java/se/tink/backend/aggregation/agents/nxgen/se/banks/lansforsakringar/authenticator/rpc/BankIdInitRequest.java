package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdInitRequest {

    private final String ssn;

    private BankIdInitRequest(String ssn) {
        this.ssn = ssn;
    }

    public static BankIdInitRequest of(String ssn) {
        return new BankIdInitRequest(ssn);
    }
}
