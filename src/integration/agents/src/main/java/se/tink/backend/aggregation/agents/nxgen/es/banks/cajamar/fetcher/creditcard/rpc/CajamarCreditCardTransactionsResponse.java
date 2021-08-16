package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.CheckedPredicate;
import io.vavr.collection.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.Fetchers;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.PaginationEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.entities.CardTransactionDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.entities.CardTransactionsPaginationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CajamarCreditCardTransactionsResponse {

    private CardTransactionsPaginationEntity cardTransactionsPagination;
    private String pan;

    @JsonIgnore
    public static CheckedPredicate<CajamarCreditCardTransactionsResponse> shouldRetryFetching(
            int attempt) {
        return response -> attempt <= Fetchers.MAX_TRY_ATTEMPTS;
    }

    public String getNextPageKey() {
        return String.valueOf(cardTransactionsPagination.getPagination().getPageNumber() + 1);
    }

    public List<CardTransactionDataEntity> getRawTransactions() {
        return cardTransactionsPagination.getDataList();
    }

    public Optional<Boolean> canFetchMore() {
        if (isPaginationEqualNullOrEmpty()) {
            return Optional.of(false);
        }
        PaginationEntity pagination = cardTransactionsPagination.getPagination();
        return Optional.of(pagination.getPageNumber() < pagination.getNumPages());
    }

    @JsonIgnore
    public boolean isPaginationEqualNullOrEmpty() {
        return cardTransactionsPagination == null
                || cardTransactionsPagination.getPagination() == null;
    }
}
