package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PersonEntity {
    private String accountIdentifierType;
    private String accountName;
    private String accountIdentifier;
    private String servicerIdentifier;
    private String servicerIdentifierType;

    public String getAccountIdentifier() {
        return accountIdentifier;
    }

    public String getAccountIdentifierType() {
        return accountIdentifierType;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getServicerIdentifier() {
        return servicerIdentifier;
    }

    public String getServicerIdentifierType() {
        return servicerIdentifierType;
    }
}
