package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinalizeAuthorizationLinksEntity {

    private Href scaStatus;

    public Href getScaStatus() {
        return scaStatus;
    }
}
