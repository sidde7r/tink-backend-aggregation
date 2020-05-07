package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.authenticator;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.TokenResponseDto;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenProvider;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;

@RequiredArgsConstructor
public class AmexAccessTokenProvider implements AccessTokenProvider<HmacToken> {

    private final AmexApiClient amexApiClient;

    @Override
    public HmacToken exchangeAuthorizationCode(String code) {
        final TokenResponseDto tokenResponse = amexApiClient.retrieveAccessToken(code);

        return convertResponseToHmacToken(tokenResponse);
    }

    @Override
    public Optional<HmacToken> refreshAccessToken(String refreshToken) {
        return amexApiClient
                .refreshAccessToken(refreshToken)
                .map(AmexAccessTokenProvider::convertResponseToHmacToken);
    }

    private static HmacToken convertResponseToHmacToken(TokenResponseDto response) {
        return new HmacToken(
                response.getTokenType(),
                response.getAccessToken(),
                response.getRefreshToken(),
                response.getMacKey(),
                response.getExpiresIn());
    }
}
