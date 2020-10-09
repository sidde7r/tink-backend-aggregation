package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.a2a.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class CollectTicketResponse {
    private String status;
    private String token;
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
    private Integer refreshExpiresIn;

    public String getStatus() {
        return status;
    }

    public String getToken() {
        return token;
    }

    public OAuth2Token getOAuthToken() {
        return Optional.ofNullable(accessToken)
                .map(
                        at ->
                                OAuth2Token.create(
                                        "bearer",
                                        accessToken,
                                        refreshToken,
                                        expiresIn,
                                        refreshExpiresIn))
                .orElse(null);
    }
}
