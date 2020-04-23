package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.authenticator.bankid.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PollBankIdResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private String expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("signature_reference")
    private String signatureReference;

    @JsonProperty("token_type")
    private String tokenType;

    private String status;

    public String getAccessToken() {
        return accessToken;
    }

    public Long getExpiresIn() {
        return Long.parseLong(expiresIn);
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getSignatureReference() {
        return signatureReference;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getStatus() {
        return status;
    }

    public String getBearerToken() {
        return tokenType + " " + accessToken;
    }
}
