package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeConstants.BodyValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DecoupledAuthenticationRequest {
    private String authenticationMethod = BodyValues.AUTHENTICATION_METHOD;
    private String country = BodyValues.COUNTRY;
    private String psuId;
    private String responseType = BodyValues.CODE;

    public DecoupledAuthenticationRequest(String psuId) {
        this.psuId = psuId;
    }
}
