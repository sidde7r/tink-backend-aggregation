package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AutorizationConsentRequest {
    private String scaAuthenticationData;

    public AutorizationConsentRequest() {}

    public AutorizationConsentRequest(String scaAuthenticationData) {
        this.scaAuthenticationData = scaAuthenticationData;
    }
}
