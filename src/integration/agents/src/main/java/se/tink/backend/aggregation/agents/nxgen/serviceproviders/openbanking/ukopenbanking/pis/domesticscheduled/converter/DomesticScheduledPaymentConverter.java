package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.converter;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

import java.time.LocalDate;
import java.time.ZoneOffset;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingV31PaymentConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.converter.PaymentConverterBase;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentConsentResponseData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentInitiation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentResponseData;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DomesticScheduledPaymentConverter extends PaymentConverterBase {

    private static final TypeMapper<PaymentStatus> SCHEDULED_PAYMENT_STATUS_MAPPER =
            TypeMapper.<PaymentStatus>builder()
                    .put(PaymentStatus.PENDING, "InitiationPending")
                    .put(PaymentStatus.SIGNED, "InitiationCompleted")
                    .put(PaymentStatus.REJECTED, "InitiationFailed")
                    .put(PaymentStatus.CANCELLED, "Cancelled")
                    .build();

    public String getRequestedExecutionDateTime(Payment payment) {
        return ISO_OFFSET_DATE_TIME.format(payment.getExecutionDate().atStartOfDay(ZoneOffset.UTC));
    }

    public PaymentResponse convertConsentResponseDtoToTinkPaymentResponse(
            DomesticScheduledPaymentConsentResponse response) {
        final DomesticScheduledPaymentConsentResponseData responseData = response.getData();
        final DomesticScheduledPaymentInitiation initiation = responseData.getInitiation();
        final PaymentStatus paymentStatus =
                convertResponseStatusToPaymentStatus(responseData.getStatus());

        final Payment payment = createPayment(initiation, paymentStatus);

        final Storage storage = new Storage();
        storage.put(
                UkOpenBankingV31PaymentConstants.Storage.CONSENT_ID, responseData.getConsentId());

        return new PaymentResponse(payment, storage);
    }

    public PaymentResponse convertResponseDtoToPaymentResponse(
            DomesticScheduledPaymentResponse response) {
        final DomesticScheduledPaymentResponseData responseData = response.getData();
        final DomesticScheduledPaymentInitiation initiation = responseData.getInitiation();
        final PaymentStatus paymentStatus =
                convertScheduledPaymentResponseStatusToPaymentStatus(responseData.getStatus());

        final Payment payment = createPayment(initiation, paymentStatus);

        final Storage storage = new Storage();
        storage.put(
                UkOpenBankingV31PaymentConstants.Storage.CONSENT_ID, responseData.getConsentId());
        storage.put(
                UkOpenBankingV31PaymentConstants.Storage.PAYMENT_ID,
                responseData.getDomesticScheduledPaymentId());

        return new PaymentResponse(payment, storage);
    }

    private Payment createPayment(
            DomesticScheduledPaymentInitiation initiation, PaymentStatus paymentStatus) {
        final RemittanceInformation remittanceInformation =
                createUnstructuredRemittanceInformation(
                        initiation.getRemittanceInformation().getUnstructured());
        final ExactCurrencyAmount amount =
                convertInstructedAmountToExactCurrencyAmount(initiation.getInstructedAmount());

        return new Payment.Builder()
                .withExactCurrencyAmount(amount)
                .withStatus(paymentStatus)
                .withDebtor(convertDebtorAccountToDebtor(initiation.getDebtorAccount()))
                .withCreditor(convertCreditorAccountToCreditor(initiation.getCreditorAccount()))
                .withCurrency(amount.getCurrencyCode())
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId(initiation.getInstructionIdentification())
                .withExecutionDate(getDate(initiation))
                .build();
    }

    private static LocalDate getDate(DomesticScheduledPaymentInitiation initiation) {
        return LocalDate.parse(initiation.getRequestedExecutionDateTime(), ISO_OFFSET_DATE_TIME);
    }

    private static PaymentStatus convertScheduledPaymentResponseStatusToPaymentStatus(
            String scheduledPaymentStatus) {
        return SCHEDULED_PAYMENT_STATUS_MAPPER
                .translate(scheduledPaymentStatus)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                "%s unknown payment status!",
                                                scheduledPaymentStatus)));
    }
}
