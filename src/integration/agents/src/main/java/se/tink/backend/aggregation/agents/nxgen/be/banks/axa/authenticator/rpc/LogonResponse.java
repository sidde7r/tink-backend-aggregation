package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.ClientInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.Optional;

@JsonObject
public final class LogonResponse {
    private String access_token;
    private String applicationEnvStatusCd;
    private String applicationStatusCd;
    private ClientInfoEntity clientInfo;
    private String degradedOperationsParam;
    private Integer expires_in;
    private String refresh_token;
    private String scope;
    private String subscriptionProfile;
    private String token_type;

    public Optional<String> getAccessToken() {
        return Optional.ofNullable(access_token);
    }

    public Optional<String> getCustomerId() {
        return Optional.ofNullable(clientInfo)
                .map(ClientInfoEntity::getCustomerId);
    }
}
