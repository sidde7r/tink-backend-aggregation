package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@JsonObject
public class OnlineTransactionsEntity {
    private List<OnlineTransactionEntity> booked;
    private List<OnlineTransactionEntity> pending;

    public List<AggregationTransaction> getTinkTransactions(String providerMarket) {
        List<AggregationTransaction> transactions = new ArrayList<>();
        if (booked != null) {
            transactions.addAll(
                    booked.stream()
                            .map(
                                    transactionEntity ->
                                            transactionEntity.toTinkTransaction(
                                                    false, providerMarket))
                            .collect(Collectors.toList()));
        }
        if (pending != null) {
            transactions.addAll(
                    pending.stream()
                            .map(
                                    transactionEntity ->
                                            transactionEntity.toTinkTransaction(
                                                    true, providerMarket))
                            .collect(Collectors.toList()));
        }
        return transactions;
    }
}
