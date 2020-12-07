package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.balance;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.common.TransactionAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceInterimAvailableEntity {
    private TransactionAmountEntity amount;
    private String date;

    public TransactionAmountEntity getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }
}
