package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankTestHelper.INSTRUCTION_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankTestHelper.STRONG_AUTH_STATE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankTestHelper.createPaymentAuthorisationResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankTestHelper.createPaymentMultiStepRequest;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankTestHelper.createPaymentStatusResponseWith;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankTestHelper.createStrongAuthenticationStateMock;
import static se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants.STEP_SIGN;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.common.SwedbankOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums.SwedbankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentAuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigningController;

public class SwedbankPaymentSignerTest {
    private SwedbankPaymentSigner swedbankPaymentSigner;
    private SwedbankOpenBankingPaymentApiClient swedbankApiClient;
    private SwedbankBankIdSigner swedbankIdSigner;
    private BankIdSigningController<PaymentMultiStepRequest> bankIdSigningController;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws PaymentException {
        swedbankApiClient = mock(SwedbankOpenBankingPaymentApiClient.class);
        swedbankIdSigner = mock(SwedbankBankIdSigner.class);
        StrongAuthenticationState strongAuthenticationState = createStrongAuthenticationStateMock();
        bankIdSigningController = mock(BankIdSigningController.class);

        swedbankPaymentSigner =
                new SwedbankPaymentSigner(
                        swedbankApiClient,
                        swedbankIdSigner,
                        strongAuthenticationState,
                        bankIdSigningController);

        givenSelectedAuthenticationMethodWithRedirect(false);
    }

    @Test
    public void authorizeShouldAuthorizePaymentIfReadyToSign() throws PaymentException {
        // given
        givenPaymentIs(true);

        // when
        boolean authorisationResult = swedbankPaymentSigner.authorize(INSTRUCTION_ID);

        // then
        verify(swedbankIdSigner, times(1)).setAuthenticationResponse(any());
        assertTrue(authorisationResult);
    }

    @Test
    public void authorizeShouldNotAuthorizePaymentIfNotReadyToSign() throws PaymentException {
        // given
        givenPaymentIs(false);

        // when
        boolean authorisationResult = swedbankPaymentSigner.authorize(INSTRUCTION_ID);

        // then
        assertFalse(authorisationResult);
    }

    @Test
    public void authorizeShouldStartAuthorisationProcessForAPayment() throws PaymentException {
        // given
        givenPaymentIs(true);

        // when
        swedbankPaymentSigner.authorize(INSTRUCTION_ID);

        // then
        verify(swedbankApiClient, times(1))
                .initiatePaymentAuthorisation(
                        INSTRUCTION_ID,
                        SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS,
                        STRONG_AUTH_STATE,
                        false);
    }

    @Test
    public void authorizeShouldSpecifySCAMethodForAuthorisation() throws PaymentException {
        // given
        givenPaymentIs(true);

        // when
        swedbankPaymentSigner.authorize(INSTRUCTION_ID);

        // then
        verify(swedbankApiClient, times(1)).startPaymentAuthorization(anyString());
    }

    @Test
    public void signShouldSignPayment() {
        // given
        final PaymentMultiStepRequest request = createPaymentMultiStepRequest(STEP_SIGN);

        // when
        swedbankPaymentSigner.sign(request);

        // then
        verify(bankIdSigningController, times(1)).sign(request);
    }

    private void givenPaymentIs(boolean readyToSign) throws PaymentException {
        final PaymentStatusResponse paymentStatusResponse =
                createPaymentStatusResponseWith(readyToSign, "");

        when(swedbankApiClient.getPaymentStatus(
                        INSTRUCTION_ID, SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS))
                .thenReturn(paymentStatusResponse);
    }

    private void givenSelectedAuthenticationMethodWithRedirect(boolean redirect)
            throws PaymentException {
        final PaymentAuthorisationResponse paymentAuthorisationResponse =
                createPaymentAuthorisationResponse();

        when(swedbankApiClient.initiatePaymentAuthorisation(
                        INSTRUCTION_ID,
                        SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS,
                        STRONG_AUTH_STATE,
                        redirect))
                .thenReturn(paymentAuthorisationResponse);
    }
}
