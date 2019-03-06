package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaTransactionsDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.Collections;
import java.util.List;

@JsonObject
public final class AccountTransactionsEntity {
    @JsonDeserialize(using = AxaTransactionsDeserializer.class)
    private List<TransactionEntity> transactions;

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public static AccountTransactionsEntity createEmpty() {
        final AccountTransactionsEntity entity = new AccountTransactionsEntity();
        entity.transactions = Collections.emptyList();
        return entity;
    }
}
