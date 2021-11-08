package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class LoginRequest {
    private final String authenticationMethodId;
}
