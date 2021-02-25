package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.CheckedPredicate;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.Fetchers;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.PaginationEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.entities.CardTransactionDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.entities.CardTransactionsPaginationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class CajamarCreditCardTransactionsResponse
        implements TransactionKeyPaginatorResponse<String> {

    private CardTransactionsPaginationEntity cardTransactionsPagination;
    private String pan;

    @JsonIgnore
    public static CheckedPredicate<CajamarCreditCardTransactionsResponse> shouldRetryFetching(
            int attempt) {
        return response -> attempt <= Fetchers.MAX_TRY_ATTEMPTS;
    }

    @Override
    public String nextKey() {
        if (isPaginationEqualNullOrEmpty()) {
            return null;
        }
        return String.valueOf(cardTransactionsPagination.getPagination().getPageNumber() + 1);
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return cardTransactionsPagination
                .getDataList()
                .map(CardTransactionDataEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        if (isPaginationEqualNullOrEmpty()) {
            return Optional.of(false);
        }
        PaginationEntity pagination = cardTransactionsPagination.getPagination();
        return Optional.of(pagination.getPageNumber() < pagination.getNumPages());
    }

    @JsonIgnore
    private boolean isPaginationEqualNullOrEmpty() {
        return cardTransactionsPagination == null
                || cardTransactionsPagination.getPagination() == null;
    }
}
