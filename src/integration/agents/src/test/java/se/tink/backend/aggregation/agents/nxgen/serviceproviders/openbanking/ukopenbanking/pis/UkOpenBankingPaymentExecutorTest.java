package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.AUTH_CODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.CONSENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createCredentials;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentConsentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentRequestForNotExecutedPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticScheduledPaymentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createOAuth2Token;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentMultiStepRequestFoAuthenticateStep;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentMultiStepRequestForExecutePaymentStep;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentResponseForConsent;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingTestValidator.validatePaymentResponsesForDomesticPaymentAreEqual;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingPisAuthFilterInstantiator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.ExecutorSignStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class UkOpenBankingPaymentExecutorTest {

    private UkOpenBankingPaymentExecutor ukOpenBankingPaymentExecutor;
    private UkOpenBankingPaymentApiClient apiClientMock;
    private UkOpenBankingPisAuthFilterInstantiator authFilterInstantiatorMock;

    @Before
    public void setUp() throws PaymentAuthorizationException {
        final Credentials credentialsMock = createCredentials();
        final UkOpenBankingPaymentAuthenticator paymentAuthenticatorMock =
                createPaymentAuthenticatorMock();
        apiClientMock = mock(UkOpenBankingPaymentApiClient.class);
        authFilterInstantiatorMock = mock(UkOpenBankingPisAuthFilterInstantiator.class);

        ukOpenBankingPaymentExecutor =
                new UkOpenBankingPaymentExecutor(
                        apiClientMock,
                        credentialsMock,
                        paymentAuthenticatorMock,
                        authFilterInstantiatorMock);
    }

    @Test
    public void shouldCreatePaymentConsent() throws PaymentException {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForNotExecutedPayment();
        final DomesticPaymentConsentResponse paymentConsentResponseMock =
                createDomesticPaymentConsentResponse();
        final OAuth2Token clientTokenMock = createOAuth2Token();

        when(apiClientMock.requestClientCredentials()).thenReturn(clientTokenMock);
        when(apiClientMock.createDomesticPaymentConsent(any(DomesticPaymentConsentRequest.class)))
                .thenReturn(paymentConsentResponseMock);

        // when
        final PaymentResponse returned = ukOpenBankingPaymentExecutor.create(paymentRequestMock);

        // then
        final PaymentResponse expected = createPaymentResponseForConsent();

        validatePaymentResponsesForDomesticPaymentAreEqual(returned, expected);
        verify(authFilterInstantiatorMock).instantiateAuthFilterWithClientToken();
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

        final DomesticScheduledPaymentResponse responseMock =
                createDomesticScheduledPaymentResponse();
        when(apiClientMock.executeDomesticScheduledPayment(
                        any(DomesticScheduledPaymentRequest.class)))
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
