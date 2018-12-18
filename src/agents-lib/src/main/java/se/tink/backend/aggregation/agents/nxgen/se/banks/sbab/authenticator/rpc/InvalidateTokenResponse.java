package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvalidateTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("was_valid_to")
    private long wasValidTo;

    @JsonProperty("invalidated_at")
    private long invalidatedAt;

    @JsonProperty("invalidate_reason")
    private String invalidateReason;

    public String getAccessToken() {
        return accessToken;
    }

    public long getWasValidTo() {
        return wasValidTo;
    }

    public long getInvalidatedAt() {
        return invalidatedAt;
    }

    public String getInvalidateReason() {
        return invalidateReason;
    }
}
