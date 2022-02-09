package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.libraries.payments.common.model.PaymentScheme.SEPA_CREDIT_TRANSFER;
import static se.tink.libraries.payments.common.model.PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner;
import se.tink.agent.sdk.utils.signer.signature.Signature;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
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
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration.Builder;
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
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@RunWith(JUnitParamsRunner.class)
public class IngPaymentControllerTest {

    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ingbase/resources/";
    private static final String CONNECTION_RESET_MESSAGE = "connection reset";
    private static final String CONNECTION_TIMEOUT_MESSAGE = "connect timed out";
    private static final String READ_TIMEOUT_MESSAGE = "read timed out";
    private static final String FAILED_TO_RESPOND_MESSAGE = "failed to respond";
    private static final String TOKEN_RESPONSE_FILE = "token_response.json";
    private static final String PAYMENT_CREATE_RESPONSE_FILE =
            "ing_payment_create_response_accepted.json";

    private PaymentController paymentController;
    private IngPaymentExecutor paymentExecutor;
    private SessionStorage sessionStorage;
    private PersistentStorage persistentStorage;

    @Mock private AgentComponentProvider agentComponentProvider;
    @Mock private Filter callFilter;
    @Mock private HttpResponse response;
    @Mock private MarketConfiguration marketConfiguration;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setUpStorage();
        configureBasicMocks();
        paymentExecutor = createIngPaymentExecutor(createIngPaymentApiClient(), false);
        paymentController = new PaymentController(paymentExecutor, paymentExecutor);
    }

    @Test
    @Parameters({
        "SEPA_CREDIT_TRANSFER, true",
        "SEPA_CREDIT_TRANSFER, false",
        "SEPA_INSTANT_CREDIT_TRANSFER, true"
    })
    public void shouldCreatePaymentSuccessfully(
            PaymentScheme paymentScheme, boolean instantSepaIsSupported) {
        // given
        paymentExecutor =
                createIngPaymentExecutor(createIngPaymentApiClient(), instantSepaIsSupported);
        paymentController = new PaymentController(paymentExecutor, paymentExecutor);

        // and
        prepareCreatePaymentTestSetupAndResponseData();

        // when
        PaymentResponse paymentResponse =
                paymentController.create(new PaymentRequest(createPayment(paymentScheme)));

        // expect
        assertThat(paymentResponse)
                .usingRecursiveComparison()
                .ignoringFields("payment.id")
                .isEqualTo(getExpectedPaymentResponseOnCreate(paymentScheme));

        // and
        assertThat(sessionStorage).containsKey("PAYMENT_AUTHORIZATION_URL");
    }

    @Test
    @Parameters(method = "prepareBankServiceErrorData")
    public void shouldThrowBankServiceErrorWhenBankRespondsWithSpecific5xxStatus(
            int statusCode, AgentError agentError) {
        // given
        bankRespondsWithGivenStatus(statusCode);

        // and
        PaymentMultiStepRequest paymentMultiStepRequest = createPaymentMultiStepRequest();

        // expect
        assertThatThrownBy(() -> paymentController.sign(paymentMultiStepRequest))
                .hasFieldOrPropertyWithValue("error", agentError);
    }

    @SuppressWarnings("unused")
    private Object[] prepareBankServiceErrorData() {
        return new Object[][] {
            {500, BankServiceError.BANK_SIDE_FAILURE},
            {502, BankServiceError.NO_BANK_SERVICE},
            {503, BankServiceError.NO_BANK_SERVICE},
            {504, BankServiceError.NO_BANK_SERVICE}
        };
    }

    @Test
    @Parameters(method = "prepareTimeoutErrorData")
    public void shouldThrowBankSideErrorWhenTimeout(
            String exceptionMessage, AgentError agentError) {
        // given
        given(callFilter.handle(any())).willThrow(new HttpClientException(exceptionMessage, null));

        // expect
        assertThatThrownBy(
                        () ->
                                paymentController.create(
                                        new PaymentRequest(createPayment(SEPA_CREDIT_TRANSFER))))
                .hasFieldOrPropertyWithValue("error", agentError);
    }

    @SuppressWarnings("unused")
    private Object[] prepareTimeoutErrorData() {
        return new Object[][] {
            {CONNECTION_RESET_MESSAGE, BankServiceError.BANK_SIDE_FAILURE},
            {CONNECTION_TIMEOUT_MESSAGE, BankServiceError.BANK_SIDE_FAILURE},
            {READ_TIMEOUT_MESSAGE, BankServiceError.BANK_SIDE_FAILURE},
            {FAILED_TO_RESPOND_MESSAGE, BankServiceError.BANK_SIDE_FAILURE},
        };
    }

    @Test
    @Parameters({
        "Remote host terminated the handshake",
        CONNECTION_RESET_MESSAGE,
        CONNECTION_TIMEOUT_MESSAGE,
        READ_TIMEOUT_MESSAGE,
        FAILED_TO_RESPOND_MESSAGE
    })
    public void shouldRetrySuccessfully(String exceptionMessage) {
        // given
        bankRespondsCorrectlyAfterSecondRequest(exceptionMessage);

        // expect
        assertThatNoException()
                .isThrownBy(
                        () ->
                                paymentController.create(
                                        new PaymentRequest(createPayment(SEPA_CREDIT_TRANSFER))));
    }

    @Test
    public void shouldThrowBankSideErrorWhenInvalidSignature() {
        // given
        bankRespondsWithUnauthorizedStatusAndInvalidSignature();

        // expect
        assertThatThrownBy(
                        () ->
                                paymentController.fetch(
                                        new PaymentRequest(createPayment(SEPA_CREDIT_TRANSFER))))
                .hasFieldOrPropertyWithValue("error", BankServiceError.BANK_SIDE_FAILURE);
    }

    @Test
    @Parameters({"422", "500"})
    public void shouldThrowWhenInstantSepaIsNotPossible(int statusCode) throws IOException {
        // given
        paymentExecutor = createIngPaymentExecutor(createIngPaymentApiClient(), true);
        paymentController = new PaymentController(paymentExecutor, paymentExecutor);

        // and
        PaymentRequest paymentRequest =
                new PaymentRequest(createPayment(SEPA_INSTANT_CREDIT_TRANSFER));

        // and
        bankRespondsWithGivenStatusAndInstantSepaNotPossible(statusCode);

        // expect
        assertInstantPaymentNotSupportedErrorThrown(() -> paymentController.create(paymentRequest));

        // and
        then(response).should(atLeastOnce()).getStatus();
        then(response).should(atLeastOnce()).getBody(String.class);
    }

    @Test
    public void shouldThrowWhenInstantPaymentSchemaIsNotSupported() {
        // given
        PaymentRequest paymentRequest =
                new PaymentRequest(createPayment(SEPA_INSTANT_CREDIT_TRANSFER));

        // expect
        assertInstantPaymentNotSupportedErrorThrown(() -> paymentController.create(paymentRequest));

        // and
        then(response).shouldHaveNoInteractions();
    }

    @Ignore("Requires MINI-1708")
    @Test
    @Parameters(method = "prepareMissingKeyData")
    public void shouldNotCreatePaymentWhenMissingKeyData(
            String tokenResponseFileName,
            String paymentCreateResponseFileName,
            String exceptionMessage) {
        // given
        prepareTestSetupForCreatePaymentWithoutKeyData(
                tokenResponseFileName, paymentCreateResponseFileName);

        // and
        PaymentRequest paymentRequest = new PaymentRequest(createPayment(SEPA_CREDIT_TRANSFER));

        // expect
        assertThatThrownBy(() -> paymentController.create(paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(exceptionMessage);
    }

    @SuppressWarnings("unused")
    private Object[] prepareMissingKeyData() {
        return new Object[][] {
            {
                "token_response_no_clientId.json",
                PAYMENT_CREATE_RESPONSE_FILE,
                "Invalid value of clientId to be persisted!"
            },
            {
                "token_response_no_token_data.json",
                PAYMENT_CREATE_RESPONSE_FILE,
                "Invalid token data to be persisted!"
            },
            {
                TOKEN_RESPONSE_FILE,
                "ing_payment_create_response_no_auth_url.json",
                "Invalid payment authorization url to be persisted!"
            }
        };
    }

    private PaymentResponse getExpectedPaymentResponseOnCreate(PaymentScheme paymentScheme) {
        Payment payment = createPayment(paymentScheme);
        payment.setStatus(PaymentStatus.PENDING);

        return new PaymentResponse(payment, new Storage());
    }

    private Payment createPayment(PaymentScheme paymentScheme) {
        LocalDate localDate =
                SEPA_INSTANT_CREDIT_TRANSFER.equals(paymentScheme) ? null : LocalDate.now();
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
                .withExecutionDate(localDate)
                .withRemittanceInformation(setUpRemittanceInformation())
                .withPaymentScheme(paymentScheme)
                .build();
    }

    private void prepareResponseForPaymentCreation() {
        given(callFilter.handle(any())).willReturn(response);
        bankRespondsWithGivenStatus(200);
        prepareResponseBodyForPaymentCreation(TOKEN_RESPONSE_FILE, PAYMENT_CREATE_RESPONSE_FILE);
    }

    private void prepareResponseBodyForPaymentCreation(
            String tokenResponseFileName, String paymentCreateResponseFileName) {
        TokenResponse ingTokenResponse =
                deserializeFromFile(RESOURCE_PATH + tokenResponseFileName, TokenResponse.class);
        IngCreatePaymentResponse ingPaymentInitiationResponseReceived =
                deserializeFromFile(
                        RESOURCE_PATH + paymentCreateResponseFileName,
                        IngCreatePaymentResponse.class);

        setUpResponseBody(TokenResponse.class, ingTokenResponse);
        setUpResponseBody(IngCreatePaymentResponse.class, ingPaymentInitiationResponseReceived);
    }

    private <T> void setUpResponseBody(Class<T> bodyClass, T responseBody) {
        given(response.getBody(bodyClass)).willReturn(responseBody);
    }

    private void bankRespondsWithUnauthorizedStatusAndInvalidSignature() {
        bankRespondsWithGivenStatus(401);
        setUpResponseBody(String.class, "Signature could not be successfully verified");
    }

    private void bankRespondsWithGivenStatusAndInstantSepaNotPossible(int status)
            throws IOException {
        bankRespondsWithGivenStatus(status);
        setUpResponseBody(
                String.class,
                FileUtils.readFileToString(
                        Paths.get(RESOURCE_PATH, "sepa_instant_not_possible.json").toFile(),
                        StandardCharsets.UTF_8));
    }

    private void bankRespondsCorrectlyAfterSecondRequest(String exceptionMessage) {
        given(callFilter.handle(any()))
                .willThrow(new HttpClientException(exceptionMessage, null))
                .willReturn(response);
        bankRespondsWithGivenStatus(200);
        prepareResponseBodyForPaymentCreation(TOKEN_RESPONSE_FILE, PAYMENT_CREATE_RESPONSE_FILE);
    }

    private void bankRespondsWithGivenStatus(int statusCode) {
        given(response.getStatus()).willReturn(statusCode);
    }

    private void prepareCreatePaymentTestSetupAndResponseData() {
        prepareResponseForPaymentCreation();
        OAuth2Token token =
                OAuth2Token.create("bearer", "ing-access-token", "ing-refresh-token", 12345L);
        persistentStorage.put(StorageKeys.TOKEN, token);
        persistentStorage.put(StorageKeys.CLIENT_ID, "some_client_id");
        sessionStorage.clear();
    }

    private void prepareTestSetupForCreatePaymentWithoutKeyData(
            String tokenResponseFileName, String paymentCreateResponseFileName) {
        given(callFilter.handle(any())).willReturn(response);
        bankRespondsWithGivenStatus(200);
        prepareResponseBodyForPaymentCreation(tokenResponseFileName, paymentCreateResponseFileName);
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

    private void assertInstantPaymentNotSupportedErrorThrown(ThrowingCallable throwingCallable) {
        assertThatThrownBy(throwingCallable)
                .isInstanceOf(PaymentValidationException.class)
                .hasMessage("Instant payment is not supported");
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
        QsealcSigner qsealcSigner = mock(QsealcSigner.class);
        when(qsealcSigner.sign(any(), any())).thenReturn(Signature.create("test".getBytes()));

        IngPaymentApiClient ingPaymentApiClient =
                new IngPaymentApiClient(
                        createTinkHttpClient(),
                        persistentStorage,
                        new ProviderSessionCacheController(
                                new MockSessionCacheProvider(new HashMap<>())),
                        marketConfiguration,
                        qsealcSigner,
                        IngApiInputData.builder()
                                .userAuthenticationData(
                                        new IngUserAuthenticationData(true, "psuIpAddress"))
                                .credentialsRequest(mock(CredentialsRequest.class))
                                .strongAuthenticationState(
                                        new StrongAuthenticationState("test_state"))
                                .build(),
                        agentComponentProvider);
        ingPaymentApiClient.setConfiguration(prepareAgentConfiguration());

        return ingPaymentApiClient;
    }

    private void configureBasicMocks() {
        setUpAgentComponentProvider();
        setUpMarketConfiguration();
        given(callFilter.handle(any())).willReturn(response);
    }

    private PaymentMultiStepRequest createPaymentMultiStepRequest() {
        return new PaymentMultiStepRequest(
                createPayment(SEPA_CREDIT_TRANSFER), persistentStorage, "INIT", emptyList());
    }

    private void setUpAgentComponentProvider() {
        given(agentComponentProvider.getRandomValueGenerator())
                .willReturn(new MockRandomValueGenerator());
        given(agentComponentProvider.getLocalDateTimeSource())
                .willReturn(new ConstantLocalDateTimeSource());
    }

    private void setUpMarketConfiguration() {
        given(marketConfiguration.marketCode()).willReturn("AA");
    }

    private AgentConfiguration<IngBaseConfiguration> prepareAgentConfiguration() {
        return new Builder<IngBaseConfiguration>()
                .setRedirectUrl("https://api.tink.test")
                .setQsealc(EIdasTinkCert.QSEALC)
                .build();
    }

    private IngPaymentExecutor createIngPaymentExecutor(
            IngPaymentApiClient ingPaymentApiClient, boolean instantSepaIsSupported) {
        return new IngPaymentExecutor(
                sessionStorage,
                ingPaymentApiClient,
                mock(IngPaymentAuthenticator.class),
                new IngPaymentMapper(new BasePaymentMapper()),
                instantSepaIsSupported);
    }

    private void setUpStorage() {
        persistentStorage = new PersistentStorage();
        sessionStorage = new SessionStorage();
        persistentStorage.put(
                StorageKeys.APPLICATION_TOKEN,
                OAuth2Token.create("bearer", "test_access_token", "test_refresh_token", 899));
        persistentStorage.put(StorageKeys.CLIENT_ID, "some_client_id");
        sessionStorage.put(StorageKeys.PAYMENT_AUTHORIZATION_URL, "https://auth.url.ing.com");
    }
}
