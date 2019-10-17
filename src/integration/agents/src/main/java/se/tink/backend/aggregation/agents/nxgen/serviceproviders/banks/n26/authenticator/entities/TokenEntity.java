package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenEntity {
    private String accessToken;
    // TODO: Figure out the use case of refresh token
    private String refreshToken;
    private long expireDate;

    public TokenEntity() {}

    public TokenEntity(String accessToken, String refreshToken, LocalDateTime expireDate) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expireDate = expireDate.toEpochSecond(ZoneOffset.UTC);
    }

    public long getExpireDate() {
        return expireDate;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    @JsonIgnore
    public LocalDateTime getExpireDateTime() {
        return LocalDateTime.ofEpochSecond(expireDate, 0, ZoneOffset.UTC);
    }

    @JsonIgnore
    public boolean isValid() {
        return !Strings.isNullOrEmpty(accessToken)
                && !Strings.isNullOrEmpty(refreshToken)
                && LocalDateTime.now().isBefore(getExpireDateTime());
    }
}
