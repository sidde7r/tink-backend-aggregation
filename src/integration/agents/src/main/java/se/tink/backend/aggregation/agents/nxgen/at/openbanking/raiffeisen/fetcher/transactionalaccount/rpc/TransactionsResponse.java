package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.entity.transaction.TransactionAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.entity.transaction.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements PaginatorResponse {

    public TransactionAccountEntity account;
    public TransactionsEntity transactions;
    public String balances;
    public LinksEntity links;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {

        return Stream.concat(
                        transactions.getBooked().stream()
                                .map(
                                        transactionEntity ->
                                                transactionEntity.toTinkTransaction(false)),
                        transactions.getPending().stream()
                                .map(
                                        transactionEntity ->
                                                transactionEntity.toTinkTransaction(true)))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(false); // TODO change once pagination is fixed in bank api
    }
}
