package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {

    private AccountEntity creditorAccount;
    private String creditorName;
    private AccountEntity debtorAccount;
    private AmountEntity instructedAmount;
    private String requestedExecutionDate;

    public CreatePaymentRequest(
            AccountEntity creditorAccount,
            String creditorName,
            AccountEntity debtorAccount,
            AmountEntity instructedAmount,
            String requestedExecutionDate) {
        this.creditorAccount = creditorAccount;
        this.creditorName = creditorName;
        this.debtorAccount = debtorAccount;
        this.instructedAmount = instructedAmount;
        this.requestedExecutionDate = requestedExecutionDate;
    }
}
