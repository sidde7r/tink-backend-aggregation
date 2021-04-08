package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@Getter
public class TransactionResponse {
    private boolean hasMoreTransactions;
    private List<TransactionsEntity> transactions;

    public Collection<Transaction> toTinkTransactions() {
        return transactions.stream()
                .map(TransactionsEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
