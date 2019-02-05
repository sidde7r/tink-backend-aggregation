package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentityEntity {
    private String id;
    private String name;
    private String maskedNumber;
    private boolean mainCard;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMaskedNumber() {
        return maskedNumber;
    }

    public boolean isMainCard() {
        return mainCard;
    }
}
