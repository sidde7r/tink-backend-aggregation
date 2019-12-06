package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entity.consent;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {
    private String href;

    public String getUrl() {
        return href;
    }
}
