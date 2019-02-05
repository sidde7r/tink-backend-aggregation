package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities.PendingTransaction;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities.TransactionGroup;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public class PendingTransactionsResponse extends BaseResponse {
    private List<TransactionGroup> transactionGroups;

    public Stream<PendingTransaction> getPendingTransactionStream() {
        return transactionGroups.stream()
                .flatMap(TransactionGroup::getPendingTransactionStream);
    }

    public Stream<PendingTransaction> getPendingTransactionStream(Account account) {
        return transactionGroups.stream()
                .filter(groupBelongsTo(account))
                .flatMap(TransactionGroup::getPendingTransactionStream);
    }

    private static Predicate<TransactionGroup> groupBelongsTo(Account account) {
        return transactionGroup -> transactionGroup.belongsTo(account);
    }
}
