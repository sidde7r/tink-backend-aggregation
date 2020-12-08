package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.AUTH_CODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.CONSENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.INSTRUCTION_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.PAYMENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createClockMock;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentRequestForAlreadyExecutedPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentRequestForNotExecutedPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentMultiStepRequestFoAuthenticateStep;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentMultiStepRequestForExecutePaymentStep;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentResponseForConsent;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingTestValidator.validatePaymentResponsesForDomesticPaymentAreEqual;

import java.time.Clock;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingPisAuthFilterInstantiator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.UkOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.ExecutorSignStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.validator.UkOpenBankingPaymentRequestValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

public class UkOpenBankingPaymentExecutorTest {

    private UkOpenBankingPaymentExecutor ukOpenBankingPaymentExecutor;
    private UkOpenBankingPaymentApiClient apiClientMock;
    private UkOpenBankingPisAuthFilterInstantiator authFilterInstantiatorMock;
    private Clock clockMock;
    private UkOpenBankingPaymentRequestValidator paymentRequestValidatorMock;

    @Before
    public void setUp() throws PaymentAuthorizationException {
        final UkOpenBankingPaymentAuthenticator paymentAuthenticatorMock =
                createPaymentAuthenticatorMock();
        apiClientMock = mock(UkOpenBankingPaymentApiClient.class);
        authFilterInstantiatorMock = mock(UkOpenBankingPisAuthFilterInstantiator.class);
        paymentRequestValidatorMock = mock(UkOpenBankingPaymentRequestValidator.class);

        ukOpenBankingPaymentExecutor =
                new UkOpenBankingPaymentExecutor(
                        apiClientMock,
                        paymentAuthenticatorMock,
                        authFilterInstantiatorMock,
                        paymentRequestValidatorMock);

        clockMock = createClockMock();
    }

    @Test
    public void shouldCreatePaymentConsent() throws PaymentException {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForNotExecutedPayment();
        final PaymentResponse paymentConsentResponseMock = createPaymentResponseForConsent();

        when(apiClientMock.createPaymentConsent(paymentRequestMock))
                .thenReturn(paymentConsentResponseMock);

        // when
        final PaymentResponse returned = ukOpenBankingPaymentExecutor.create(paymentRequestMock);

        // then
        final PaymentResponse expected = createPaymentResponseForConsent();

        validatePaymentResponsesForDomesticPaymentAreEqual(returned, expected);

        verify(apiClientMock).createPaymentConsent(any(PaymentRequest.class));
        verify(paymentRequestValidatorMock).validate(paymentRequestMock);
        verify(authFilterInstantiatorMock).instantiateAuthFilterWithClientToken();
    }

    @Test
    public void shouldFetchPaymentIfPresent() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForAlreadyExecutedPayment(this.clockMock);
        final PaymentResponse paymentResponseMock = createPaymentResponse();

        when(apiClientMock.getPayment(PAYMENT_ID)).thenReturn(paymentResponseMock);

        // when
        final PaymentResponse returnedResponse =
                ukOpenBankingPaymentExecutor.fetch(paymentRequestMock);

        // then
        assertThat(returnedResponse).isEqualTo(paymentResponseMock);
        verify(apiClientMock).getPayment(PAYMENT_ID);
        verify(apiClientMock, times(0)).getPaymentConsent(CONSENT_ID);
    }

    @Test
    public void shouldFetchPaymentConsentIfPaymentNotPresent() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForAlreadyExecutedPayment(this.clockMock);
        final PaymentResponse paymentResponseMock = createPaymentResponse();

        when(apiClientMock.getPaymentConsent(CONSENT_ID)).thenReturn(paymentResponseMock);

        // when
        final PaymentResponse returnedResponse =
                ukOpenBankingPaymentExecutor.fetch(paymentRequestMock);

        // then
        assertThat(returnedResponse).isEqualTo(paymentResponseMock);
        verify(apiClientMock).getPayment(PAYMENT_ID);
        verify(apiClientMock).getPaymentConsent(CONSENT_ID);
    }

    @Test
    public void shouldRunAuthenticateStep() throws PaymentException {
        // given
        final PaymentMultiStepRequest request = createPaymentMultiStepRequestFoAuthenticateStep();

        // when
        final PaymentMultiStepResponse returned = ukOpenBankingPaymentExecutor.sign(request);

        // then
        assertThat(returned.getStep()).isEqualTo(ExecutorSignStep.EXECUTE_PAYMENT.name());
        verify(authFilterInstantiatorMock).instantiateAuthFilterWithAccessToken(AUTH_CODE);
    }

    @Test
    public void shouldRunExecutePaymentStep() throws PaymentException {
        // given
        final PaymentMultiStepRequest request =
                createPaymentMultiStepRequestForExecutePaymentStep();

        final PaymentResponse responseMock = createPaymentResponse();
        when(apiClientMock.executePayment(request, CONSENT_ID, INSTRUCTION_ID, INSTRUCTION_ID))
                .thenReturn(responseMock);

        // when
        final PaymentMultiStepResponse returned = ukOpenBankingPaymentExecutor.sign(request);

        // then
        assertThat(returned.getStep()).isEqualTo(AuthenticationStepConstants.STEP_FINALIZE);
    }

    private static UkOpenBankingPaymentAuthenticator createPaymentAuthenticatorMock()
            throws PaymentAuthorizationException {
        final UkOpenBankingPaymentAuthenticator paymentAuthenticatorMock =
                mock(UkOpenBankingPaymentAuthenticator.class);

        when(paymentAuthenticatorMock.authenticate(CONSENT_ID)).thenReturn(AUTH_CODE);

        return paymentAuthenticatorMock;
    }
}
