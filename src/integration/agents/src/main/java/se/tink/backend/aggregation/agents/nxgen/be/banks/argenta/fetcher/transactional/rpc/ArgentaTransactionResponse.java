package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.entity.ArgentaTransaction;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class ArgentaTransactionResponse implements PaginatorResponse {
    private int page;
    private int nextPage;
    private List<ArgentaTransaction> transactions;

    public int getPage() {
        return page;
    }

    public int getNextPage() {
        return nextPage;
    }

    public List<ArgentaTransaction> getTransactions() {
        return transactions;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(ArgentaTransaction::toTinkTransaction)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(this.nextPage != ArgentaConstants.Fetcher.END_PAGE);
    }
}
