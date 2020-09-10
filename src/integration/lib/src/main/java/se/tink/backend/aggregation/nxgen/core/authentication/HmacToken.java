package se.tink.backend.aggregation.nxgen.core.authentication;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class HmacToken extends OAuth2TokenBase {

    public static final String MAC_TOKEN_TYPE = "mac";

    private String macKey;

    public HmacToken(
            String tokenType,
            String accessToken,
            String refreshToken,
            String macKey,
            long expiresInSeconds) {
        super(
                tokenType,
                accessToken,
                refreshToken,
                null,
                expiresInSeconds,
                OAuth2TokenBase.REFRESH_TOKEN_EXPIRES_NOT_SPECIFIED,
                getCurrentEpoch());
        this.macKey = macKey;
    }

    @Override
    public boolean isTokenTypeValid() {
        return MAC_TOKEN_TYPE.equalsIgnoreCase(getTokenType());
    }
}
