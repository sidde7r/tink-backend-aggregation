package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import javax.ws.rs.core.MediaType;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration.MarketConfiguration;
import se.tink.backend.aggregation.eidassigner.FakeQsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.ProviderSessionCacheController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RunWith(JUnitParamsRunner.class)
public final class IngBaseAutoAuthenticationTest {

    private static final OAuth2Token ACCESS_TOKEN =
            OAuth2Token.create("bearer", "ing_access_token", "ing_refresh_token", 12345L);
    private static final OAuth2Token REFRESHED_ACCESS_TOKEN =
            OAuth2Token.create("bearer", "ing_test_access_token", "ing_refresh_token", 899L);

    private final PersistentStorage persistentStorage = new PersistentStorage();
    private final IngBaseAuthenticationTestFixture testFixture =
            new IngBaseAuthenticationTestFixture();
    private final Credentials credentials =
            testFixture.deserializeFromFile("ing_credentials.json", Credentials.class);
    private final CredentialsRequest credentialsRequest =
            testFixture.createCredentialsRequest(credentials, false);

    private Authenticator authenticationController;

    @Mock private HttpResponse response;
    @Mock private Filter executionFilter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        given(executionFilter.handle(any())).willReturn(response);
        authenticationController = createAuthenticationController();
    }

    @Test
    public void shouldAuthenticateSuccessfully() {
        // given
        validAccessToken();

        // then
        assertThatNoException()
                .isThrownBy(() -> authenticationController.authenticate(credentials));

        // and
        assertThatUserIsAuthenticatedSuccessfully(ACCESS_TOKEN);
    }

    @Test
    public void shouldAuthenticateUsingRefreshTokenWhenAccessTokenIsExpired() {
        // given
        expiredAccessToken();

        // and
        bankReturnsAccessTokenBasedOnValidRefreshToken();

        // when
        authenticationController.authenticate(credentials);

        // then
        assertThatUserIsAuthenticatedSuccessfully(REFRESHED_ACCESS_TOKEN);
    }

    @Test
    @Parameters({
        "Remote host terminated the handshake",
        "connection reset",
        "connect timed out",
        "read timed out",
        "failed to respond"
    })
    public void shouldRetrySuccessfullyOnHttpClientExceptionWith(String exceptionMessage) {
        // given
        expiredAccessToken();

        // and
        bankRespondsCorrectlyAfterSecondRequest(exceptionMessage);

        // when
        authenticationController.authenticate(credentials);

        // then
        assertThatUserIsAuthenticatedSuccessfully(REFRESHED_ACCESS_TOKEN);
    }

    @Test
    @Parameters(method = "prepareResponseStatusesAndExpectedAgentErrors")
    public void shouldThrowProperExceptionWhenBankRespondsWith(
            int statusCode, AgentError agentError) {
        // given
        expiredAccessToken();

        // and
        bankRespondsWithGivenStatus(statusCode);

        // then
        assertThatThrownBy(() -> authenticationController.authenticate(credentials))
                .hasFieldOrPropertyWithValue("error", agentError);
    }

    @SuppressWarnings("unused")
    private Object[] prepareResponseStatusesAndExpectedAgentErrors() {
        return new Object[][] {
            {500, BankServiceError.BANK_SIDE_FAILURE},
            {502, BankServiceError.NO_BANK_SERVICE},
            {503, BankServiceError.NO_BANK_SERVICE},
            {504, BankServiceError.NO_BANK_SERVICE},
            {575, BankServiceError.BANK_SIDE_FAILURE}
        };
    }

    @Test
    public void shouldThrowBankSideErrorWhenInvalidSignature() {
        // given
        expiredAccessToken();

        // and
        bankRespondsWithUnauthorizedStatusAndInvalidSignature();

        // then
        assertThatThrownBy(() -> authenticationController.authenticate(credentials))
                .hasFieldOrPropertyWithValue("error", BankServiceError.BANK_SIDE_FAILURE);
    }

    @Test
    public void shouldThrowSessionErrorAndRemoveOauth2TokenWhenBadRequest() {
        // given
        expiredAccessToken();

        // and
        bankRespondsWithBadRequest();

        // then
        assertThatThrownBy(() -> authenticationController.authenticate(credentials))
                .hasFieldOrPropertyWithValue("error", SessionError.SESSION_EXPIRED);
        assertThat(persistentStorage.containsKey("oauth2_access_token")).isFalse();
    }

    private void bankRespondsWithBadRequest() {
        bankRespondsWithGivenStatus(400);
        given(response.getType()).willReturn(MediaType.APPLICATION_JSON_TYPE);
        given(response.getBody(ErrorResponse.class))
                .willReturn(
                        testFixture.deserializeFromFile(
                                "ing_error_response.json", ErrorResponse.class));
    }

    private void validAccessToken() {
        persistentStorage.put("oauth2_access_token", ACCESS_TOKEN);
    }

    private void bankReturnsAccessTokenBasedOnValidRefreshToken() {
        TokenResponse accessToken =
                testFixture.deserializeFromFile("ing_token_response.json", TokenResponse.class);
        given(response.getBody(TokenResponse.class)).willReturn(accessToken);
    }

    private void bankRespondsWithGivenStatus(int statusCode) {
        given(response.getStatus()).willReturn(statusCode);
    }

    private void bankRespondsCorrectlyAfterSecondRequest(String exceptionMessage) {
        given(executionFilter.handle(any()))
                .willThrow(new HttpClientException(exceptionMessage, null))
                .willReturn(response);
        bankReturnsAccessTokenBasedOnValidRefreshToken();
    }

    private void bankRespondsWithUnauthorizedStatusAndInvalidSignature() {
        bankRespondsWithGivenStatus(401);
        given(response.getBody(String.class))
                .willReturn("Signature could not be successfully verified");
    }

    private void assertThatUserIsAuthenticatedSuccessfully(OAuth2Token expectedToken) {
        assertThat(persistentStorage.get(StorageKeys.TOKEN, OAuth2Token.class))
                .isNotEmpty()
                .get()
                .usingRecursiveComparison()
                .ignoringFields("issuedAt")
                .ignoringFields("refreshExpiresInSeconds")
                .isEqualTo(expectedToken);
    }

    private void expiredAccessToken() {
        persistentStorage.put("CLIENT_ID", "client_Id_test");
        OAuth2Token token = createExpiredOAuth2Token();
        persistentStorage.put("oauth2_access_token", token);
        persistentStorage.put("TOKEN", token);
    }

    private OAuth2Token createExpiredOAuth2Token() {
        return OAuth2Token.create("bearer", "ing_access_token", "ing_refresh_token", -1);
    }

    private AutoAuthenticationController createAuthenticationController() {
        SupplementalInformationHelper supplementalInfoHelper =
                mock(SupplementalInformationHelper.class);
        return new AutoAuthenticationController(
                credentialsRequest,
                new AgentTestContext(credentials),
                createTypedAuthenticator(supplementalInfoHelper),
                createOAuth2AuthenticationController(supplementalInfoHelper));
    }

    private OAuth2AuthenticationController createOAuth2AuthenticationController(
            SupplementalInformationHelper supplementalInfoHelper) {
        return new OAuth2AuthenticationController(
                persistentStorage,
                supplementalInfoHelper,
                new IngBaseAuthenticator(
                        createIngBaseApiClient(),
                        persistentStorage,
                        credentialsRequest,
                        new ConstantLocalDateTimeSource()),
                credentials,
                testFixture.createStrongAuthenticationState());
    }

    private TypedAuthenticator createTypedAuthenticator(
            SupplementalInformationHelper supplementalInfoHelper) {
        return new ThirdPartyAppAuthenticationController<>(
                createThirdPartyAppAuthenticator(supplementalInfoHelper), supplementalInfoHelper);
    }

    private ThirdPartyAppAuthenticator<String> createThirdPartyAppAuthenticator(
            SupplementalInformationHelper supplementalInfoHelper) {
        return createOAuth2AuthenticationController(supplementalInfoHelper);
    }

    private IngBaseApiClient createIngBaseApiClient() {
        return new IngBaseApiClient(
                testFixture.createTinkHttpClient(executionFilter),
                persistentStorage,
                new ProviderSessionCacheController(new AgentTestContext(credentials)),
                mock(MarketConfiguration.class),
                new FakeQsealcSigner(),
                testFixture.createIngApiInputDataMock(credentialsRequest),
                testFixture.prepareAgentComponentProvider());
    }
}
