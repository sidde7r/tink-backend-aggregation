package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {
    private AccountEntity creditorAccount;
    private AccountEntity debtorAccount;
    private AmountEntity instructedAmount;

    public CreatePaymentRequest(
            AccountEntity creditorAccount,
            AccountEntity debtorAccount,
            AmountEntity instructedAmount) {
        this.creditorAccount = creditorAccount;
        this.debtorAccount = debtorAccount;
        this.instructedAmount = instructedAmount;
    }
}
