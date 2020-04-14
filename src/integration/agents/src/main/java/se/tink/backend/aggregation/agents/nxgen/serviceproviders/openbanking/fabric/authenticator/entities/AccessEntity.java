package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {
    private String allPsd2;

    public AccessEntity() {
        allPsd2 = FabricConstants.Consent.ALL_ACCOUNTS;
    }
}
