package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MandatesEntity {
    private String displayName;
    private String mandateType;

    public String getDisplayName() {
        return displayName;
    }

    public String getMandateType() {
        return mandateType;
    }
}
