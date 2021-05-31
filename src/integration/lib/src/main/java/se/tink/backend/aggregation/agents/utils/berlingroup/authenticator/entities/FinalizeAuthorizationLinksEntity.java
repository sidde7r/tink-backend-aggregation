package se.tink.backend.aggregation.agents.utils.berlingroup.authenticator.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinalizeAuthorizationLinksEntity {

    private Href scaStatus;

    public Href getScaStatus() {
        return scaStatus;
    }
}
