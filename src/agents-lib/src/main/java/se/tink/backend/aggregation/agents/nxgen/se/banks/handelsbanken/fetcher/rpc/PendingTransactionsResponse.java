package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities.TransactionGroup;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class PendingTransactionsResponse extends BaseResponse {
    private List<TransactionGroup> transactionGroups;

    public Stream<UpcomingTransaction> toTinkTransactions(Account account, HandelsbankenSEApiClient client) {
        return transactionGroups.stream()
                .filter(groupBelongsTo(account))
                .flatMap(transactionGroup -> transactionGroup.toTinkTransactions(client));
    }

    public boolean hasTransactionsFor(Account account) {
        return transactionGroups.stream()
                .anyMatch(groupBelongsTo(account));
    }

    private static Predicate<TransactionGroup> groupBelongsTo(Account account) {
        return transactionGroup -> transactionGroup.belongsTo(account);
    }
}
