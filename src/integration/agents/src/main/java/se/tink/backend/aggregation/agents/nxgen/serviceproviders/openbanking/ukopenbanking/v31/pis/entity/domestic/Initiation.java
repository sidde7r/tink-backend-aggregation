package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class Initiation {

    private String instructionIdentification;
    private String endToEndIdentification;
    private InstructedAmount instructedAmount;
    /**
     * IF debtorAccount is NOT specified in the request:
     *
     * <p>The merchant has not specified the Debtor Account details for the PSU. The PSU will select
     * their account during the authorisation of consent.
     *
     * <p>We will always specify it in order to not have to go through an extra step. This can be
     * made configurable
     */
    private DebtorAccount debtorAccount;

    private CreditorAccount creditorAccount;
    private RemittanceInformation remittanceInformation;

    // Used in serialization unit tests
    protected Initiation() {}

    public Initiation(Payment payment) {

        // TODO: What to use here?
        this.instructionIdentification = payment.getUniqueId();
        this.endToEndIdentification = payment.getUniqueId();

        this.instructedAmount = new InstructedAmount(payment.getAmount());
        this.debtorAccount = new DebtorAccount(payment.getDebtor());
        this.creditorAccount = new CreditorAccount(payment.getCreditor());
        this.remittanceInformation =
                new RemittanceInformation(payment.getUniqueId(), payment.getReference());
    }

    public Amount toTinkAmount() {
        return instructedAmount.toTinkAmount();
    }

    @JsonIgnore
    public Debtor getDebtor() {
        return debtorAccount.toDebtor();
    }

    @JsonIgnore
    public Creditor getCreditor() {
        return creditorAccount.toCreditor();
    }

    @JsonIgnore
    public Reference getReference() {
        return remittanceInformation.createTinkReference();
    }

    public String getInstructionIdentification() {
        return instructionIdentification;
    }
}
