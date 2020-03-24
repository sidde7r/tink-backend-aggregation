package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.ClientInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class LogonResponse {

    @JsonProperty("access_token")
    private String accessToken;

    private String applicationEnvStatusCd;
    private String applicationStatusCd;
    private ClientInfoEntity clientInfo;
    private String degradedOperationsParam;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    private String scope;
    private String subscriptionProfile;

    @JsonProperty("token_type")
    private String tokenType;

    public Optional<String> getAccessToken() {
        return Optional.ofNullable(accessToken);
    }

    public Optional<String> getCustomerId() {
        return Optional.ofNullable(clientInfo).map(ClientInfoEntity::getCustomerId);
    }
}
