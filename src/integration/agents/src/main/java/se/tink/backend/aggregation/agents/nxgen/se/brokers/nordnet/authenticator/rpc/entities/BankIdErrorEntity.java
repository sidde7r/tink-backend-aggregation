package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdErrorEntity {

    private String code;

    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
