package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class DecoupledAuthorizationResponse {
    private String code;
}
