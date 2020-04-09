package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private LinkEntity updatePsuAuthentication;
    private LinkEntity authoriseTransaction;
    private LinkEntity scaOAuth;
    private LinkEntity scaRedirect;

    public String getScaRedirect() {
        return scaRedirect.getLink();
    }
}
