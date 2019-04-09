package se.tink.backend.aggregation.agents.brokers.nordnet.model.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import se.tink.libraries.uuid.UUIDUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String type;

    @JsonProperty("validation_token")
    private String validationToken;

    @JsonProperty("expires_in")
    private int expiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValidationToken() {
        return validationToken;
    }

    public void setValidationToken(String validationToken) {
        this.validationToken = validationToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("access_token", stringifyToken(accessToken))
                .add("validation_token", stringifyToken(validationToken))
                .add("token_type", type)
                .add("expires_in", expiresIn)
                .toString();
    }

    private String stringifyToken(String token) {
        return String.format("validUUID(%s)", UUIDUtils.isValidUUIDv4(token));
    }
}
