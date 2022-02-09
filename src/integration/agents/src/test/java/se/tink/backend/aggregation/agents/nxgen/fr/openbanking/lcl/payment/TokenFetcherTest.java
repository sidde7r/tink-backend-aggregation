package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclTokenApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken.TokenResponseDto;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RunWith(MockitoJUnitRunner.class)
public class TokenFetcherTest {

    @Mock private LclTokenApiClient tokenApiClient;
    @Mock private SessionStorage sessionStorage;
    @Mock private TokenResponseDto tokenResponseDto;
    @Mock private OAuth2Token oAuth2Token;
    private TokenFetcher tokenFetcher;

    @Before
    public void setUp() {
        tokenFetcher = new TokenFetcher(tokenApiClient, sessionStorage);
    }

    @Test
    public void shouldFetchTokenIfInvalid() {

        // given:
        Optional<OAuth2Token> tokenOptional = Optional.empty();
        given(sessionStorage.get(eq("pis_token"), eq(OAuth2Token.class))).willReturn(tokenOptional);
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
        given(oAuth2Token.canUseAccessToken()).willReturn(false);
        Optional<OAuth2Token> tokenOptional = Optional.of(oAuth2Token);
        given(sessionStorage.get(eq("pis_token"), eq(OAuth2Token.class))).willReturn(tokenOptional);
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
