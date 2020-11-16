package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.domestic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class DomesticPaymentInitiation {
    private RemittanceInformation remittanceInformation;
    /**
     * Unique identification assigned by the initiating party to unambiguously identify the
     * transaction. This identification is passed on, unchanged, throughout the entire end-to-end
     * chain. Usage: The end-to-end identification can be used for reconciliation or to link tasks
     * relating to the transaction. It can be included in several messages related to the
     * transaction. OB: The Faster Payments Scheme can only access 31 characters for the
     * EndToEndIdentification field
     *
     * <p>minLength: 1 maxLength: 35
     */
    private String endToEndIdentification;
    /**
     * Unique identification as assigned by an instructing party for an instructed party to
     * unambiguously identify the instruction. Usage: the instruction identification is a point to
     * point reference that can be used between the instructing party and the instructed party to
     * refer to the individual instruction. It can be included in several messages related to the
     * instruction.
     *
     * <p>minLength: 1 maxLength: 35
     */
    private String instructionIdentification;

    @JsonInclude(Include.NON_NULL)
    private DebtorAccount debtorAccount;

    private CreditorAccount creditorAccount;
    private InstructedAmount instructedAmount;

    // Used in serialization unit tests
    protected DomesticPaymentInitiation() {}

    public DomesticPaymentInitiation(
            Payment payment, String endToEndIdentification, String instructionIdentification) {
        String unstructuredRemittanceInformation =
                Optional.ofNullable(payment.getRemittanceInformation())
                        .map(se.tink.libraries.transfer.rpc.RemittanceInformation::getValue)
                        .orElse("");
        this.remittanceInformation =
                RemittanceInformation.ofUnstructuredAndReference(unstructuredRemittanceInformation);
        this.endToEndIdentification = endToEndIdentification;
        this.instructionIdentification = instructionIdentification;
        this.creditorAccount =
                Objects.nonNull(payment.getCreditor())
                        ? new CreditorAccount(payment.getCreditor())
                        : null;
        this.debtorAccount =
                Objects.isNull(payment.getDebtor()) ? null : new DebtorAccount(payment.getDebtor());
        this.instructedAmount =
                Objects.nonNull(payment.getExactCurrencyAmountFromField())
                        ? new InstructedAmount(payment.getExactCurrencyAmountFromField())
                        : null;
    }

    @JsonIgnore
    public void setReferenceAsUnstructured() {
        // HSBC and Monzo has requirements on reference
        this.remittanceInformation.setReference(remittanceInformation.getUnstructured());
    }
}
