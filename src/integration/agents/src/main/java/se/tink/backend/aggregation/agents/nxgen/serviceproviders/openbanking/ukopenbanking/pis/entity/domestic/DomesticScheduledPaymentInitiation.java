package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.domestic;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Data
@NoArgsConstructor
public class DomesticScheduledPaymentInitiation {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SupplementaryData supplementaryData;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> localInstrument;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DebtorAccount debtorAccount;

    private RemittanceInformation remittanceInformation;
    private String instructionIdentification;
    private CreditorAccount creditorAccount;
    private InstructedAmount instructedAmount;
    private String requestedExecutionDateTime;

    DomesticScheduledPaymentInitiation(Payment payment, String instructionIdentification) {

        this.remittanceInformation = getRemittanceInformation(payment);
        this.instructionIdentification = instructionIdentification;
        this.creditorAccount =
                Objects.nonNull(payment.getCreditor())
                        ? new CreditorAccount(payment.getCreditor())
                        : null;
        this.debtorAccount = getDebtorAccount(payment);
        this.instructedAmount =
                Objects.nonNull(payment.getExactCurrencyAmountFromField())
                        ? new InstructedAmount(payment.getExactCurrencyAmountFromField())
                        : null;
        this.requestedExecutionDateTime =
                ISO_OFFSET_DATE_TIME.format(
                        payment.getExecutionDate().atStartOfDay(ZoneOffset.UTC));
    }

    private static RemittanceInformation getRemittanceInformation(Payment payment) {
        final String unstructuredRemittanceInformation =
                Optional.ofNullable(payment.getRemittanceInformation())
                        .map(se.tink.libraries.transfer.rpc.RemittanceInformation::getValue)
                        .orElse("");

        return RemittanceInformation.ofUnstructuredAndReference(unstructuredRemittanceInformation);
    }

    private static DebtorAccount getDebtorAccount(Payment payment) {
        return Objects.isNull(payment.getDebtor()) ? null : new DebtorAccount(payment.getDebtor());
    }
}
