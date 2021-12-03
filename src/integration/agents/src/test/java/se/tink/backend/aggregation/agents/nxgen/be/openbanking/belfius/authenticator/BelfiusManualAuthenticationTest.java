package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.filter.BelfiusClientConfigurator;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.NoCodeParamException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.MockSupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;

@RunWith(JUnitParamsRunner.class)
public class BelfiusManualAuthenticationTest {

    private Authenticator authenticationController;
    private CredentialsRequest credentialsRequest;
    private Credentials credentials;
    private PersistentStorage persistentStorage;
    private BelfiusApiClient belfiusApiClient;
    private BelfiusTestFixture belfiusTestFixture;
    @Mock private Filter executionFilter;
    @Mock private HttpResponse response;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        belfiusTestFixture = new BelfiusTestFixture();
        TinkHttpClient tinkHttpClient = tinkHttpClient();
        belfiusApiClient =
                new BelfiusApiClient(
                        tinkHttpClient,
                        belfiusTestFixture.belfiusAgentConfiguration(),
                        new MockRandomValueGenerator());
        persistentStorage = new PersistentStorage();
        credentialsRequest = manualCredentialsRequest();
        credentials = credentialsRequest.getCredentials();
        setDefaultBankResponses();
    }

    @Test
    @Parameters(method = "callbackDataWithCorrespondingErrors")
    public void shouldThrowProperExceptionWhenNoCallbackCodeIsReturned(
            Map<String, String> callbackData, Class<RuntimeException> exceptionClass) {
        // given
        bankReturnsConsentResponse();

        // and
        SupplementalInformationHelper supplementalInformationHelper =
                new MockSupplementalInformationHelper(callbackData);

        // when
        authenticationController = authenticationController(supplementalInformationHelper);

        // then
        assertThatThrownBy(() -> authenticationController.authenticate(credentials))
                .isExactlyInstanceOf(exceptionClass);
    }

    @Test
    @Parameters({"500", "501", "502", "503", "555"})
    public void shouldThrowProperExceptionWhenBankRespondsWith(int statusCode) {
        // given
        SupplementalInformationHelper supplementalInformationHelper =
                new MockSupplementalInformationHelper(validCallbackData());
        authenticationController = authenticationController(supplementalInformationHelper);

        // and
        bankRespondsWithGivenStatusWhenCreatingConsent(statusCode);

        // expect
        assertThatThrownBy(() -> authenticationController.authenticate(credentials))
                .isExactlyInstanceOf(BankServiceException.class);
    }

    @SuppressWarnings("unused")
    private Object[] callbackDataWithCorrespondingErrors() {
        return new Object[][] {
            {errorCodeCallbackDataWithMessage("unknown"), IllegalStateException.class},
            {errorCodeCallbackDataWithMessage("access_denied"), LoginException.class},
            {errorCodeCallbackDataWithMessage("login_required"), LoginException.class},
            {errorCodeCallbackDataWithMessage("server_error"), BankServiceException.class},
            {errorCodeCallbackDataWithMessage("random_data"), IllegalStateException.class},
            {
                errorCodeCallbackDataWithMessage("temporarily_unavailable"),
                BankServiceException.class
            },
            {noCallback(), ThirdPartyAppException.class},
            {emptyCallbackData(), NoCodeParamException.class}
        };
    }

    private ImmutableMap<String, String> validCallbackData() {
        return ImmutableMap.of("code", "1234");
    }

    private Map<String, String> errorCodeCallbackDataWithMessage(String message) {
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put("error", message);
        return callbackData;
    }

    private Map<String, String> noCallback() {
        return null;
    }

    private Map<String, String> emptyCallbackData() {
        return emptyMap();
    }

    private AutoAuthenticationController authenticationController(
            SupplementalInformationHelper supplementalInformationHelper) {
        OAuth2AuthenticationController oAuth2AuthenticationController =
                oAuth2AuthenticationController(supplementalInformationHelper);
        ThirdPartyAppAuthenticationController<String> thirdPartyAppAuthenticationController =
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController, supplementalInformationHelper);

        return new AutoAuthenticationController(
                credentialsRequest,
                new AgentTestContext(credentials),
                thirdPartyAppAuthenticationController,
                oAuth2AuthenticationController);
    }

    private OAuth2AuthenticationController oAuth2AuthenticationController(
            SupplementalInformationHelper supplementalInformationHelper) {
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

    private void setDefaultBankResponses() {
        given(executionFilter.handle(any())).willReturn(response);
        bankRespondsWithGivenStatusWhenCreatingConsent(200);
    }

    private void bankRespondsWithGivenStatusWhenCreatingConsent(int statusCode) {
        given(response.getStatus()).willReturn(statusCode);
    }

    private void bankReturnsConsentResponse() {
        given(response.getBody(ConsentResponse[].class)).willReturn(consentResponse());
    }

    private ConsentResponse[] consentResponse() {
        return belfiusTestFixture.fileContent(
                "belfius_consent_response.json", ConsentResponse[].class);
    }

    private CredentialsRequest manualCredentialsRequest() {
        return new RefreshInformationRequest.Builder()
                .user(belfiusTestFixture.belfiusUser())
                .provider(belfiusTestFixture.providerConfiguration())
                .credentials(belfiusTestFixture.belfiusCredentials())
                .userAvailability(belfiusTestFixture.userAvailability())
                .forceAuthenticate(true)
                .build();
    }

    private TinkHttpClient tinkHttpClient() {
        TinkHttpClient tinkHttpClient =
                NextGenTinkHttpClient.builder(
                                new FakeLogMasker(),
                                LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();
        new BelfiusClientConfigurator(new ConstantLocalDateTimeSource())
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
