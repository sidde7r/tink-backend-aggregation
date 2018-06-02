package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities;

import java.util.List;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class TransactionGroup {
    private List<PendingTransaction> transactions;
    private HandelsbankenSEAccount account;

    public Stream<UpcomingTransaction> toTinkTransactions(HandelsbankenSEApiClient client) {
        return transactions.stream()
                .filter(PendingTransaction::isNotSuspended)
                .filter(PendingTransaction::isNotAbandoned)
                .map(pendingTransaction -> pendingTransaction.toTinkTransaction(client));
    }

    public boolean belongsTo(Account tinkAccount) {
        return account != null && account.is(tinkAccount);
    }
}
