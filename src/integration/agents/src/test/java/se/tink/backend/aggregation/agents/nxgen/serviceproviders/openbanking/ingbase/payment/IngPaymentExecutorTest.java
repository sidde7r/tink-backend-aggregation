package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.entities.PaymentsLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.entities.SimpleAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class IngPaymentExecutorTest {

    private static final String AUTHORIZATION_URL = "http://something.com/redirect?id=123";

    private IngPaymentExecutor paymentExecutor;
    private IngBaseApiClient apiClient;
    private SessionStorage sessionStorage;
    private SupplementalInformationHelper supplementalInformationHelper;
    private StrongAuthenticationState strongAuthenticationState;

    @Before
    public void setup() {
        apiClient = mock(IngBaseApiClient.class);
        sessionStorage = mock(SessionStorage.class);
        strongAuthenticationState = mock(StrongAuthenticationState.class);
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);

        paymentExecutor =
                new IngPaymentExecutor(
                        apiClient,
                        sessionStorage,
                        strongAuthenticationState,
                        supplementalInformationHelper);
    }

    @Test
    public void createPaymentRequestShoulReturnCorrectPaymentRequest() {
        // given
        Iban sourceIban = new Iban.Builder().countryCode(CountryCode.FR).buildRandom();
        Iban destIban = new Iban.Builder().countryCode(CountryCode.FR).buildRandom();
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("ReferenceToCreditor");
        PaymentRequest paymentRequest =
                new PaymentRequest(
                        new Payment.Builder()
                                .withCreditor(
                                        new Creditor(new IbanIdentifier(sourceIban.toString())))
                                .withDebtor(new Debtor(new IbanIdentifier(destIban.toString())))
                                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1))
                                .withCurrency("EUR")
                                .withRemittanceInformation(remittanceInformation)
                                .withUniqueId(UUID.randomUUID().toString())
                                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                                .withExecutionDate(LocalDate.of(2019, 4, 5))
                                .build());

        SimpleAccountEntity creditor =
                new SimpleAccountEntity(
                        paymentRequest.getPayment().getCreditor().getAccountNumber(),
                        paymentRequest.getPayment().getCurrency());
        SimpleAccountEntity debtor =
                new SimpleAccountEntity(
                        paymentRequest.getPayment().getDebtor().getAccountNumber(),
                        paymentRequest.getPayment().getCurrency());

        // when
        CreatePaymentRequest result = paymentExecutor.createPaymentRequest(paymentRequest);

        // then
        Assertions.assertThat(result.getCreditorAgent()).isEqualTo("INGBFRPP");
        Assertions.assertThat(result.getCreditorName()).isEqualTo("Payment Creditor");
        Assertions.assertThat(result.getCreditorAccount()).isEqualTo(creditor);
        Assertions.assertThat(result.getDebtorAccount()).isEqualTo(debtor);
        Assertions.assertThat(result.getInstructedAmount().getAmount()).isEqualTo("1.0");
        Assertions.assertThat(result.getInstructedAmount().getCurrency()).isEqualTo("EUR");
        Assertions.assertThat(result.getRemittanceInformationUnstructured())
                .isEqualTo("ReferenceToCreditor");
        Assertions.assertThat(result.getChargeBearer()).isEqualTo("SLEV");
        Assertions.assertThat(result.getLocalInstrumentCode()).isEqualTo(null);
        Assertions.assertThat(result.getServiceLevelCode()).isEqualTo("SEPA");
        Assertions.assertThat(result.getRequestedExecutionDate()).isEqualTo("2019-04-05");
    }

    @SneakyThrows
    @Test
    public void createShouldCallApiClientAndReturnPaymentResponse() {
        // given
        IbanIdentifier cred = new IbanIdentifier("FR383390733324Z58PF2RSRNB11");
        IbanIdentifier deb = new IbanIdentifier("FR385390733324Z58PF2RSRNB11");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("ReferenceToCreditor");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        PaymentRequest paymentRequest =
                new PaymentRequest(
                        new Payment.Builder()
                                .withCreditor(new Creditor(cred))
                                .withDebtor(new Debtor(deb))
                                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1))
                                .withCurrency("EUR")
                                .withRemittanceInformation(remittanceInformation)
                                .withUniqueId(UUID.randomUUID().toString())
                                .build());

        String paymentId = "paymentId";
        when(apiClient.createPayment(any()))
                .thenReturn(
                        new CreatePaymentResponse(
                                paymentId, new PaymentsLinksEntity(AUTHORIZATION_URL)));

        // when
        PaymentResponse paymentResponse = paymentExecutor.create(paymentRequest);

        // then
        Assertions.assertThat(paymentResponse.getPayment().getStatus())
                .isEqualTo(PaymentStatus.CREATED);
        Assertions.assertThat(paymentResponse.getPayment().getUniqueId()).isEqualTo(paymentId);
        Assertions.assertThat(paymentResponse.getPayment().getType()).isEqualTo(PaymentType.SEPA);
        Assertions.assertThat(paymentResponse.getPayment().getCurrency()).isEqualTo("EUR");
        Assertions.assertThat(
                        paymentResponse.getPayment().getExactCurrencyAmount().getDoubleValue())
                .isEqualTo(1.0);
        Assertions.assertThat(paymentResponse.getPayment().getCreditor().getAccountNumber())
                .isEqualTo("FR383390733324Z58PF2RSRNB11");
        Assertions.assertThat(
                        paymentResponse
                                .getPayment()
                                .getCreditor()
                                .getAccountIdentifier()
                                .getIdentifier())
                .isEqualTo("FR383390733324Z58PF2RSRNB11");
        Assertions.assertThat(
                        paymentResponse.getPayment().getCreditor().getAccountIdentifier().getType())
                .isEqualTo(AccountIdentifierType.IBAN);
        Assertions.assertThat(paymentResponse.getPayment().getDebtor()).isEqualTo(null);

        verify(sessionStorage).put(IngPaymentExecutor.PAYMENT_AUTHORIZATION_URL, AUTHORIZATION_URL);
        verify(apiClient).createPayment(any());
    }

    @Test
    public void signShouldOpenThirdPartyAppOnInitWithCallbackParams() throws PaymentException {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        AuthenticationStepConstants.STEP_INIT,
                        Collections.emptyList(),
                        Collections.emptyList());

        when(sessionStorage.get(IngPaymentExecutor.PAYMENT_AUTHORIZATION_URL))
                .thenReturn(AUTHORIZATION_URL);

        String supplementKey = "supplementKey";
        when(strongAuthenticationState.getSupplementalKey()).thenReturn(supplementKey);

        when(supplementalInformationHelper.waitForSupplementalInformation(
                        eq(supplementKey), eq(9L), eq(TimeUnit.MINUTES)))
                .thenReturn(Optional.of(new HashMap<>()));

        // when
        PaymentMultiStepResponse response = paymentExecutor.sign(paymentRequest);

        // then
        Assertions.assertThat(response.getStep()).isEqualTo(IngPaymentExecutor.VALIDATE_PAYMENT);
        verify(sessionStorage).get(IngPaymentExecutor.PAYMENT_AUTHORIZATION_URL);
        verify(supplementalInformationHelper).openThirdPartyApp(any());
    }

    @Test
    public void signShouldVerifyPaymentStatusOnPostSign() throws PaymentException {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        IngPaymentExecutor.VALIDATE_PAYMENT,
                        Collections.emptyList(),
                        Collections.emptyList());

        when(apiClient.getPayment(any())).thenReturn(new GetPaymentResponse("ACSC"));

        // when
        PaymentMultiStepResponse response = paymentExecutor.sign(paymentRequest);

        // then
        Assertions.assertThat(response.getStep())
                .isEqualTo(AuthenticationStepConstants.STEP_FINALIZE);
        verify(apiClient).getPayment(any());
    }

    @Test
    public void signShouldThrowExceptionIfPaymentIsPending() {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        IngPaymentExecutor.VALIDATE_PAYMENT,
                        Collections.emptyList(),
                        Collections.emptyList());

        when(apiClient.getPayment(any())).thenReturn(new GetPaymentResponse("RCVD"));

        // when
        Throwable thrown = catchThrowable(() -> paymentExecutor.sign(paymentRequest));

        Assertions.assertThat(thrown).isInstanceOf(PaymentRejectedException.class);
        verify(apiClient).getPayment(any());
    }

    @Test
    public void signShouldThrowExceptionIfPaymentIsRejected() {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        IngPaymentExecutor.VALIDATE_PAYMENT,
                        Collections.emptyList(),
                        Collections.emptyList());

        when(apiClient.getPayment(any())).thenReturn(new GetPaymentResponse("RCVD"));

        // when
        Throwable thrown = catchThrowable(() -> paymentExecutor.sign(paymentRequest));

        Assertions.assertThat(thrown).isInstanceOf(PaymentRejectedException.class);
        verify(apiClient).getPayment(any());
    }
}
