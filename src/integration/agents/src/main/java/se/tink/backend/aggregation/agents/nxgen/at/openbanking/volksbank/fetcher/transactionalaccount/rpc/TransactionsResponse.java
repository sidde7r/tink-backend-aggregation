package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.entity.common.AccountInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.entity.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.entity.transaction.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements PaginatorResponse {

    public AccountInfoEntity account;
    public TransactionsEntity transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {

        return Stream.concat(
                        transactions.getBooked().stream()
                                .map(TransactionEntity::toTinkBookedTransaction),
                        transactions.getPending().stream()
                                .map(TransactionEntity::toTinkPendingTransaction))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(false); // TODO Sandbox limitation, set Optional.empty() for prod
    }
}
