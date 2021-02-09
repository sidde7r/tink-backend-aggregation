package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard.model;

import java.util.Collection;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@AllArgsConstructor
public class NordeaPartnerCreditCardPaginatorResponse implements PaginatorResponse {

    @Getter private final Collection<? extends Transaction> tinkTransactions;
    private final Optional<Boolean> canFetchMore;

    @Override
    public Optional<Boolean> canFetchMore() {
        return canFetchMore;
    }
}
