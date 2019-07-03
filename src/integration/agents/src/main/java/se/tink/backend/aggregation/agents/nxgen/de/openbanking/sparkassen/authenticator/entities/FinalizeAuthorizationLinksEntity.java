package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinalizeAuthorizationLinksEntity {

    private Href scaStatus;

    public Href getScaStatus() {
        return scaStatus;
    }
}
