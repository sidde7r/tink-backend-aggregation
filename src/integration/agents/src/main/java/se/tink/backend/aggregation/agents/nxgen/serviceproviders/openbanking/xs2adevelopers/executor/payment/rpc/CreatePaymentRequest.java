package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class CreatePaymentRequest {
    private AccountEntity creditorAccount;
    private String creditorName;
    private String dayOfExecution;
    private AccountEntity debtorAccount;
    private AmountEntity instructedAmount;

    public CreatePaymentRequest(
            AccountEntity creditorAccount,
            String creditorName,
            AccountEntity debtorAccount,
            AmountEntity instructedAmount) {
        this.creditorAccount = creditorAccount;
        this.creditorName = creditorName;
        this.debtorAccount = debtorAccount;
        this.instructedAmount = instructedAmount;
    }
}
