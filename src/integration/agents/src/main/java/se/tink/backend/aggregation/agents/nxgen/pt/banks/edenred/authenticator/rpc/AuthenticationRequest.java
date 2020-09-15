package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.authenticator.rpc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthenticationRequest {

    private final String userId;
    private final String password;
    private final boolean rememberMe;
    private final String appType;
    private final String channel;
    private final String appVersion;
}
