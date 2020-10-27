package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.ZoneOffset;
import java.util.Objects;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Data
public class DomesticScheduledPaymentConsentInitiation {

    private String instructionIdentification;
    private String endToEndIdentification;
    private String requestedExecutionDateTime;
    private InstructedAmount instructedAmount;
    private DebtorAccount debtorAccount;
    private CreditorAccount creditorAccount;
    private RemittanceInformation remittanceInformation;

    DomesticScheduledPaymentConsentInitiation(Payment payment) {
        this.instructionIdentification = payment.getUniqueId();
        this.endToEndIdentification = payment.getUniqueIdForUKOPenBanking();
        this.requestedExecutionDateTime =
                ISO_OFFSET_DATE_TIME.format(
                        payment.getExecutionDate().atStartOfDay(ZoneOffset.UTC));
        this.instructedAmount =
                Objects.nonNull(payment.getExactCurrencyAmountFromField())
                        ? new InstructedAmount(payment.getExactCurrencyAmountFromField())
                        : null;
        this.debtorAccount =
                Objects.isNull(payment.getDebtor()) ? null : new DebtorAccount(payment.getDebtor());
        this.creditorAccount =
                Objects.nonNull(payment.getCreditor())
                        ? new CreditorAccount(payment.getCreditor())
                        : null;
        this.remittanceInformation =
                Objects.nonNull(payment.getRemittanceInformation())
                        ? RemittanceInformation.ofUnstructured(
                                payment.getRemittanceInformation().getValue())
                        : null;
    }
}
