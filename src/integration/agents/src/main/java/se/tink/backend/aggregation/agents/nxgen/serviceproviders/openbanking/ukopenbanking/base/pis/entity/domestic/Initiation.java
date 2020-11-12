package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.domestic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class Initiation {

    private String instructionIdentification;
    private String endToEndIdentification;
    private InstructedAmount instructedAmount;
    /**  {@link DebtorAccount} is not mandatory so added the null support. */
    @JsonInclude(Include.NON_NULL)
    private DebtorAccount debtorAccount;

    private CreditorAccount creditorAccount;
    private RemittanceInformation remittanceInformation;

    // Used in serialization unit tests
    protected Initiation() {}

    public Initiation(Payment payment) {

        // TODO: What to use here?
        this.instructionIdentification = payment.getUniqueId();
        this.endToEndIdentification = payment.getUniqueIdForUKOPenBanking();

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
                        ? RemittanceInformation.ofUnstructuredAndReference(
                                payment.getRemittanceInformation().getValue())
                        : null;
    }

    public ExactCurrencyAmount toTinkAmount() {
        return instructedAmount.toTinkAmount();
    }

    @JsonIgnore
    public Debtor getDebtor() {
        return Objects.isNull(debtorAccount) ? null : debtorAccount.toDebtor();
    }

    @JsonIgnore
    public Creditor getCreditor() {
        return creditorAccount.toCreditor();
    }

    @JsonIgnore
    public String getUnstructuredRemittanceInformation() {
        return remittanceInformation.getUnstructured();
    }

    public String getInstructionIdentification() {
        return instructionIdentification;
    }

    @JsonIgnore
    public void setReferenceAsUnstructured() {
        // HSBC and Monzo has requirements on reference
        this.remittanceInformation.setReference(remittanceInformation.getUnstructured());
    }
}
