package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.rpc;

import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Setter
public class TokenResponse {

    private TokenRequest tokenRequest;
}
