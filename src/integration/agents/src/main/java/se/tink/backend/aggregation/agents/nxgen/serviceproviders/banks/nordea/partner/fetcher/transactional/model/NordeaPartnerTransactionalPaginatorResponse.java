package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.model;

import java.util.Collection;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@AllArgsConstructor
public class NordeaPartnerTransactionalPaginatorResponse
        implements TransactionKeyPaginatorResponse<String> {

    @Getter private final Collection<? extends Transaction> tinkTransactions;
    private final String nextKey;
    private final Optional<Boolean> canFetchMore;

    @Override
    public String nextKey() {
        return nextKey;
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return canFetchMore;
    }
}
