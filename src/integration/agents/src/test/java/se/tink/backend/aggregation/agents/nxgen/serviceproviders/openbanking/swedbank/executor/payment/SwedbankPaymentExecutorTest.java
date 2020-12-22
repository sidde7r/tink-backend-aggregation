package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankTestHelper.INSTRUCTION_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankTestHelper.createPaymentMultiStepRequest;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankTestHelper.createPaymentRequest;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankTestHelper.createPaymentStatusResponseWith;
import static se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants.STEP_FINALIZE;
import static se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants.STEP_INIT;
import static se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants.STEP_SIGN;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.common.SwedbankOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankPaymentSigner.MissingExtendedBankIdException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums.SwedbankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;

public class SwedbankPaymentExecutorTest {

    private SwedbankPaymentExecutor swedbankPaymentExecutor;
    private SwedbankOpenBankingPaymentApiClient swedbankApiClient;
    private SwedbankPaymentSigner swedbankPaymentSigner;

    @Before
    public void setUp() throws PaymentAuthorizationException {
        swedbankApiClient = mock(SwedbankOpenBankingPaymentApiClient.class);
        swedbankPaymentSigner = mock(SwedbankPaymentSigner.class);
        swedbankPaymentExecutor =
                new SwedbankPaymentExecutor(swedbankApiClient, swedbankPaymentSigner);
    }

    @Test
    public void signShouldStayInInitStateIfAuthorizeProcessNotStarted() {
        // given
        final PaymentMultiStepRequest request = createPaymentRequest();
        givenPaymentAuthorisationWas(false);

        // when
        final PaymentMultiStepResponse result = swedbankPaymentExecutor.sign(request);

        // then
        assertThat(result.getStep(), is(SigningStepConstants.STEP_INIT));
    }

    @Test
    public void signShouldGoToSIGNStateIfAuthorizeProcessIsSuccessful() {
        // given
        final PaymentMultiStepRequest request = createPaymentRequest();
        givenPaymentAuthorisationWas(true);

        // when
        final PaymentMultiStepResponse result = swedbankPaymentExecutor.sign(request);

        // then
        assertThat(result.getStep(), is(STEP_SIGN));
        verify(swedbankPaymentSigner, times(1)).authorize(INSTRUCTION_ID);
    }

    private void givenPaymentAuthorisationWas(boolean success) {
        when(swedbankPaymentSigner.authorize(INSTRUCTION_ID)).thenReturn(success);
    }

    @Test
    public void signShouldFallBackToRedirectFlowIfMissingExtendedBankId() {
        // given
        final PaymentMultiStepRequest request = createPaymentMultiStepRequest(STEP_SIGN);
        givenPaymentWithStatus("");
        givenMissingExtendedBankId();

        // when
        final PaymentMultiStepResponse result = swedbankPaymentExecutor.sign(request);

        // then
        assertThat(result.getStep(), is(STEP_FINALIZE));
        verify(swedbankPaymentSigner, times(1)).signWithRedirect(INSTRUCTION_ID);
    }

    @Test
    public void signShouldTryToSignPayment() {
        // given
        final PaymentMultiStepRequest request = createPaymentMultiStepRequest(STEP_SIGN);
        givenPaymentWithStatus("");

        // when
        final PaymentMultiStepResponse result = swedbankPaymentExecutor.sign(request);

        // then
        assertThat(result.getStep(), is(STEP_FINALIZE));
        verify(swedbankPaymentSigner, times(1)).sign(request);
    }

    @Test
    public void signShouldReturnToInitStepIfPaymentIsPending() {
        // given
        final PaymentMultiStepRequest request = createPaymentMultiStepRequest(STEP_SIGN);
        givenPaymentWithStatus("ACTC");

        // when
        final PaymentMultiStepResponse result = swedbankPaymentExecutor.sign(request);

        // then
        assertThat(result.getStep(), is(STEP_INIT));
    }

    @Test(expected = IllegalStateException.class)
    public void signShouldThrowIllegalStateException() {
        // given
        final PaymentMultiStepRequest request = createPaymentMultiStepRequest("INVALID_STEP");

        // when
        swedbankPaymentExecutor.sign(request);

        // then - assert in annotation
    }

    private void givenPaymentWithStatus(String status) {
        final PaymentStatusResponse paymentStatusResponse =
                createPaymentStatusResponseWith(true, status);

        when(swedbankApiClient.getPaymentStatus(
                        SwedbankTestHelper.INSTRUCTION_ID,
                        SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS))
                .thenReturn(paymentStatusResponse);
    }

    private void givenMissingExtendedBankId() {
        doThrow(MissingExtendedBankIdException.class)
                .when(swedbankPaymentSigner)
                .sign(any(PaymentMultiStepRequest.class));
    }
}
