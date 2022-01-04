package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc;

import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
public class LoginVMRequest {
    private Boolean resendChallange;
    private String token;
}
