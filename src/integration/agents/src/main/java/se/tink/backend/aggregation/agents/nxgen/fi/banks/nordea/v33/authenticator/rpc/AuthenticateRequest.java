package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@ToString
@AllArgsConstructor
@Builder
@Getter
public class AuthenticateRequest {
    private String redirectUri;
    private String scope;
    private String clientId;
    private String userId;
    private String codeChallenge;
    private String codeChallengeMethod;
    private String responseType;
}
