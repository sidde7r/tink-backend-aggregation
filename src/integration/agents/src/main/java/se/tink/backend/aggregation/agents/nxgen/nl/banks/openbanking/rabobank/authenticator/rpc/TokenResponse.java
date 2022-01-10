package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.StorageKey;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.cryptography.hash.Hash;

@Slf4j
@JsonObject
public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private String expiresIn;

    private String metadata;

    @JsonProperty("token_type")
    private String tokenType;

    private String scope;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("refresh_token_expires_in")
    private String refreshTokenExpiresIn;

    @JsonIgnore
    public String getConsentId() {
        return metadata.replace("a:consentId", "").trim();
    }

    public String getScope() {
        return scope;
    }

    public OAuth2Token toOauthToken(final PersistentStorage persistentStorage) {
        final String refreshTokenExpiryDate = getRefreshTokenExpireDate(getRefreshTokenExpiresIn());
        log.info(
                "New Refresh Token: {}, Expires on: {}",
                Hash.sha256AsHex(refreshToken),
                refreshTokenExpiryDate);
        persistentStorage.put(StorageKey.TOKEN_EXPIRY_DATE, refreshTokenExpiryDate);
        // Not using refreshTokenExpiresIn: the bank response for 30 days of token expiry
        return OAuth2Token.create(tokenType, accessToken, refreshToken, getExpiresIn());
    }

    private long getExpiresIn() {
        return Long.parseLong(expiresIn);
    }

    private long getRefreshTokenExpiresIn() {
        return Long.parseLong(refreshTokenExpiresIn);
    }

    private String getRefreshTokenExpireDate(final Long refreshTokenExpiresInSeconds) {
        return Instant.ofEpochMilli(System.currentTimeMillis())
                .atZone(ZoneId.systemDefault())
                .plusSeconds(refreshTokenExpiresInSeconds)
                .toLocalDate()
                .toString();
    }
}
