package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationTimeOutException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsRateLimitFilterProperties;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsRetryFilterProperties;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsTinkApiClientConfigurator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsTransactionStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc.SibsGetPaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc.SibsPaymentInitiationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign.SibsRedirectCallbackHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign.SibsRedirectSignPaymentStrategy;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign.SignPaymentStrategy;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils.SibsUtils;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@RunWith(JUnitParamsRunner.class)
public class SibsPaymentExecutorTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sibs/executor/payment/resources/";

    private static final boolean IS_USER_PRESENT = false;
    private static final String USER_IP = "0.0.0.0";
    private static final String STATE = "test_state";
    private static final String ASPSP_CODE = "TEST_CODE";
    private static final String REDIRECT_URL = "https://api.tink.test";
    private static final String PROVIDER_NAME = "TEST_PROVIDER";
    private static final LocalDateTimeSource DATE_TIME_SOURCE = new ConstantLocalDateTimeSource();

    private static final SibsPaymentInitiationResponse SIBS_PAYMENT_INITIATION_RESPONSE_RECEIVED =
            deserializeFromFile(
                    RESOURCES_PATH + "sibs_payment_init_received.json",
                    SibsPaymentInitiationResponse.class);
    private static final SibsGetPaymentStatusResponse SIBS_PAYMENT_STATUS_RESPONSE_ACSC =
            deserializeFromFile(
                    RESOURCES_PATH + "sibs_payment_status_accepted.json",
                    SibsGetPaymentStatusResponse.class);

    @Mock private Filter callFilter;
    @Mock private HttpResponse response;
    @Mock private SibsRedirectCallbackHandler redirectCallbackHandler;
    @Mock private AgentConfiguration<SibsConfiguration> agentConfiguration;

    private SibsPaymentExecutor sibsPaymentExecutor;
    private PaymentController paymentController;

    private final PersistentStorage persistentStorage = new PersistentStorage();
    private final TinkHttpClient client =
            NextGenTinkHttpClient.builder(
                            new FakeLogMasker(),
                            LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                    .build();

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        given(agentConfiguration.getRedirectUrl()).willReturn(REDIRECT_URL);
        SibsBaseApiClient sibsBaseApiClient = createAndConfigureBaseApiClient();
        SignPaymentStrategy signPaymentStrategy =
                new SibsRedirectSignPaymentStrategy(
                        sibsBaseApiClient,
                        redirectCallbackHandler,
                        SibsUtils.getPaymentStatusRetryer(1, 1));
        StrongAuthenticationState strongAuthenticationState = new StrongAuthenticationState(STATE);
        sibsPaymentExecutor =
                new SibsPaymentExecutor(
                        sibsBaseApiClient, signPaymentStrategy, strongAuthenticationState);
        paymentController = new PaymentController(sibsPaymentExecutor);
    }

    @SneakyThrows
    @Test
    public void shouldAcceptPaymentOnCreate() {
        // given
        mockCallFilterAndResponse(
                SibsPaymentInitiationResponse.class, SIBS_PAYMENT_INITIATION_RESPONSE_RECEIVED);

        // when
        PaymentResponse paymentResponse =
                paymentController.create(new PaymentRequest(createPayment()));

        // then
        assertThat(paymentResponse)
                .usingRecursiveComparison()
                .ignoringFields("payment.id")
                .isEqualTo(getExpectedPaymentResponseOnCreate());
    }

    @SneakyThrows
    @Test
    public void shouldCreatePaymentOnSignInit() {
        // given
        given(redirectCallbackHandler.handleRedirect(any(), any()))
                .willReturn(Optional.of(new HashMap<>()));

        // when
        PaymentMultiStepResponse paymentMultiStepResponse =
                paymentController.sign(
                        createPaymentMultiStepRequest(AuthenticationStepConstants.STEP_INIT));

        // then
        assertThat(paymentMultiStepResponse)
                .usingRecursiveComparison()
                .ignoringFields("payment.id")
                .isEqualTo(getExpectedResponseOnSignInitStep());
    }

    @SneakyThrows
    @Test
    public void shouldRespondCorrectlyOnPostSign() {
        // given
        mockCallFilterAndResponse(
                SibsGetPaymentStatusResponse.class, SIBS_PAYMENT_STATUS_RESPONSE_ACSC);

        // when
        PaymentMultiStepResponse paymentMultiStepResponse =
                paymentController.sign(
                        createPaymentMultiStepRequest(
                                SibsConstants.SibsSignSteps.SIBS_PAYMENT_POST_SIGN_STATE));

        // then
        assertThat(paymentMultiStepResponse.getPayment().getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @SneakyThrows
    @Test
    public void shouldThrowPaymentAuthorizationExceptionWhenNoCallback() {
        // when
        Throwable exception =
                Assertions.catchThrowable(
                        () ->
                                paymentController.sign(
                                        createPaymentMultiStepRequest(
                                                AuthenticationStepConstants.STEP_INIT)));

        // then
        assertThat(exception)
                .isInstanceOf(PaymentAuthorizationException.class)
                .hasMessage("SCA time-out.");
    }

    @Test
    @Parameters(method = "shouldThrowBankSideErrorOnCreateWhenBankReturnsSpecifiedStatusAndBody")
    public void shouldThrowBankSideErrorOnCreateWhen(
            int status, String body, Class<?> exceptionClass) {
        // given
        given(redirectCallbackHandler.handleRedirect(any(), any()))
                .willReturn(Optional.of(new HashMap<>()));
        given(callFilter.handle(any())).willReturn(response);
        given(response.getStatus()).willReturn(status);
        given(response.hasBody()).willReturn(true);
        given(response.getBody(String.class)).willReturn(body);

        // when
        Throwable exception =
                Assertions.catchThrowable(
                        () ->
                                sibsPaymentExecutor.create(
                                        createPaymentMultiStepRequest(
                                                AuthenticationStepConstants.STEP_INIT)));

        // expected
        assertThat(exception).isInstanceOf(exceptionClass);
    }

    @SneakyThrows
    @Test
    @Parameters(method = "specifiedTransactionStatusAndErrorThrown")
    public void shouldThrowCorrectErrorWhenNotPaid(
            SibsTransactionStatus status, Class<?> exceptionThrown) {
        // given
        SibsGetPaymentStatusResponse sibsGetPaymentStatusResponse =
                new SibsGetPaymentStatusResponse();
        sibsGetPaymentStatusResponse.setTransactionStatus(status);

        mockCallFilterAndResponse(SibsGetPaymentStatusResponse.class, sibsGetPaymentStatusResponse);

        // when
        Throwable exception =
                Assertions.catchThrowable(
                        () ->
                                paymentController.sign(
                                        createPaymentMultiStepRequest(
                                                SibsConstants.SibsSignSteps
                                                        .SIBS_PAYMENT_POST_SIGN_STATE)));

        // then
        assertThat(exception).isInstanceOf(exceptionThrown);
    }

    private <T> void mockCallFilterAndResponse(Class<T> bodyClass, T responseBody) {
        given(callFilter.handle(any())).willReturn(response);
        given(response.getStatus()).willReturn(200);
        given(response.getBody(bodyClass)).willReturn(responseBody);
    }

    private PaymentResponse getExpectedPaymentResponseOnCreate() {
        Payment payment = createPayment();
        payment.setStatus(PaymentStatus.PENDING);

        Storage storage = new Storage();
        storage.put(SibsConstants.Storage.STATE, "test_state");
        storage.put(
                SibsConstants.Storage.PAYMENT_REDIRECT_URI,
                "https://app.cgd.pt/cdoOBA/login.seam?ref=DUMMY_REF");
        storage.put(SibsConstants.Storage.PAYMENT_UPDATE_PSU_URI, "DUMMY_PSU_ID");

        return new PaymentResponse(payment, storage);
    }

    private PaymentMultiStepResponse getExpectedResponseOnSignInitStep() {
        Payment payment = createPayment();
        payment.setStatus(PaymentStatus.CREATED);

        return new PaymentMultiStepResponse(payment, "sibs_payment_post_sign_state");
    }

    private SibsBaseApiClient createAndConfigureBaseApiClient() {

        new SibsTinkApiClientConfigurator()
                .applyFilters(
                        client,
                        new SibsRetryFilterProperties(1, 1, 1),
                        new SibsRateLimitFilterProperties(1, 2, 1),
                        PROVIDER_NAME);
        client.addFilter(callFilter);

        SibsUserState userState = new SibsUserState(persistentStorage);
        return new SibsBaseApiClient(
                client,
                userState,
                ASPSP_CODE,
                IS_USER_PRESENT,
                USER_IP,
                DATE_TIME_SOURCE,
                agentConfiguration);
    }

    private PaymentMultiStepRequest createPaymentMultiStepRequest(String step) {
        Storage storage = new Storage();
        storage.put(
                SibsConstants.Storage.PAYMENT_REDIRECT_URI,
                "https://app.cgd.pt/cdoOBA/login.seam?ref=DUMMY_REF");
        storage.put(SibsConstants.Storage.PAYMENT_UPDATE_PSU_URI, "DUMMY_PSU_ID");
        storage.put(SibsConstants.Storage.STATE, STATE);

        return new PaymentMultiStepRequest(createPayment(), storage, step, Collections.emptyList());
    }

    private Payment createPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Example remittance information");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        return new Payment.Builder()
                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1.0))
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN,
                                        "PT88003506518731338795511",
                                        "accountTest1"),
                                "Jan Kovalsky"))
                .withDebtor(
                        new Debtor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN,
                                        "PT88003506518731338795511",
                                        "accountTest2")))
                .withCurrency("EUR")
                .withUniqueId("DUMMY_PAYMENT_ID")
                .withExecutionDate(LocalDate.now())
                .withRemittanceInformation(remittanceInformation)
                .build();
    }

    private static <T> T deserializeFromFile(String filePath, Class<T> clazz) {
        return SerializationUtils.deserializeFromString(Paths.get(filePath).toFile(), clazz);
    }

    @SuppressWarnings("unused")
    private Object[] shouldThrowBankSideErrorOnCreateWhenBankReturnsSpecifiedStatusAndBody() {
        return new Object[][] {
            {500, "", BankServiceException.class},
            {405, "", BankServiceException.class},
            {401, "", SessionException.class},
            {503, "", BankServiceException.class},
            {400, "BAD_REQUEST", BankServiceException.class},
            {429, "ACCESS_EXCEEDED", BankServiceException.class}
        };
    }

    @SuppressWarnings("unused")
    private Object[] specifiedTransactionStatusAndErrorThrown() {
        return new Object[][] {
            {SibsTransactionStatus.RCVD, PaymentAuthorizationTimeOutException.class},
            {SibsTransactionStatus.PDNG, PaymentAuthorizationTimeOutException.class},
            {SibsTransactionStatus.PATC, PaymentAuthorizationTimeOutException.class},
            {SibsTransactionStatus.RJC, PaymentRejectedException.class},
            {SibsTransactionStatus.RJCT, PaymentRejectedException.class},
            {SibsTransactionStatus.CANC, PaymentCancelledException.class}
        };
    }
}
