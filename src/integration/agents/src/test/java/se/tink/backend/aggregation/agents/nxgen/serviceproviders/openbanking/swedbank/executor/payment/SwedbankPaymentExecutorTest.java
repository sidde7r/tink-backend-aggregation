package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankTestHelper.createPaymentAuthorisationResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankTestHelper.createPaymentMultiStepRequest;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankTestHelper.createPaymentRequest;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankTestHelper.createPaymentStatusResponseWith;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankTestHelper.createStrongAuthenticationStateMock;
import static se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants.STEP_FINALIZE;
import static se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants.STEP_INIT;
import static se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants.STEP_SIGN;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.SwedbankPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.common.SwedbankOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums.SwedbankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentAuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigningController;

public class SwedbankPaymentExecutorTest {

    private SwedbankPaymentExecutor swedbankPaymentExecutor;
    private SwedbankOpenBankingPaymentApiClient swedbankApiClient;
    private SwedbankPaymentAuthenticator swedbankPaymentAuthenticator;
    private SwedbankBankIdSigner swedbankIdSigner;
    private BankIdSigningController<PaymentMultiStepRequest> bankIdSigningController;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws PaymentAuthorizationException {
        swedbankApiClient = mock(SwedbankOpenBankingPaymentApiClient.class);
        swedbankPaymentAuthenticator = mock(SwedbankPaymentAuthenticator.class);
        swedbankIdSigner = mock(SwedbankBankIdSigner.class);
        bankIdSigningController = mock(BankIdSigningController.class);

        swedbankPaymentExecutor =
                new SwedbankPaymentExecutor(
                        swedbankApiClient,
                        swedbankPaymentAuthenticator,
                        createStrongAuthenticationStateMock(),
                        swedbankIdSigner,
                        bankIdSigningController);

        givenPaymentAuthorization();
    }

    @Test
    public void shouldStayInInitStateIfNotReadyForSigning() {
        // given
        final PaymentMultiStepRequest request = createPaymentRequest();
        givenPaymentStatusIs(false, "");

        // when
        final PaymentMultiStepResponse result = swedbankPaymentExecutor.sign(request);

        // then
        assertThat(result.getStep(), is(SigningStepConstants.STEP_INIT));
    }

    @Test
    public void shouldAuthorizePayment() {
        // given
        final PaymentMultiStepRequest request = createPaymentRequest();
        givenPaymentStatusIs(true, "");

        // when
        final PaymentMultiStepResponse result = swedbankPaymentExecutor.sign(request);

        // then
        assertThat(result.getStep(), is(STEP_SIGN));
        verify(swedbankIdSigner, times(1)).setAuthenticationResponse(any());
    }

    @Test
    public void shouldFallBackToRedirectFlowIfMissingExtendedBankId() {
        // given
        final PaymentMultiStepRequest request = createPaymentMultiStepRequest(STEP_SIGN);

        givenPaymentStatusIs(true, "");
        givenMissingExtendedBankId();

        // when
        final PaymentMultiStepResponse result = swedbankPaymentExecutor.sign(request);

        // then
        assertThat(result.getStep(), is(STEP_FINALIZE));
        verify(swedbankPaymentAuthenticator, times(1)).openThirdPartyApp(any(), any());
    }

    @Test
    public void shouldTryToSignPayment() {
        // given
        final PaymentMultiStepRequest request = createPaymentMultiStepRequest(STEP_SIGN);
        givenPaymentStatusIs(true, "");

        // when
        final PaymentMultiStepResponse result = swedbankPaymentExecutor.sign(request);

        // then
        assertThat(result.getStep(), is(STEP_FINALIZE));
        verify(bankIdSigningController, times(1)).sign(request);
    }

    @Test
    public void shouldReturnToInitStepIfPaymentIsPending() {
        // given
        final PaymentMultiStepRequest request = createPaymentMultiStepRequest(STEP_SIGN);
        givenPaymentStatusIs(true, "ACTC");

        // when
        final PaymentMultiStepResponse result = swedbankPaymentExecutor.sign(request);

        // then
        assertThat(result.getStep(), is(STEP_INIT));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateException() {
        // given
        final PaymentMultiStepRequest request = createPaymentMultiStepRequest("INVALID_STEP");

        // when
        swedbankPaymentExecutor.sign(request);

        // then - assert in annotation
    }

    private void givenPaymentStatusIs(boolean readyToSign, String status) {
        final PaymentStatusResponse paymentStatusResponse =
                createPaymentStatusResponseWith(readyToSign, status);

        when(swedbankApiClient.getPaymentStatus(
                        SwedbankTestHelper.INSTRUCTION_ID,
                        SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS))
                .thenReturn(paymentStatusResponse);
    }

    private void givenMissingExtendedBankId() {
        when(swedbankIdSigner.isMissingExtendedBankId()).thenReturn(true);
    }

    private void givenPaymentAuthorization() {
        final PaymentAuthorisationResponse paymentAuthorisationResponse =
                createPaymentAuthorisationResponse();

        when(swedbankApiClient.startPaymentAuthorisation(
                        anyString(), any(), anyString(), anyBoolean()))
                .thenReturn(paymentAuthorisationResponse);
    }
}
