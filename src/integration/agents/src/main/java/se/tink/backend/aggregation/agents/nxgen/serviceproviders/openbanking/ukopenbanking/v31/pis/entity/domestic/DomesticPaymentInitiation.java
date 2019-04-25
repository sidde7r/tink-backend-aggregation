package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
     * minLength: 1
     * maxLength: 35
     */
    private String endToEndIdentification;
    /**
     * Unique identification as assigned by an instructing party for an instructed party to
     * unambiguously identify the instruction. Usage: the instruction identification is a point to
     * point reference that can be used between the instructing party and the instructed party to
     * refer to the individual instruction. It can be included in several messages related to the
     * instruction.
     *
     * minLength: 1
     * maxLength: 35
     */
    private String instructionIdentification;

    private CreditorAccount creditorAccount;
    private InstructedAmount instructedAmount;

    public DomesticPaymentInitiation() {}

    public DomesticPaymentInitiation(
            Payment payment, String endToEndIdentification, String instructionIdentification) {
        this.remittanceInformation =
                new RemittanceInformation(payment.getId().toString(), payment.getReference());
        this.endToEndIdentification = endToEndIdentification;
        this.instructionIdentification = instructionIdentification;
        this.creditorAccount = new CreditorAccount(payment.getCreditor());
        this.instructedAmount = new InstructedAmount(payment.getAmount());
    }
}
