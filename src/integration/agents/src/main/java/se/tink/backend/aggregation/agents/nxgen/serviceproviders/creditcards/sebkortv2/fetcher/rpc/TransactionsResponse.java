package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher.entity.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse {
    private boolean moreDataExists;
    private List<TransactionEntity> transactions;

    public List<Transaction> getTinkTranscations() {
        if (Objects.isNull(transactions)) {
            return Collections.emptyList();
        }

        return transactions.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
