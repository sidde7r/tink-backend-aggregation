package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardSummaryEntity {
    private IdentityEntity identity;
    private String validUntil;

    public IdentityEntity getIdentity() {
        return identity;
    }

    public String getValidUntil() {
        return validUntil;
    }
}
