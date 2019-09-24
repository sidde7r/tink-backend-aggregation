package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.entities;

import com.google.common.base.Strings;
import java.time.LocalDateTime;

public class TokenEntity {
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expireDate;

    public TokenEntity() {}

    public TokenEntity(String accessToken, String refreshToken, LocalDateTime expireDate) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expireDate = expireDate;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public LocalDateTime getExpireDate() {
        return expireDate;
    }

    public boolean isValid() {
        return !Strings.isNullOrEmpty(accessToken)
                && !Strings.isNullOrEmpty(refreshToken)
                && LocalDateTime.now().isBefore(expireDate);
    }
}
