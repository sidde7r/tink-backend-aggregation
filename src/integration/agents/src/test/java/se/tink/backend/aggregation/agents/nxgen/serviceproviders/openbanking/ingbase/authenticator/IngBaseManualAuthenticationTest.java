package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc.AuthorizationUrl;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration.MarketConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration.Builder;
import se.tink.backend.aggregation.eidassigner.FakeQsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.NoCodeParamException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.configuration.EIdasTinkCert;
import se.tink.backend.aggregation.nxgen.controllers.utils.MockSupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.controllers.utils.ProviderSessionCacheController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RunWith(JUnitParamsRunner.class)
public final class IngBaseManualAuthenticationTest {

    private final PersistentStorage persistentStorage = new PersistentStorage();
    private final IngBaseAuthenticationTestFixture testFixture =
            new IngBaseAuthenticationTestFixture();
    private final Credentials credentials =
            testFixture.deserializeFromFile("ing_credentials.json", Credentials.class);
    private final CredentialsRequest credentialsRequest =
            testFixture.createCredentialsRequest(credentials, true);
    private final Map<String, String> callbackData = new HashMap<>();

    private Authenticator authenticationController;

    @Mock private HttpResponse response;
    @Mock private Filter executionFilter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        given(executionFilter.handle(any())).willReturn(response);
        authenticationController =
                createAuthenticationController(new MockSupplementalInformationHelper(callbackData));
    }

    @After
    public void cleanUp() {
        callbackData.clear();
    }

    @Test
    @Parameters(method = "prepareExpectedAgentErrors")
    public void shouldThrowProperExceptionWhenBankReturnsSpecificCallbackData(
            String callbackValue, AgentError expectedAgentError) {
        // given
        bankRespondsCorrectlyWithTokenAndAuthorizationUrl();

        // and
        errorCallbackData(callbackValue);

        // then
        assertThatThrownBy(() -> authenticationController.authenticate(credentials))
                .hasFieldOrPropertyWithValue("error", expectedAgentError);
    }

    @SuppressWarnings("unused")
    private Object[] prepareExpectedAgentErrors() {
        return new Object[][] {
            {"access_denied", LoginError.INCORRECT_CREDENTIALS},
            {"login_required", LoginError.INCORRECT_CREDENTIALS},
            {"action_canceled_by_user", ThirdPartyAppError.CANCELLED},
            {"USER-CANCELED-AUTHORIZATION", ThirdPartyAppError.CANCELLED},
            {"invalid_authentication", ThirdPartyAppError.CANCELLED},
            {"server_error", BankServiceError.BANK_SIDE_FAILURE},
            {"temporarily_unavailable", BankServiceError.BANK_SIDE_FAILURE}
        };
    }

    @Test
    @Parameters(method = "prepareExpectedDataForUnrecognisedCallbackData")
    public void shouldThrowWhenUnregonisedCallbackData(
            String callbackKey,
            String callbackValue,
            Class<? extends RuntimeException> expectedException) {
        // given
        bankRespondsCorrectlyWithTokenAndAuthorizationUrl();

        // and
        unrecognisedErrorCallbackData(callbackKey, callbackValue);

        // then
        assertThatThrownBy(() -> authenticationController.authenticate(credentials))
                .isExactlyInstanceOf(expectedException);
    }

    @SuppressWarnings("unused")
    private Object[] prepareExpectedDataForUnrecognisedCallbackData() {
        return new Object[][] {
            {"unrecognised key", "unrecognised value", NoCodeParamException.class},
            {"", "", NoCodeParamException.class},
            {"error", "unrecognised testing error", IllegalStateException.class}
        };
    }

    @Test
    @Parameters(method = "prepareResponseStatusesAndExpectedAgentErrors")
    public void shouldThrowWhenBankRespondsWith(int statusCode, AgentError agentError) {
        // given
        bankRespondsWithGivenStatus(statusCode);

        // and
        correctCallbackData();

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
            {504, BankServiceError.NO_BANK_SERVICE}
        };
    }

    @Test
    public void shouldThrowBankSideErrorWhenInvalidSignature() {
        // given
        bankRespondsWithUnauthorizedStatusAndInvalidSignature();

        // and
        correctCallbackData();

        // then
        assertThatThrownBy(() -> authenticationController.authenticate(credentials))
                .hasFieldOrPropertyWithValue("error", BankServiceError.BANK_SIDE_FAILURE);
    }

    @Test
    @Parameters({"ing_expired_token_response.json", "ing_wrong_token_type_response.json"})
    public void shouldThrowWhenBankReturnsIncorrectTokenResponse(String fileName) {
        // given
        bankRespondsWithToken(fileName);

        // and
        bankRespondsWithAuthorizationUrl();

        // and
        correctCallbackData();

        // then
        assertThatThrownBy(() -> authenticationController.authenticate(credentials))
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    private void bankRespondsWithUnauthorizedStatusAndInvalidSignature() {
        bankRespondsWithGivenStatus(401);
        given(response.getBody(String.class))
                .willReturn("Signature could not be successfully verified");
    }

    private void bankRespondsCorrectlyWithTokenAndAuthorizationUrl() {
        bankRespondsWithToken("ing_token_response.json");
        bankRespondsWithAuthorizationUrl();
    }

    private void bankRespondsWithToken(String fileName) {
        TokenResponse accessToken = testFixture.deserializeFromFile(fileName, TokenResponse.class);
        given(response.getBody(TokenResponse.class)).willReturn(accessToken);
    }

    private void errorCallbackData(String value) {
        callbackData.put("error", value);
    }

    private void unrecognisedErrorCallbackData(String callbackKey, String callbackValue) {
        callbackData.put(callbackKey, callbackValue);
    }

    private void correctCallbackData() {
        callbackData.put("code", "code_test_123");
    }

    private void bankRespondsWithAuthorizationUrl() {
        AuthorizationUrl authorizationUrl =
                testFixture.deserializeFromFile(
                        "ing_authorization_url.json", AuthorizationUrl.class);
        given(response.getBody(AuthorizationUrl.class)).willReturn(authorizationUrl);
    }

    private void bankRespondsWithGivenStatus(int statusCode) {
        given(response.getStatus()).willReturn(statusCode);
    }

    private AutoAuthenticationController createAuthenticationController(
            SupplementalInformationHelper supplementalInfoHelper) {
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
                createThirdPartyAppAuthenticator(supplementalInfoHelper),
                supplementalInfoHelper,
                1);
    }

    private ThirdPartyAppAuthenticator<String> createThirdPartyAppAuthenticator(
            SupplementalInformationHelper supplementalInfoHelper) {
        return createOAuth2AuthenticationController(supplementalInfoHelper);
    }

    @SneakyThrows
    private IngBaseApiClient createIngBaseApiClient() {
        IngBaseApiClient ingBaseApiClient =
                new IngBaseApiClient(
                        testFixture.createTinkHttpClient(executionFilter),
                        persistentStorage,
                        new ProviderSessionCacheController(new AgentTestContext(credentials)),
                        mock(MarketConfiguration.class),
                        new FakeQsealcSigner(),
                        testFixture.createIngApiInputDataMock(credentialsRequest),
                        testFixture.prepareAgentComponentProvider());
        ingBaseApiClient.setConfiguration(prepareAgentConfiguration());

        return ingBaseApiClient;
    }

    private AgentConfiguration<IngBaseConfiguration> prepareAgentConfiguration() {
        return new Builder<IngBaseConfiguration>()
                .setRedirectUrl("https://api.tink.test")
                .setQsealc(EIdasTinkCert.QSEALC)
                .build();
    }
}
