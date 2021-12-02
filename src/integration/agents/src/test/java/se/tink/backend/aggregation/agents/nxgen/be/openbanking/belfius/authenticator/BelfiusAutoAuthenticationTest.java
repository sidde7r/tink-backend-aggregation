package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator;

import static com.google.common.collect.Maps.newHashMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.filter.BelfiusClientConfigurator;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.MockSupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;

@RunWith(JUnitParamsRunner.class)
public class BelfiusAutoAuthenticationTest {

    private static final OAuth2Token VALID_ACCESS_TOKEN =
            OAuth2Token.create("oauth2", "belfiusAccessToken", "belfiusRefreshToken", 98765);

    private Authenticator authenticationController;
    private Credentials credentials;
    private OAuth2AuthenticationController oAuth2AuthenticationController;
    private PersistentStorage persistentStorage;
    private BelfiusTestFixture belfiusTestFixture;
    @Mock private Filter executionFilter;
    @Mock private HttpResponse response;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        belfiusTestFixture = new BelfiusTestFixture();
        BelfiusApiClient belfiusApiClient = belfiusApiClient(tinkHttpClient());
        persistentStorage = new PersistentStorage();
        CredentialsRequest credentialsRequest = credentialsRequest();
        credentials = credentialsRequest.getCredentials();
        SupplementalInformationHelper supplementalInformationHelper =
                new MockSupplementalInformationHelper(newHashMap());
        oAuth2AuthenticationController =
                oAuth2AuthenticationController(supplementalInformationHelper, belfiusApiClient);
        ThirdPartyAppAuthenticationController<String> thirdPartyAppAuthenticationController =
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController, supplementalInformationHelper);
        authenticationController =
                autoAuthenticationController(
                        credentialsRequest, thirdPartyAppAuthenticationController);
        setDefaultBankResponses();
    }

    @Test
    public void shouldAuthenticateUserWhenAccessTokenIsValid() {
        // given
        userHasValidAccessToken();

        // when
        authenticationController.authenticate(credentials);

        // then
        assertThatUserIsAuthenticated();
    }

    @Test
    public void shouldAuthenticateUserWhenAccessTokenIsExpiredButRefreshTokenExists() {
        // given
        userHasExpiredAccessToken();

        // and
        bankReturnsOauth2TokenBasedOnRefreshToken();

        // when
        authenticationController.authenticate(credentials);

        // then
        assertThatUserIsAuthenticated();
    }

    @Test
    @Parameters({"500", "501", "502", "503", "555"})
    public void shouldThrowProperExceptionWhenBankRespondsWith(int statusCode) {
        // given
        userHasExpiredAccessToken();

        // and
        bankRespondsWithGiven(statusCode);

        // expect
        assertThatThrownBy(() -> authenticationController.authenticate(credentials))
                .isExactlyInstanceOf(BankServiceException.class);
    }

    @Test
    @Parameters({"Remote host terminated the handshake", "connect timed out", "connection reset"})
    public void shouldRetryRequestWhenBanksReturnErrorAtFirstTime(String errorMessage) {
        // given
        userHasExpiredAccessToken();

        // and
        bankFailsToRespondOnFirstCall(errorMessage);

        // and
        bankReturnsOauth2TokenBasedOnRefreshToken();

        // when
        authenticationController.authenticate(credentials);

        // then
        assertThatUserIsAuthenticated();
    }

    private void assertThatUserIsAuthenticated() {
        assertThat(persistentStorage.get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class))
                .isNotEmpty()
                .get()
                .usingRecursiveComparison()
                .ignoringFields("issuedAt")
                .isEqualTo(VALID_ACCESS_TOKEN);
    }

    private void setDefaultBankResponses() {
        given(executionFilter.handle(any())).willReturn(response);
        bankRespondsWithGiven(200);
    }

    private void bankRespondsWithGiven(int statusCode) {
        given(response.getStatus()).willReturn(statusCode);
    }

    private void bankReturnsOauth2TokenBasedOnRefreshToken() {
        given(response.getBody(TokenResponse.class)).willReturn(refreshToken());
    }

    private void bankFailsToRespondOnFirstCall(String errorMessage) {
        given(executionFilter.handle(any()))
                .willThrow(new HttpClientException(errorMessage, null))
                .willReturn(response);
    }

    private BelfiusApiClient belfiusApiClient(TinkHttpClient tinkHttpClient) {
        return new BelfiusApiClient(
                tinkHttpClient,
                belfiusTestFixture.belfiusAgentConfiguration(),
                new MockRandomValueGenerator());
    }

    private AutoAuthenticationController autoAuthenticationController(
            CredentialsRequest credentialsRequest,
            ThirdPartyAppAuthenticationController<String> thirdPartyAppAuthenticationController) {
        return new AutoAuthenticationController(
                credentialsRequest,
                new AgentTestContext(credentials),
                thirdPartyAppAuthenticationController,
                oAuth2AuthenticationController);
    }

    private OAuth2AuthenticationController oAuth2AuthenticationController(
            SupplementalInformationHelper supplementalInformationHelper,
            BelfiusApiClient belfiusApiClient) {
        return new OAuth2AuthenticationController(
                persistentStorage,
                supplementalInformationHelper,
                new BelfiusAuthenticator(
                        belfiusApiClient,
                        persistentStorage,
                        belfiusTestFixture.belfiusAgentConfiguration(),
                        credentials.getField(BelfiusConstants.CredentialKeys.IBAN)),
                credentials,
                new StrongAuthenticationState("test_state"));
    }

    private TokenResponse refreshToken() {
        return belfiusTestFixture.fileContent("belfius_oauth2_token.json", TokenResponse.class);
    }

    private void userHasValidAccessToken() {
        persistentStorage.put("oauth2_access_token", VALID_ACCESS_TOKEN);
    }

    private void userHasExpiredAccessToken() {
        OAuth2Token expiredOAuth2Token =
                OAuth2Token.create("blabla-token-type", "tak-acccessToken", "nie-refresh", -1);
        persistentStorage.put("oauth2_access_token", expiredOAuth2Token);
    }

    private CredentialsRequest credentialsRequest() {
        return new RefreshInformationRequest.Builder()
                .user(belfiusTestFixture.belfiusUser())
                .provider(belfiusTestFixture.providerConfiguration())
                .credentials(belfiusTestFixture.belfiusCredentials())
                .userAvailability(belfiusTestFixture.userAvailability())
                .forceAuthenticate(false)
                .build();
    }

    private TinkHttpClient tinkHttpClient() {
        TinkHttpClient tinkHttpClient =
                NextGenTinkHttpClient.builder(
                                new FakeLogMasker(),
                                LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();
        new BelfiusClientConfigurator()
                .configure(
                        tinkHttpClient,
                        persistentStorage,
                        1,
                        1,
                        belfiusTestFixture.sessionExpiryDate());
        tinkHttpClient.addFilter(executionFilter);
        return tinkHttpClient;
    }
}
