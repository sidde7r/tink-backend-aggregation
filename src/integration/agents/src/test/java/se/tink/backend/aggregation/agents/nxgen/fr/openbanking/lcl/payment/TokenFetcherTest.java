package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclTokenApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken.TokenResponseDto;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class TokenFetcherTest {

    @Test
    public void shouldFetchTokenIfInvalid() {

        // given:
        LclTokenApiClient tokenApiClient = mock(LclTokenApiClient.class);
        SessionStorage sessionStorage = mock(SessionStorage.class);
        TokenFetcher tokenFetcher = new TokenFetcher(tokenApiClient, sessionStorage);
        Optional<OAuth2Token> tokenOptional = Optional.empty();
        given(sessionStorage.get(eq("pis_token"), eq(OAuth2Token.class))).willReturn(tokenOptional);
        TokenResponseDto tokenResponseDto = mock(TokenResponseDto.class);
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);
        given(tokenResponseDto.toOauthToken()).willReturn(oAuth2Token);
        given(tokenApiClient.getPispToken()).willReturn(tokenResponseDto);

        // when:
        tokenFetcher.fetchToken();

        // then:
        then(sessionStorage).should().get(eq("pis_token"), eq(OAuth2Token.class));
        then(tokenApiClient).should().getPispToken();
        then(tokenResponseDto).should().toOauthToken();
        then(sessionStorage).should().put("pis_token", oAuth2Token);
    }

    @Test
    public void shouldNotFetchTokenIsValid() {

        // given:
        LclTokenApiClient tokenApiClient = mock(LclTokenApiClient.class);
        SessionStorage sessionStorage = mock(SessionStorage.class);
        TokenFetcher tokenFetcher = new TokenFetcher(tokenApiClient, sessionStorage);
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);
        given(oAuth2Token.isValid()).willReturn(true);
        Optional<OAuth2Token> tokenOptional = Optional.of(oAuth2Token);
        given(sessionStorage.get(eq("pis_token"), eq(OAuth2Token.class))).willReturn(tokenOptional);

        // when:
        tokenFetcher.fetchToken();

        // then:
        then(sessionStorage).should().get(eq("pis_token"), eq(OAuth2Token.class));
        then(tokenApiClient).shouldHaveNoInteractions();
    }

    @Test
    public void shouldReuseToken() {

        // given:
        LclTokenApiClient tokenApiClient = mock(LclTokenApiClient.class);
        SessionStorage sessionStorage = mock(SessionStorage.class);
        TokenFetcher tokenFetcher = new TokenFetcher(tokenApiClient, sessionStorage);
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);
        given(oAuth2Token.canUseAccessToken()).willReturn(true);
        Optional<OAuth2Token> tokenOptional = Optional.of(oAuth2Token);
        given(sessionStorage.get(eq("pis_token"), eq(OAuth2Token.class))).willReturn(tokenOptional);

        // when:
        OAuth2Token actualToken = tokenFetcher.reuseTokenOrRefetch();

        // then:
        assertThat(actualToken).isEqualTo(oAuth2Token);
        then(sessionStorage).should().get(eq("pis_token"), eq(OAuth2Token.class));
        then(tokenApiClient).shouldHaveNoInteractions();
    }

    @Test
    public void shouldRefetchTokenIfCannotBeUsed() {

        // given:
        LclTokenApiClient tokenApiClient = mock(LclTokenApiClient.class);
        SessionStorage sessionStorage = mock(SessionStorage.class);
        TokenFetcher tokenFetcher = new TokenFetcher(tokenApiClient, sessionStorage);
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);
        given(oAuth2Token.canUseAccessToken()).willReturn(false);
        Optional<OAuth2Token> tokenOptional = Optional.of(oAuth2Token);
        given(sessionStorage.get(eq("pis_token"), eq(OAuth2Token.class))).willReturn(tokenOptional);
        TokenResponseDto tokenResponseDto = mock(TokenResponseDto.class);
        given(tokenResponseDto.toOauthToken()).willReturn(oAuth2Token);
        given(tokenApiClient.getPispToken()).willReturn(tokenResponseDto);

        // when:
        OAuth2Token actualToken = tokenFetcher.reuseTokenOrRefetch();

        // then:
        assertThat(actualToken).isEqualTo(oAuth2Token);
        then(sessionStorage).should().get(eq("pis_token"), eq(OAuth2Token.class));
        then(tokenApiClient).should().getPispToken();
        then(tokenResponseDto).should().toOauthToken();
    }
}
