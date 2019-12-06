package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DomesticPaymentRequest {

    private AccountEntity creditorAccount;
    private AccountEntity debtorAccount;
    private AmountEntity instructedAmount;

    public DomesticPaymentRequest() {}

    public DomesticPaymentRequest(
            AccountEntity creditorAccount,
            AccountEntity debtorAccount,
            AmountEntity instructedAmount) {
        this.creditorAccount = creditorAccount;
        this.debtorAccount = debtorAccount;
        this.instructedAmount = instructedAmount;
    }
}
