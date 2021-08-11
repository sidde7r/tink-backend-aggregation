package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.PaginationDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class AccountTransactionsResponse
        implements TransactionKeyPaginatorResponse<PaginationDataEntity> {
    @JsonProperty("indicadorPaginacion")
    private boolean paginationIndicator;

    @JsonProperty("movimientos")
    private List<TransactionEntity> transactions;

    @JsonProperty("indicadorMasRegistros")
    private boolean moreRecordsIndicator;

    @JsonProperty("datosRellamada")
    private PaginationDataEntity paginationData;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(moreRecordsIndicator);
    }

    @Override
    public PaginationDataEntity nextKey() {
        return paginationData;
    }
}
