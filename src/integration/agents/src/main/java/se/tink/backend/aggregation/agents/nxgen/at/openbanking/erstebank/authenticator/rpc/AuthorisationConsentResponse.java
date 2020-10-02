package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorisationConsentResponse {
    private String authorisationId;
    private String scaStatus;
}
