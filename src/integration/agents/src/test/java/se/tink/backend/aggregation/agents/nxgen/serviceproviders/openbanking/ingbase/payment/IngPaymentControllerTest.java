package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngApiInputData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngUserAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration.MarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.filters.IngBaseTinkClientConfigurator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngCreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentMapper;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.configuration.EIdasTinkCert;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.MockSessionCacheProvider;
import se.tink.backend.aggregation.nxgen.controllers.utils.ProviderSessionCacheController;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@RunWith(JUnitParamsRunner.class)
public class IngPaymentControllerTest {

    private static final String CONNECTION_RESET_MESSAGE = "connection reset";
    private static final String CONNECTION_TIMEOUT_MESSAGE = "connect timed out";
    private static final String READ_TIMEOUT_MESSAGE = "read timed out";
    private static final String FAILED_TO_RESPOND_MESSAGE = "failed to respond";
    private static final AgentError BANK_SIDE_FAILURE = BankServiceError.BANK_SIDE_FAILURE;

    private PaymentController paymentController;
    private SessionStorage sessionStorage;
    private PersistentStorage persistentStorage;

    @Mock private Filter callFilter;
    @Mock private HttpResponse response;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setUpStorage();
        final IngPaymentApiClient paymentApiClient = createIngPaymentApiClient();
        final IngPaymentExecutor paymentExecutor = createIngPaymentExecutor(paymentApiClient);
        paymentController = new PaymentController(paymentExecutor, paymentExecutor);
        given(callFilter.handle(any())).willReturn(response);
    }

    @Test
    public void shouldCreatePaymentSuccessfully() {

        // given
        prepareResponseForPaymentCreation();

        // when
        PaymentResponse paymentResponse =
                paymentController.create(new PaymentRequest(createPayment()));

        // expect
        assertThat(paymentResponse)
                .usingRecursiveComparison()
                .ignoringFields("payment.id")
                .isEqualTo(getExpectedPaymentResponseOnCreate());
    }

    @Test
    @Parameters(method = "prepareNoCallbackErrorData")
    public void shouldThrowBankServiceErrorWhenNoCallback(int statusCode, AgentError agentError) {

        // given
        bankRespondsWithGivenStatus(statusCode);

        // expect
        assertThatThrownBy(
                        () ->
                                paymentController.sign(
                                        new PaymentMultiStepRequest(
                                                createPayment(),
                                                persistentStorage,
                                                "INIT",
                                                emptyList())))
                .hasFieldOrPropertyWithValue("error", agentError);
    }

    @SuppressWarnings("unused")
    private Object[] prepareNoCallbackErrorData() {
        AgentError noBankService = BankServiceError.NO_BANK_SERVICE;
        return new Object[][] {
            {500, BANK_SIDE_FAILURE},
            {502, noBankService},
            {503, noBankService},
            {504, noBankService}
        };
    }

    @Test
    @Parameters(method = "prepareTimeoutErrorData")
    public void shouldThrowBankSideErrorWhenTimeout(
            String exceptionMessage, AgentError agentError) {

        // given
        given(callFilter.handle(any())).willThrow(new HttpClientException(exceptionMessage, null));

        // expect
        assertThatThrownBy(() -> paymentController.create(new PaymentRequest(createPayment())))
                .hasFieldOrPropertyWithValue("error", agentError);
    }

    @SuppressWarnings("unused")
    private Object[] prepareTimeoutErrorData() {
        return new Object[][] {
            {CONNECTION_RESET_MESSAGE, BANK_SIDE_FAILURE},
            {CONNECTION_TIMEOUT_MESSAGE, BANK_SIDE_FAILURE},
            {READ_TIMEOUT_MESSAGE, BANK_SIDE_FAILURE},
            {FAILED_TO_RESPOND_MESSAGE, BANK_SIDE_FAILURE},
        };
    }

    @Test
    @Parameters(method = "prepareRetryErrorData")
    public void shouldRetrySuccessfully(String exceptionMessage) {

        // given
        bankRespondsCorrectlyAfterSecondRequest(exceptionMessage);

        // expect
        assertThatNoException()
                .isThrownBy(() -> paymentController.create(new PaymentRequest(createPayment())));
    }

    @SuppressWarnings("unused")
    private Object[] prepareRetryErrorData() {
        return new Object[] {
            "Remote host terminated the handshake",
            CONNECTION_RESET_MESSAGE,
            CONNECTION_TIMEOUT_MESSAGE,
            READ_TIMEOUT_MESSAGE,
            FAILED_TO_RESPOND_MESSAGE
        };
    }

    @Test
    public void shouldThrowBankSideErrorWhenBankReturnsUnauthorizedStatus() {

        // given
        bankRespondsWithUnauthorizedStatus();

        // expect
        assertThatThrownBy(() -> paymentController.fetch(new PaymentRequest(createPayment())))
                .hasFieldOrPropertyWithValue("error", BANK_SIDE_FAILURE);
    }

    @Test
    @Parameters(method = "prepareMissingKeyData")
    public void shouldNotSignPaymentWhenMissingKeyData(
            boolean userIsAuthenticated,
            boolean userHasClientId,
            boolean sessionHasAuthorizationUrl,
            String exceptionMessage) {

        // given
        setUpTestDataWithMissingKeyUserData(
                userIsAuthenticated, userHasClientId, sessionHasAuthorizationUrl);

        // and
        bankRespondsWithGivenStatus(200);

        // expect
        assertThatThrownBy(
                        () ->
                                paymentController.sign(
                                        new PaymentMultiStepRequest(
                                                createPayment(),
                                                persistentStorage,
                                                "INIT",
                                                emptyList())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(exceptionMessage);
    }

    @SuppressWarnings("unused")
    private Object[] prepareMissingKeyData() {
        boolean userIsAuthenticated = true;
        boolean userHasClientId = true;
        boolean sessionHasAuthorizationUrl = true;

        return new Object[][] {
            {
                !userIsAuthenticated,
                userHasClientId,
                sessionHasAuthorizationUrl,
                "Cannot find Token!"
            },
            {
                userIsAuthenticated,
                !userHasClientId,
                sessionHasAuthorizationUrl,
                "Cannot find client id!"
            },
            {
                userIsAuthenticated,
                userHasClientId,
                !sessionHasAuthorizationUrl,
                "Missing authorize payment url"
            }
        };
    }

    private PaymentResponse getExpectedPaymentResponseOnCreate() {
        Payment payment = createPayment();
        payment.setStatus(PaymentStatus.PENDING);

        return new PaymentResponse(payment, new Storage());
    }

    private Payment createPayment() {
        return new Payment.Builder()
                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1.0))
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN,
                                        "BE54000000000000",
                                        "accountTest1"),
                                "Jan Kovalsky"))
                .withDebtor(
                        new Debtor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN,
                                        "BE54000000000000",
                                        "accountTest2")))
                .withCurrency("EUR")
                .withUniqueId("DUMMY_PAYMENT_ID")
                .withExecutionDate(LocalDate.now())
                .withRemittanceInformation(setUpRemittanceInformation())
                .build();
    }

    private void prepareResponseForPaymentCreation() {
        given(callFilter.handle(any())).willReturn(response);
        bankRespondsWithGivenStatus(200);
        prepareResponseBodyForPaymentCreation();
    }

    private void prepareResponseBodyForPaymentCreation() {
        String resourcePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ingbase/resources/";
        TokenResponse ingCorrectTokenResponse =
                deserializeFromFile(resourcePath + "token_response.json", TokenResponse.class);
        IngCreatePaymentResponse ingPaymentInitiationResponseReceived =
                deserializeFromFile(
                        resourcePath + "ing_payment_create_response_accepted.json",
                        IngCreatePaymentResponse.class);

        setUpResponseBody(TokenResponse.class, ingCorrectTokenResponse);
        setUpResponseBody(IngCreatePaymentResponse.class, ingPaymentInitiationResponseReceived);
    }

    private <T> void setUpResponseBody(Class<T> bodyClass, T responseBody) {
        given(response.getBody(bodyClass)).willReturn(responseBody);
    }

    private void bankRespondsWithUnauthorizedStatus() {
        bankRespondsWithGivenStatus(401);
        setUpResponseBody(String.class, "Signature could not be successfully verified");
    }

    private void bankRespondsCorrectlyAfterSecondRequest(String exceptionMessage) {
        given(callFilter.handle(any()))
                .willThrow(new HttpClientException(exceptionMessage, null))
                .willReturn(response);
        bankRespondsWithGivenStatus(200);
        prepareResponseBodyForPaymentCreation();
    }

    private void bankRespondsWithGivenStatus(int statusCode) {
        given(response.getStatus()).willReturn(statusCode);
    }

    private void setUpTestDataWithMissingKeyUserData(
            boolean userIsAuthenticated,
            boolean userHasClientId,
            boolean sessionHasAuthorizationUrl) {
        persistentStorage = new PersistentStorage();

        if (userIsAuthenticated) {
            OAuth2Token token =
                    OAuth2Token.create("bearer", "ing-access-token", "ing-refresh-token", 12345L);
            persistentStorage.put(StorageKeys.TOKEN, token);
        }
        if (userHasClientId) {
            persistentStorage.put(StorageKeys.CLIENT_ID, "some_client_id");
        }
        if (!sessionHasAuthorizationUrl) {
            sessionStorage = new SessionStorage();
        }
        IngPaymentApiClient paymentApiClient = createIngPaymentApiClient();
        final IngPaymentExecutor paymentExecutor = createIngPaymentExecutor(paymentApiClient);
        paymentController = new PaymentController(paymentExecutor, paymentExecutor);
    }

    private RemittanceInformation setUpRemittanceInformation() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Example remittance information");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        return remittanceInformation;
    }

    private static <T> T deserializeFromFile(String filePath, Class<T> clazz) {
        return SerializationUtils.deserializeFromString(Paths.get(filePath).toFile(), clazz);
    }

    private TinkHttpClient createTinkHttpClient() {
        TinkHttpClient client =
                NextGenTinkHttpClient.builder(
                                new FakeLogMasker(),
                                LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();
        new IngBaseTinkClientConfigurator().configureClient(client, 1, 1);
        client.addFilter(callFilter);

        return client;
    }

    @SneakyThrows
    private IngPaymentApiClient createIngPaymentApiClient() {
        ProviderSessionCacheController providerSessionCacheController =
                new ProviderSessionCacheController(new MockSessionCacheProvider(new HashMap<>()));

        IngPaymentApiClient ingPaymentApiClient =
                new IngPaymentApiClient(
                        createTinkHttpClient(),
                        persistentStorage,
                        providerSessionCacheController,
                        createMarketConfiguration(),
                        mock(QsealcSigner.class),
                        IngApiInputData.builder()
                                .userAuthenticationData(
                                        new IngUserAuthenticationData(true, "psuIpAddress"))
                                .credentialsRequest(mock(CredentialsRequest.class))
                                .strongAuthenticationState(
                                        new StrongAuthenticationState("test_state"))
                                .build(),
                        createAgentComponentProvider());
        ingPaymentApiClient.setConfiguration(prepareAgentConfiguration());

        return ingPaymentApiClient;
    }

    private AgentComponentProvider createAgentComponentProvider() {
        AgentComponentProvider agentComponentProvider = mock(AgentComponentProvider.class);
        given(agentComponentProvider.getRandomValueGenerator())
                .willReturn(new MockRandomValueGenerator());
        given(agentComponentProvider.getLocalDateTimeSource())
                .willReturn(new ConstantLocalDateTimeSource());

        return agentComponentProvider;
    }

    private AgentConfiguration prepareAgentConfiguration() {
        AgentConfiguration<IngBaseConfiguration> agentConfiguration =
                mock(AgentConfiguration.class);
        given(agentConfiguration.getRedirectUrl()).willReturn("https://api.tink.test");
        given(agentConfiguration.getQsealc()).willReturn(EIdasTinkCert.QSEALC);

        return agentConfiguration;
    }

    private MarketConfiguration createMarketConfiguration() {
        MarketConfiguration marketConfiguration = mock(MarketConfiguration.class);
        given(marketConfiguration.marketCode()).willReturn("AA");

        return marketConfiguration;
    }

    private IngPaymentExecutor createIngPaymentExecutor(IngPaymentApiClient ingPaymentApiClient) {
        IngPaymentMapper paymentMapper = new IngPaymentMapper(new BasePaymentMapper());
        IngPaymentAuthenticator paymentAuthenticator = mock(IngPaymentAuthenticator.class);

        return new IngPaymentExecutor(
                sessionStorage, ingPaymentApiClient, paymentAuthenticator, paymentMapper);
    }

    private void setUpStorage() {
        persistentStorage = new PersistentStorage();
        sessionStorage = new SessionStorage();
        persistentStorage.put(
                StorageKeys.APPLICATION_TOKEN,
                OAuth2Token.create("bearer", "test_access_token", "test_refresh_token", 899));
        persistentStorage.put(StorageKeys.CLIENT_ID, "some_client_id");
        sessionStorage.put(StorageKeys.PAYMENT_AUTHORIZATION_URL, "http://auth.url.ing.com");
    }
}
