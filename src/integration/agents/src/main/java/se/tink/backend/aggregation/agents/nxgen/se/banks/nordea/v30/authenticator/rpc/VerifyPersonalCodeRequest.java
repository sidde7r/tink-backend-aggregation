package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class VerifyPersonalCodeRequest {
    @JsonProperty("personal_code")
    private String personalCode;

    @JsonProperty("authorization_code")
    private String authorizationCode;

    public static VerifyPersonalCodeRequest create(String personalCode, String authorizationCode) {
        VerifyPersonalCodeRequest verifyPersonalCodeRequest = new VerifyPersonalCodeRequest();
        verifyPersonalCodeRequest.authorizationCode = authorizationCode;
        verifyPersonalCodeRequest.personalCode = personalCode;
        return verifyPersonalCodeRequest;
    }
}
