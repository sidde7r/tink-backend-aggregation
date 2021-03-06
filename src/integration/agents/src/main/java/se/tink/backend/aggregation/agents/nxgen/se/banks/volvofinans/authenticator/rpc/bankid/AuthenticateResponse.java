package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.authenticator.rpc.bankid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class AuthenticateResponse extends AbstractBankIdResponse {

    private String bearerToken;

    @JsonProperty("identifieringsId")
    private String identificationId;
}
