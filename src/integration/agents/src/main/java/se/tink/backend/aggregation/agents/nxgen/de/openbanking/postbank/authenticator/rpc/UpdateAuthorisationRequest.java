package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateAuthorisationRequest {

    private String scaAuthenticationData;

    private String authenticationMethodId;

    public UpdateAuthorisationRequest(String scaAuthenticationData, String authenticationMethodId) {
        this.scaAuthenticationData = scaAuthenticationData;
        this.authenticationMethodId = authenticationMethodId;
    }
}
