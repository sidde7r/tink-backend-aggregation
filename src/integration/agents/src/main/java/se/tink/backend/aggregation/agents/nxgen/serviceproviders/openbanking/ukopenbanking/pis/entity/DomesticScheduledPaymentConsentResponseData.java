package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingV31PaymentConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Data
public class DomesticScheduledPaymentConsentResponseData {

    private String status;
    private String statusUpdateDateTime;
    private String creationDateTime;
    private String consentId;
    private String permission;
    private String readRefundAccount;
    private DomesticScheduledPaymentInitiation initiation;

    private static Debtor getDebtor(DomesticScheduledPaymentInitiation initiation) {
        return Objects.isNull(initiation.getDebtorAccount())
                ? null
                : initiation.getDebtorAccount().toDebtor();
    }

    private static LocalDate getDate(DomesticScheduledPaymentInitiation initiation) {
        return LocalDate.parse(initiation.getRequestedExecutionDateTime(), ISO_OFFSET_DATE_TIME);
    }

    public PaymentResponse toTinkPaymentResponse() {
        final RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue(initiation.getRemittanceInformation().getUnstructured());

        final ExactCurrencyAmount amount = initiation.getInstructedAmount().toTinkAmount();

        final Payment payment =
                new Payment.Builder()
                        .withExactCurrencyAmount(amount)
                        .withStatus(UkOpenBankingV31PaymentConstants.toPaymentStatus(status))
                        .withDebtor(getDebtor(initiation))
                        .withCreditor(initiation.getCreditorAccount().toCreditor())
                        .withCurrency(amount.getCurrencyCode())
                        .withRemittanceInformation(remittanceInformation)
                        .withUniqueId(initiation.getInstructionIdentification())
                        .withExecutionDate(getDate(initiation))
                        .build();

        Storage storage = new Storage();
        storage.put(UkOpenBankingV31PaymentConstants.Storage.CONSENT_ID, consentId);

        return new PaymentResponse(payment, storage);
    }
}
