package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.rpc.domestic.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.rpc.domestic.DomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.rpc.domestic.DomesticScheduledPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.rpc.domestic.DomesticScheduledPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.rpc.international.FundsConfirmationResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.rpc.Payment;

public class UkOpenbankingPaymentTestFixtures {

    static final String PAYMENT_ID = "DUMMY_PAYMENT_ID";
    static final String CONSENT_ID = "DUMMY_CONSENT_ID";
    static final String END_TO_END_ID = "DUMMY_END_TO_END_ID";
    static final String INSTRUCTION_ID = "DUMMY_INSTRUCTION_ID";

    static PaymentRequest createDomesticPaymentRequestForNotExecutedPayment(Clock clockMock) {
        final PaymentRequest paymentRequestMock = mock(PaymentRequest.class);
        final Payment paymentMock = createTodayPayment(clockMock);

        when(paymentRequestMock.getPayment()).thenReturn(paymentMock);
        setConsentId(paymentRequestMock);

        return paymentRequestMock;
    }

    static PaymentRequest createDomesticPaymentRequestForAlreadyExecutedPayment(Clock clockMock) {
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForNotExecutedPayment(clockMock);

        setPaymentId(paymentRequestMock);

        return paymentRequestMock;
    }

    static PaymentRequest createDomesticScheduledPaymentRequestForNotExecutedPayment(
            Clock clockMock) {
        final PaymentRequest paymentRequestMock = mock(PaymentRequest.class);
        final Payment paymentMock = createFutureDatePayment(clockMock);

        when(paymentRequestMock.getPayment()).thenReturn(paymentMock);
        setConsentId(paymentRequestMock);

        return paymentRequestMock;
    }

    static PaymentRequest createDomesticScheduledPaymentRequestForAlreadyExecutedPayment(
            Clock clockMock) {
        final PaymentRequest paymentRequestMock =
                createDomesticScheduledPaymentRequestForNotExecutedPayment(clockMock);

        setPaymentId(paymentRequestMock);

        return paymentRequestMock;
    }

    static FundsConfirmationResponse createFundsConfirmationResponse() {
        return mock(FundsConfirmationResponse.class);
    }

    static PaymentResponse createPaymentResponse() {
        return mock(PaymentResponse.class);
    }

    static DomesticPaymentResponse createDomesticPaymentResponse(PaymentResponse paymentResponse) {
        final DomesticPaymentResponse domesticPaymentResponse = mock(DomesticPaymentResponse.class);

        when(domesticPaymentResponse.toTinkPaymentResponse()).thenReturn(paymentResponse);

        return domesticPaymentResponse;
    }

    static DomesticPaymentConsentResponse createDomesticPaymentConsentResponse(
            PaymentResponse paymentResponse) {
        final DomesticPaymentConsentResponse domesticPaymentConsentResponse =
                mock(DomesticPaymentConsentResponse.class);

        when(domesticPaymentConsentResponse.toTinkPaymentResponse()).thenReturn(paymentResponse);

        return domesticPaymentConsentResponse;
    }

    static DomesticScheduledPaymentResponse createDomesticScheduledPaymentResponse(
            PaymentResponse paymentResponse) {
        final DomesticScheduledPaymentResponse domesticScheduledPaymentResponse =
                mock(DomesticScheduledPaymentResponse.class);

        when(domesticScheduledPaymentResponse.toTinkPaymentResponse()).thenReturn(paymentResponse);

        return domesticScheduledPaymentResponse;
    }

    static DomesticScheduledPaymentConsentResponse createDomesticScheduledPaymentConsentResponse(
            PaymentResponse paymentResponse) {
        final DomesticScheduledPaymentConsentResponse domesticScheduledPaymentConsentResponse =
                mock(DomesticScheduledPaymentConsentResponse.class);

        when(domesticScheduledPaymentConsentResponse.toTinkPaymentResponse())
                .thenReturn(paymentResponse);

        return domesticScheduledPaymentConsentResponse;
    }

    private static Payment createTodayPayment(Clock clockMock) {
        final Payment paymentMock = mock(Payment.class);

        when(paymentMock.getCreditorAndDebtorAccountType())
                .thenReturn(
                        new Pair<>(
                                AccountIdentifier.Type.SORT_CODE,
                                AccountIdentifier.Type.SORT_CODE));

        final LocalDate executionDate = LocalDate.now(clockMock);
        when(paymentMock.getExecutionDate()).thenReturn(executionDate);

        return paymentMock;
    }

    private static Payment createFutureDatePayment(Clock clockMock) {
        final Payment paymentMock = mock(Payment.class);

        when(paymentMock.getCreditorAndDebtorAccountType())
                .thenReturn(
                        new Pair<>(
                                AccountIdentifier.Type.SORT_CODE,
                                AccountIdentifier.Type.SORT_CODE));

        final LocalDate executionDate = LocalDate.now(clockMock).plusDays(1L);
        when(paymentMock.getExecutionDate()).thenReturn(executionDate);

        return paymentMock;
    }

    private static void setPaymentId(PaymentRequest paymentRequestMock) {
        final Storage storageMock = mock(Storage.class);

        when(storageMock.get(UkOpenBankingV31Constants.Storage.PAYMENT_ID)).thenReturn(PAYMENT_ID);
        when(paymentRequestMock.getStorage()).thenReturn(storageMock);
    }

    private static void setConsentId(PaymentRequest paymentRequestMock) {
        final Storage storageMock = mock(Storage.class);

        when(storageMock.get(UkOpenBankingV31Constants.Storage.CONSENT_ID)).thenReturn(CONSENT_ID);
        when(paymentRequestMock.getStorage()).thenReturn(storageMock);
    }
}
