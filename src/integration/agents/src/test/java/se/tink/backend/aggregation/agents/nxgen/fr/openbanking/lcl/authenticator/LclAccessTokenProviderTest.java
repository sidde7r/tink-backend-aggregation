package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.AUTH_CODE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.REFRESH_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createOAuth2Token;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createTokenResponseDto;

import java.util.Optional;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclTokenApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken.TokenResponseDto;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class LclAccessTokenProviderTest {

    private LclAccessTokenProvider lclAccessTokenProvider;

    private LclTokenApiClient apiClientMock;

    @Before
    public void setUp() {
        apiClientMock = mock(LclTokenApiClient.class);
        lclAccessTokenProvider = new LclAccessTokenProvider(apiClientMock);
    }

    @Test
    public void shouldExchangeAuthorizationCode() {
        // given
        final OAuth2Token expectedResult = createOAuth2Token();
        // and
        final TokenResponseDto tokenResponseDto = createTokenResponseDto();
        when(apiClientMock.retrieveAccessToken(AUTH_CODE)).thenReturn(tokenResponseDto);

        // when
        final OAuth2Token returnedResult =
                lclAccessTokenProvider.exchangeAuthorizationCode(AUTH_CODE);

        // then
        assertThat(returnedResult).matches(new OAuth2TokenPredicate(expectedResult));
    }

    @Test
    public void shouldRefreshAccessToken() {
        // given
        final OAuth2Token expectedResult = createOAuth2Token();
        // and
        final TokenResponseDto tokenResponseDto = createTokenResponseDto();
        when(apiClientMock.refreshAccessToken(REFRESH_TOKEN))
                .thenReturn(Optional.of(tokenResponseDto));

        // when
        final Optional<OAuth2Token> returnedResult =
                lclAccessTokenProvider.refreshAccessToken(REFRESH_TOKEN);

        // then
        assertThat(returnedResult).isPresent();
        returnedResult.ifPresent(
                actualToken ->
                        assertThat(actualToken).matches(new OAuth2TokenPredicate(expectedResult)));
    }
}

class OAuth2TokenPredicate implements Predicate<OAuth2Token> {

    private final OAuth2Token expectedToken;

    OAuth2TokenPredicate(OAuth2Token expectedToken) {
        this.expectedToken = expectedToken;
    }

    @Override
    public boolean test(OAuth2Token actualToken) {
        return expectedToken.getTokenType().equals(actualToken.getTokenType())
                && expectedToken.getAccessToken().equals(actualToken.getAccessToken())
                && expectedToken.getRefreshToken().equals(actualToken.getRefreshToken())
                && expectedToken.getExpiresInSeconds() == actualToken.getExpiresInSeconds();
    }
}
