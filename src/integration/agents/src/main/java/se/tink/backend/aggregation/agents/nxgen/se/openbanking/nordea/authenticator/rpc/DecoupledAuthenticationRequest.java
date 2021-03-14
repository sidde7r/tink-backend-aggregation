package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeConstants.Authentication;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DecoupledAuthenticationRequest {
    private String authenticationMethod = Authentication.AUTHENTICATION_METHOD;
    private String country = Authentication.COUNTRY;
    private String psuId;
    private String responseType = Authentication.RESPONSE_TYPE;

    public DecoupledAuthenticationRequest(String psuId) {
        this.psuId = psuId;
    }
}
