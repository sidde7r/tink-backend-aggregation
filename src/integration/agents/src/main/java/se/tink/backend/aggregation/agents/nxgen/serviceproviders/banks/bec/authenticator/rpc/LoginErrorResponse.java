package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class LoginErrorResponse {
    private String action;
    private String message;
}
