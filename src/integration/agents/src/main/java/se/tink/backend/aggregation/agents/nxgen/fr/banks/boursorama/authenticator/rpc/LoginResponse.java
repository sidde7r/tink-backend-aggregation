package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity.ErrorResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoginResponse extends ErrorResponse {

    private String deviceEnrolmentTokenValue;
}
