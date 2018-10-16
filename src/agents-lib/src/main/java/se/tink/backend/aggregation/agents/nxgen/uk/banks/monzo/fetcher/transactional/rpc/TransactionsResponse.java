package se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.fetcher.transactional.rpc;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.MonzoConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.fetcher.transactional.entity.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    private List<TransactionEntity> transactions;
    private boolean canFetchMore = true;

    @Override
    public String nextKey() {
        return transactions.stream().max(Comparator.comparing(e -> e.getCreated(), Instant::compareTo))
                .map(e -> e.getId()).orElse(null);
    }

    @Override
    public List<Transaction> getTinkTransactions() {

        List<Transaction> retVal = transactions.stream().map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());

        canFetchMore = retVal.size() == MonzoConstants.FetchControl.LIMIT;

        return retVal;
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(canFetchMore);
    }
    
}
