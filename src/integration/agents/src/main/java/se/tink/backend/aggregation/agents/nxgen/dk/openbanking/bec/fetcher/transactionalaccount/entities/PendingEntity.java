package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

import java.util.Date;

@JsonObject
public class PendingEntity {
    private String creditorAccount;
    private String entryReference;
    private AmountEntity transactionAmount;
    private String transactionId;
    private Date valueDate;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toAmount())
                .setDate(valueDate)
                .setDescription(entryReference)
                .setPending(true)
                .build();
    }
}