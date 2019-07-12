package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.PaymentRequestValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditTransferTransactionEntity {
    private PaymentIdEntity paymentId;
    private AmountEntity instructedAmount;
    private List<String> remittanceInformation;

    @JsonIgnore
    public CreditTransferTransactionEntity(AmountEntity instructedAmount) {
        paymentId = new PaymentIdEntity();
        this.instructedAmount = instructedAmount;
        remittanceInformation = Arrays.asList(PaymentRequestValues.REMITTANCE_INFORMATION);
    }

    public CreditTransferTransactionEntity() {}

    public AmountEntity getAmount() {
        return instructedAmount;
    }
}
