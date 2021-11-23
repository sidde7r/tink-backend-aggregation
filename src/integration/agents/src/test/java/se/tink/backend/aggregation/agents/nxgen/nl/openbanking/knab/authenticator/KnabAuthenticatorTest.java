package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RunWith(MockitoJUnitRunner.class)
public class KnabAuthenticatorTest {

    private final String authenticationState = "00000000-0000-5000-0000-000000000000";

    private final OAuth2Token applicationAccessToken = applicationAccessToken();

    private final OAuth2Token refreshedAccessToken =
            accessToken("another-access-token", "another-refresh-token");

    private final OAuth2Token accessToken = accessToken("some-access-token", "some-refresh-token");

    private final String refreshToken =
            accessToken
                    .getRefreshToken()
                    .orElseThrow(
                            () ->
                                    new IllegalStateException(
                                            "refresh token is needed in some tests"));

    private final String accessTokenCode = "some-access-token-code";

    private final String consentId = "00000000-0000-4000-0000-000000000000";

    private final URL anonymousConsentApprovalUrl = URL.of("https://login.knab.nl.fake/authorize");

    @Mock private KnabApiClient apiClient;

    @Mock private KnabStorage storage;

    private KnabAuthenticator authenticator;

    @Before
    public void setUp() {
        authenticator =
                new KnabAuthenticator(
                        new StrongAuthenticationState(authenticationState), apiClient, storage);

        when(apiClient.applicationAccessToken()).thenReturn(applicationAccessToken);
        when(apiClient.anonymousConsent(applicationAccessToken)).thenReturn(consentId);
        when(apiClient.anonymousConsentApprovalUrl(scope(), authenticationState))
                .thenReturn(anonymousConsentApprovalUrl);
        when(apiClient.accessToken(accessTokenCode, authenticationState)).thenReturn(accessToken);
        when(apiClient.refreshToken(refreshToken)).thenReturn(refreshedAccessToken);
        when(apiClient.consentStatus(consentId, accessToken)).thenReturn(true);

        when(storage.findConsentId()).thenReturn(Optional.of(consentId));
    }

    @Test
    public void shouldReturnAuthorizationUrlAndRequestForAnonymousConsentBefore() {
        // when
        URL authorizationUrl = authenticator.buildAuthorizeUrl(authenticationState);

        // then
        InOrder inOrder = inOrder(apiClient, storage);

        // and
        then(apiClient).should(inOrder).applicationAccessToken();
        then(apiClient).should(inOrder).anonymousConsent(applicationAccessToken);
        then(storage).should(inOrder).persistConsentId(consentId);
        then(apiClient).should(inOrder).anonymousConsentApprovalUrl(scope(), authenticationState);

        // and
        inOrder.verifyNoMoreInteractions();

        // and
        assertThat(authorizationUrl).isEqualTo(anonymousConsentApprovalUrl);
    }

    @Test
    public void shouldReturnAccessToken() {
        // when
        OAuth2Token accessToken = authenticator.exchangeAuthorizationCode(accessTokenCode);

        // then
        then(apiClient).should().accessToken(accessTokenCode, authenticationState);

        // and
        assertThat(accessToken).usingRecursiveComparison().isEqualTo(accessToken);
    }

    @Test
    public void shouldReturnAccessTokenAfterRefresh() {
        // when
        OAuth2Token newAccessToken = authenticator.refreshAccessToken(refreshToken);

        // then
        then(apiClient).should().refreshToken(refreshToken);

        // and
        then(storage).should().persistBearerToken(newAccessToken);

        // and
        assertThat(newAccessToken).usingRecursiveComparison().isEqualTo(refreshedAccessToken);
    }

    @Test
    public void shouldPersistAccessToken() {
        // when
        authenticator.useAccessToken(accessToken);

        // then
        then(apiClient).should().consentStatus(consentId, accessToken);

        // and
        then(storage).should().persistBearerToken(accessToken);
    }

    @Test
    public void shouldNotPersistAccessTokenWithoutConsent() {
        // given
        given(storage.findConsentId()).willReturn(Optional.empty());

        // when
        authenticator.useAccessToken(accessToken);

        // then
        then(storage).should().findConsentId();

        // and
        then(storage).shouldHaveNoMoreInteractions();
    }

    @Test
    public void shouldNotPersistAccessTokenWithoutValidConsent() {
        // given
        given(apiClient.consentStatus(consentId, accessToken)).willReturn(false);

        // when
        authenticator.useAccessToken(accessToken);

        // then
        then(storage).should().findConsentId();

        // and
        then(apiClient).should().consentStatus(consentId, accessToken);

        // and
        then(storage).shouldHaveNoMoreInteractions();
    }

    private String scope() {
        return String.format("psd2 offline_access AIS:%s", consentId);
    }

    private OAuth2Token accessToken(String accessToken, String refreshToken) {
        return OAuth2Token.createBearer(accessToken, refreshToken, 30);
    }

    private OAuth2Token applicationAccessToken() {
        return accessToken("some-application-access-token", null);
    }
}
