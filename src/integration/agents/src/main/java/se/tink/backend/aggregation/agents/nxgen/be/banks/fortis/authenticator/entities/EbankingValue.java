package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EbankingValue {
    private String authenticationProcessId;

    public String getAuthenticationProcessId() {
        return authenticationProcessId;
    }
}
