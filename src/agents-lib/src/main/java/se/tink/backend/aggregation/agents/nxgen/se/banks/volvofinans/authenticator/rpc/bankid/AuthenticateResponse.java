package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.authenticator.rpc.bankid;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticateResponse extends AbstractBankIdResponse {

    private String bearerToken;
    @JsonProperty("identifieringsId")
    private String identificationId;

    public String getBearerToken() {
        return bearerToken;
    }

    public String getIdentificationId() {
        return identificationId;
    }

}
