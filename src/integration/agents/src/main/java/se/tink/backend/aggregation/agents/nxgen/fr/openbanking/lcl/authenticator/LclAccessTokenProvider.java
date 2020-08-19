package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.authenticator;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken.TokenResponseDto;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenProvider;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@RequiredArgsConstructor
public class LclAccessTokenProvider implements AccessTokenProvider<OAuth2Token> {

    private final LclApiClient apiClient;

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        final TokenResponseDto tokenResponse = apiClient.retrieveAccessToken(code);

        return convertResponseToOauth2Token(tokenResponse);
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        return apiClient
                .refreshAccessToken(refreshToken)
                .map(LclAccessTokenProvider::convertResponseToOauth2Token);
    }

    private static OAuth2Token convertResponseToOauth2Token(TokenResponseDto response) {
        return OAuth2Token.create(
                response.getTokenType(),
                response.getAccessToken(),
                response.getRefreshToken(),
                response.getExpiresIn());
    }
}
