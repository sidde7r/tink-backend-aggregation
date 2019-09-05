package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc.SdcConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc.fetcher.transactionalaccount.entity.transaction.TransactionAccountInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc.fetcher.transactionalaccount.entity.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc.fetcher.transactionalaccount.entity.transaction.Transactions;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements PaginatorResponse {

    private TransactionAccountInfoEntity account;
    private Transactions transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Stream.concat(
                        transactions.getBooked().stream()
                                .map(TransactionEntity::toBookedTinkTransaction),
                        transactions.getPending().stream()
                                .map(TransactionEntity::toPendingTinkTransaction))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }

    public Optional<Date> getOverflowTransactionDate() {

        int totalTransactionsReturned =
                transactions.getBooked().size() + transactions.getPending().size();

        return totalTransactionsReturned >= SdcConstants.Transactions.MAX_TRANSACTIONS_PER_RESPONSE
                ? Stream.concat(
                                transactions.getBooked().stream(),
                                transactions.getPending().stream())
                        .map(TransactionEntity::getValueDate)
                        .min(Date::compareTo)
                : Optional.empty();
    }
}
