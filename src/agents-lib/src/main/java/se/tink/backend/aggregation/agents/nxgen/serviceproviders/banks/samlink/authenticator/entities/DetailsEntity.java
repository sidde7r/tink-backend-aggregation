package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DetailsEntity {
    private Integer keyCount;
    private Boolean fixed;
    private Boolean keysUsed;

    public Integer getKeyCount() {
        return keyCount;
    }

    public Boolean getFixed() {
        return fixed;
    }

    public Boolean getKeysUsed() {
        return keysUsed;
    }
}
