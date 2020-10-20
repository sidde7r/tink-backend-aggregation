package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class UpdateAuthorisationRequest {

    private String scaAuthenticationData;
    private String authenticationMethodId;
}
