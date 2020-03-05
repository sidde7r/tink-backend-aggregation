package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.entities.HandelsbankenSEAccount;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.transactionalaccount.entities.HandelsbankenSETransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsSEResponse extends TransactionsResponse implements PaginatorResponse {
    private HandelsbankenSEAccount account;
    private HandelsbankenSETransaction transactions;

    public HandelsbankenSEAccount getAccount() {
        return account;
    }

    @Override
    public List<Transaction> toTinkTransactions() {
        return transactions.toTinkTransactions();
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return toTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }
}
