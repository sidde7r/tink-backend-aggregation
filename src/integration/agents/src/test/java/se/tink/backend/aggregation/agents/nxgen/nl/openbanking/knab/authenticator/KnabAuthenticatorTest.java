package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

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

    private final PersistentStorage persistentStorage = new PersistentStorage();

    private final KnabStorage storage = new KnabStorage(persistentStorage);

    @Mock private KnabApiClient apiClient;

    private KnabAuthenticator authenticator;

    @Before
    public void setUp() {
        authenticator =
                new KnabAuthenticator(
                        new StrongAuthenticationState(authenticationState), apiClient, storage);

        storage.persistConsentId(consentId);
    }

    @Test
    public void shouldReturnAuthorizationUrlAndRequestForAnonymousConsentBefore() {
        // given
        emptyStorage();

        // and
        bankAnonymousConsentFlow();

        // when
        URL authorizationUrl = authenticator.buildAuthorizeUrl(authenticationState);

        // then
        assertThat(authorizationUrl).isEqualTo(anonymousConsentApprovalUrl);

        // and
        assertThat(storage.findConsentId()).contains(consentId);
    }

    @Test
    public void shouldReturnAccessToken() {
        // given
        bankRespondsWithAccessToken();

        // when
        OAuth2Token accessToken = authenticator.exchangeAuthorizationCode(accessTokenCode);

        // then
        assertThat(accessToken).usingRecursiveComparison().isEqualTo(accessToken);
    }

    @Test
    public void shouldReturnAccessTokenAfterRefresh() {
        // given
        bankRespondsWithRefreshedAccessToken();

        // when
        OAuth2Token newAccessToken = authenticator.refreshAccessToken(refreshToken);

        // then
        assertThat(newAccessToken).usingRecursiveComparison().isEqualTo(refreshedAccessToken);

        // and
        assertThat(storage.findBearerToken()).contains(refreshedAccessToken);
    }

    @Test
    public void shouldPersistAccessToken() {
        // given
        bankRespondsWithValidConsentStatus();

        // when
        authenticator.useAccessToken(accessToken);

        // then
        assertThat(storage.findBearerToken()).contains(accessToken);
    }

    @Test
    public void shouldNotPersistAccessTokenWithoutConsent() {
        // given
        noConsent();

        // when
        authenticator.useAccessToken(accessToken);

        // then
        assertThat(storage.findBearerToken()).isEmpty();
    }

    @Test
    public void shouldNotPersistAccessTokenWithoutValidConsent() {
        // given
        bankRespondsWithInvalidConsentStatus();

        // when
        authenticator.useAccessToken(accessToken);

        // then
        assertThat(storage.findBearerToken()).isEmpty();
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

    private void bankAnonymousConsentFlow() {
        when(apiClient.applicationAccessToken()).thenReturn(applicationAccessToken);
        when(apiClient.anonymousConsent(applicationAccessToken)).thenReturn(consentId);
        when(apiClient.anonymousConsentApprovalUrl(scope(), authenticationState))
                .thenReturn(anonymousConsentApprovalUrl);
    }

    private void bankRespondsWithAccessToken() {
        when(apiClient.accessToken(accessTokenCode, authenticationState)).thenReturn(accessToken);
    }

    private void bankRespondsWithRefreshedAccessToken() {
        when(apiClient.refreshToken(refreshToken)).thenReturn(refreshedAccessToken);
    }

    private void bankRespondsWithValidConsentStatus() {
        when(apiClient.consentStatus(consentId, accessToken)).thenReturn(true);
    }

    private void bankRespondsWithInvalidConsentStatus() {
        when(apiClient.consentStatus(consentId, accessToken)).thenReturn(false);
    }

    private void noConsent() {
        storage.persistConsentId(null);
    }

    private void emptyStorage() {
        persistentStorage.clear();
    }
}
