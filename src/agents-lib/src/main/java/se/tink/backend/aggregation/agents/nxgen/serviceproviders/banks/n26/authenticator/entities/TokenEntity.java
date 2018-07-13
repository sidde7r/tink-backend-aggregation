package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.entities;

import com.google.common.base.Strings;
import java.util.Date;

public class TokenEntity {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;

    public TokenEntity(){}

    public TokenEntity(String accessToken, String refreshToken, long expiresIn){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public boolean isValid() {
        return new Date().before(new Date(expiresIn)) && !Strings.isNullOrEmpty(accessToken);
    }
}
