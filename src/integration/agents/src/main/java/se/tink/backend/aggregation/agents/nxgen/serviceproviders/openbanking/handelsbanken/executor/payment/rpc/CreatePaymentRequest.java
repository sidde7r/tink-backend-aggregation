package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {

    @JsonProperty("creditorAccount")
    private AccountEntity creditor;

    @JsonProperty("debtorAccount")
    private AccountEntity debtor;

    @JsonProperty("instructedAmount")
    private AmountEntity amount;

    public CreatePaymentRequest(AccountEntity creditor, AccountEntity debtor, AmountEntity amount) {
        this.creditor = creditor;
        this.debtor = debtor;
        this.amount = amount;
    }
}
