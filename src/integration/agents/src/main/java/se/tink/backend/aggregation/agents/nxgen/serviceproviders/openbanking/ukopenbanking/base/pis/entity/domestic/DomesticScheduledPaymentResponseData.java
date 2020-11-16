package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.domestic;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Data
public class DomesticScheduledPaymentResponseData {

    private String domesticScheduledPaymentId;
    private String consentId;
    private DomesticScheduledPaymentInitiation initiation;
    private String status;

    public PaymentResponse toTinkPaymentResponse() {
        final RemittanceInformation transferRemittanceInformation = new RemittanceInformation();
        transferRemittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transferRemittanceInformation.setValue(
                initiation.getRemittanceInformation().getUnstructured());

        final Payment payment =
                new Payment.Builder()
                        .withRemittanceInformation(transferRemittanceInformation)
                        .withCreditor(initiation.getCreditorAccount().toCreditor())
                        .withExactCurrencyAmount(initiation.getInstructedAmount().toTinkAmount())
                        .withDebtor(getDebtor(initiation))
                        .withStatus(
                                UkOpenBankingV31Constants.scheduledPaymentStatusToPaymentStatus(
                                        status))
                        .withExecutionDate(getDate(initiation))
                        .build();

        Storage storage = new Storage();
        storage.put(UkOpenBankingV31Constants.Storage.CONSENT_ID, consentId);
        storage.put(UkOpenBankingV31Constants.Storage.PAYMENT_ID, domesticScheduledPaymentId);

        return new PaymentResponse(payment, storage);
    }

    private static Debtor getDebtor(DomesticScheduledPaymentInitiation initiation) {
        return Objects.isNull(initiation.getDebtorAccount())
                ? null
                : initiation.getDebtorAccount().toDebtor();
    }

    private static LocalDate getDate(DomesticScheduledPaymentInitiation initiation) {
        return LocalDate.parse(initiation.getRequestedExecutionDateTime(), ISO_OFFSET_DATE_TIME);
    }
}
