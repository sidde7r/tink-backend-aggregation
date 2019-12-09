package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SCARedirect {
    private String href;

    public String getHref() {
        return href;
    }
}
