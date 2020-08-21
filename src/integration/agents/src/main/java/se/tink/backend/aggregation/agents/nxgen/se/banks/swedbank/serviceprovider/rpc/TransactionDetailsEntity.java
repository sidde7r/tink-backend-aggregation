package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionDetailsEntity {
    private String reference;
    private String message;

    public String getReference() {
        return reference;
    }

    public String getMessage() {
        return message;
    }
}
