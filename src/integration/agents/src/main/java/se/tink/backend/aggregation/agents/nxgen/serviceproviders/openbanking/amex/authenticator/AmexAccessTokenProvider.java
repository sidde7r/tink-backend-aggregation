package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.authenticator;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.TokenResponseDto;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenProvider;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;

@RequiredArgsConstructor
public class AmexAccessTokenProvider implements AccessTokenProvider<HmacToken> {

    private static final Logger logger = LoggerFactory.getLogger(AmexAccessTokenProvider.class);
    private final AmexApiClient amexApiClient;

    @Override
    public HmacToken exchangeAuthorizationCode(String code) {
        final TokenResponseDto tokenResponse = amexApiClient.retrieveAccessToken(code);

        return convertResponseToHmacToken(tokenResponse);
    }

    @Override
    public Optional<HmacToken> refreshAccessToken(String refreshToken) {
        logger.info("Refresh Token from Storage: {}", Hash.sha256AsHex(refreshToken));
        return amexApiClient
                .refreshAccessToken(refreshToken)
                .map(AmexAccessTokenProvider::convertResponseToHmacToken);
    }

    private static HmacToken convertResponseToHmacToken(TokenResponseDto response) {
        logger.info("New Refresh Token: {}", Hash.sha256AsHex(response.getRefreshToken()));
        return new HmacToken(
                response.getTokenType(),
                response.getAccessToken(),
                response.getRefreshToken(),
                response.getMacKey(),
                response.getExpiresIn());
    }
}
