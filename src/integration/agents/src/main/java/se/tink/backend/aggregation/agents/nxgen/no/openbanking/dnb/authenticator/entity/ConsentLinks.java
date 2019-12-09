package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentLinks {
    private SCARedirect scaRedirect;

    public SCARedirect getScaRedirect() {
        return scaRedirect;
    }
}
