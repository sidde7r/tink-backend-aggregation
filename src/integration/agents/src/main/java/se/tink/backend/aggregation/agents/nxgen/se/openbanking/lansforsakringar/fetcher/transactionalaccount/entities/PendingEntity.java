package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.entities;

import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class PendingEntity {

    private DebtorAccountEntity debtorAccount;
    private String entryReference;
    private BalanceAmountEntity transactionAmount;
    private Date transactionDate;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toAmount())
                .setDate(transactionDate)
                .setDescription(entryReference)
                .setPending(true)
                .build();
    }
}
