package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SdcCreditCardAccountEntity {
    private String id;
    private SdcAccountKey entityKey;
    private String localizedAccountId;
    private String type;
    private String currency;
    private String name;

    public String getId() {
        return id;
    }

    public SdcAccountKey getEntityKey() {
        return entityKey != null ? entityKey : new SdcAccountKey();
    }

    public String getLocalizedAccountId() {
        return localizedAccountId;
    }

    public String getType() {
        return type;
    }

    public String getCurrency() {
        return currency;
    }

    public String getName() {
        return name;
    }
}
