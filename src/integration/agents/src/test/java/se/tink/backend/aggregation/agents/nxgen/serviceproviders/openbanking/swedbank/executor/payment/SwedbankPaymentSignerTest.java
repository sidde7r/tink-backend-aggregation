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
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankTestHelper.createPaymentStatusResponseWith;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankTestHelper.createStrongAuthenticationStateMock;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.common.SwedbankOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums.SwedbankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentAuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

public class SwedbankPaymentSignerTest {
    private SwedbankPaymentSigner swedbankPaymentSigner;
    private SwedbankOpenBankingPaymentApiClient swedbankApiClient;
    private SwedbankBankIdSigner swedbankIdSigner;
    private StrongAuthenticationState strongAuthenticationState;

    @Before
    public void setUp() {
        swedbankApiClient = mock(SwedbankOpenBankingPaymentApiClient.class);
        swedbankIdSigner = mock(SwedbankBankIdSigner.class);
        strongAuthenticationState = createStrongAuthenticationStateMock();

        swedbankPaymentSigner =
                new SwedbankPaymentSigner(
                        swedbankApiClient, swedbankIdSigner, strongAuthenticationState);

        givenSelectedAuthenticationMethod();
    }

    @Test
    public void shouldAuthorizePaymentIfReadyToSign() {
        // given
        givenPaymentStatusIs(true);

        // when
        boolean authorisationResult = swedbankPaymentSigner.authorize(INSTRUCTION_ID);

        // then
        verify(swedbankIdSigner, times(1)).setAuthenticationResponse(any());
        assertTrue(authorisationResult);
    }

    @Test
    public void shouldNotAuthorizePaymentIfNotReadyToSign() {
        // given
        givenPaymentStatusIs(false);

        // when
        boolean authorisationResult = swedbankPaymentSigner.authorize(INSTRUCTION_ID);

        // then
        assertFalse(authorisationResult);
    }

    @Test
    public void shouldStartAuthorisationProcessForAPayment() {
        // given
        givenPaymentStatusIs(true);

        // when
        swedbankPaymentSigner.authorize(INSTRUCTION_ID);

        // then
        verify(swedbankApiClient, times(1))
                .startPaymentAuthorisation(
                        INSTRUCTION_ID,
                        SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS,
                        STRONG_AUTH_STATE,
                        false);
    }

    @Test
    public void shouldSpecifySCAMethodForAuthorisation() {
        // given
        givenPaymentStatusIs(true);

        // when
        swedbankPaymentSigner.authorize(INSTRUCTION_ID);

        // then
        verify(swedbankApiClient, times(1)).startPaymentAuthorization(anyString());
    }

    private void givenPaymentStatusIs(boolean readyToSign) {
        final PaymentStatusResponse paymentStatusResponse =
                createPaymentStatusResponseWith(readyToSign, "");

        when(swedbankApiClient.getPaymentStatus(
                        INSTRUCTION_ID, SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS))
                .thenReturn(paymentStatusResponse);
    }

    private void givenSelectedAuthenticationMethod() {
        final PaymentAuthorisationResponse paymentAuthorisationResponse =
                createPaymentAuthorisationResponse();

        when(swedbankApiClient.startPaymentAuthorisation(
                        INSTRUCTION_ID,
                        SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS,
                        STRONG_AUTH_STATE,
                        false))
                .thenReturn(paymentAuthorisationResponse);
    }
}
