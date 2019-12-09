package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorisationLinksEntity {
    private String scaRedirect;
    private String scaStatus;

    public String getScaRedirect() {
        return scaRedirect;
    }
}
