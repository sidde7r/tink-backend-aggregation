package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

/** Page consisting of multiple sub-pages. */
public final class CompositePaginatorResponse implements PaginatorResponse {

    private final Collection<PaginatorResponse> pages;

    public CompositePaginatorResponse(final Collection<PaginatorResponse> pages) {
        this.pages = pages;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return pages.stream()
                .map(PaginatorResponse::getTinkTransactions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }
}
