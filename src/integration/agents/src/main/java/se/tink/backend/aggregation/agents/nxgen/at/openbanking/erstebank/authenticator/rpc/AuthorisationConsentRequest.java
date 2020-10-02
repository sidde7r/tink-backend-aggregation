package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorisationConsentRequest {

    private String scaAuthenticationData;

    public AuthorisationConsentRequest(String scaAuthenticationData) {
        this.scaAuthenticationData = scaAuthenticationData;
    }

    public AuthorisationConsentRequest() {}
}
