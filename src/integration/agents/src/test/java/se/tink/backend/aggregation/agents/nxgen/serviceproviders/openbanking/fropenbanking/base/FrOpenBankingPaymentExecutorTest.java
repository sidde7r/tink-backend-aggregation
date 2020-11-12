package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentExecutor.PAYMENT_POST_SIGN_STATE;

import java.util.Collections;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.ConsentApprovalEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class FrOpenBankingPaymentExecutorTest {

    private static final String AUTHORIZATION_URL = "http://something.com/redirect?id=123";

    private FrOpenBankingPaymentExecutor paymentExecutor;
    private FrOpenBankingPaymentApiClient apiClient;
    private SessionStorage sessionStorage;
    private SupplementalInformationHelper supplementalInformationHelper;

    @Before
    public void setup() {
        apiClient = mock(FrOpenBankingPaymentApiClient.class);
        sessionStorage = mock(SessionStorage.class);
        StrongAuthenticationState strongAuthenticationState = mock(StrongAuthenticationState.class);
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);

        paymentExecutor =
                new FrOpenBankingPaymentExecutor(
                        apiClient,
                        "someUrl",
                        sessionStorage,
                        strongAuthenticationState,
                        supplementalInformationHelper);
    }

    @Test
    public void createShouldCallApiClientAndReturnPaymentResponse() {
        // given
        Iban sourceIban = new Iban.Builder().countryCode(CountryCode.FR).buildRandom();
        Iban destIban = new Iban.Builder().countryCode(CountryCode.FR).buildRandom();
        RemittanceInformation remittanceInformation = new RemittanceInformation();
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
                                .build());

        when(apiClient.createPayment(any()))
                .thenReturn(
                        new CreatePaymentResponse(
                                new LinksEntity(new ConsentApprovalEntity(AUTHORIZATION_URL))));

        // when
        PaymentResponse paymentResponse = paymentExecutor.create(paymentRequest);

        // then
        Assertions.assertThat(paymentResponse.getPayment().getStatus())
                .isEqualTo(PaymentStatus.CREATED);
        verify(sessionStorage, times(1))
                .put(FrOpenBankingPaymentExecutor.PAYMENT_AUTHORIZATION_URL, AUTHORIZATION_URL);
        verify(apiClient, times(1)).createPayment(any());
    }

    @Test
    public void signShouldOpenThirdPartyAppOnInit() throws PaymentException {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        AuthenticationStepConstants.STEP_INIT,
                        Collections.emptyList(),
                        Collections.emptyList());

        when(sessionStorage.get(FrOpenBankingPaymentExecutor.PAYMENT_AUTHORIZATION_URL))
                .thenReturn(AUTHORIZATION_URL);

        // when
        PaymentMultiStepResponse response = paymentExecutor.sign(paymentRequest);

        // then
        Assertions.assertThat(response.getStep()).isEqualTo(PAYMENT_POST_SIGN_STATE);
        verify(sessionStorage, times(1))
                .get(FrOpenBankingPaymentExecutor.PAYMENT_AUTHORIZATION_URL);
        verify(supplementalInformationHelper, times(1)).openThirdPartyApp(any());
    }

    @Test
    public void signShouldVerifyPaymentStatusOnPostSign() throws PaymentException {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        PAYMENT_POST_SIGN_STATE,
                        Collections.emptyList(),
                        Collections.emptyList());

        when(apiClient.getPayment(any()))
                .thenReturn(
                        new GetPaymentResponse(new PaymentEntity("ACSC", null, null, null, null)));

        // when
        PaymentMultiStepResponse response = paymentExecutor.sign(paymentRequest);

        // then
        Assertions.assertThat(response.getStep())
                .isEqualTo(AuthenticationStepConstants.STEP_FINALIZE);
        verify(apiClient, times(1)).getPayment(any());
    }

    @Test
    public void signShouldThrowExceptionIfPaymentIsPending() {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        PAYMENT_POST_SIGN_STATE,
                        Collections.emptyList(),
                        Collections.emptyList());

        when(apiClient.getPayment(any()))
                .thenReturn(
                        new GetPaymentResponse(new PaymentEntity("ACTC", null, null, null, null)));

        // when
        Throwable thrown = catchThrowable(() -> paymentExecutor.sign(paymentRequest));

        // then
        Assertions.assertThat(thrown).isInstanceOf(PaymentAuthenticationException.class);
        verify(apiClient, times(1)).getPayment(any());
    }

    @Test
    public void signShouldThrowExceptionIfPaymentIsRejected() {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        PAYMENT_POST_SIGN_STATE,
                        Collections.emptyList(),
                        Collections.emptyList());

        when(apiClient.getPayment(any()))
                .thenReturn(
                        new GetPaymentResponse(new PaymentEntity("RJCT", null, null, null, null)));

        // when
        Throwable thrown = catchThrowable(() -> paymentExecutor.sign(paymentRequest));

        // then
        Assertions.assertThat(thrown).isInstanceOf(PaymentRejectedException.class);
        verify(apiClient, times(1)).getPayment(any());
    }
}
