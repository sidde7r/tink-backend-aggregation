package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.control.Option;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.IbanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.PaginationEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.RepaginationEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class AccountTransactionsResponse implements TransactionKeyPaginatorResponse<URL> {
    @JsonProperty("_links")
    private PaginationEntity pagination;

    @JsonProperty("repaginacion")
    private RepaginationEntity repagination;

    @JsonProperty("movimientos")
    private List<TransactionEntity> transactions;

    @JsonProperty("cuentaAsociada")
    private IbanEntity associatedAccount;

    @JsonProperty("titular")
    private String accountHolder;

    @JsonProperty("masMovimientos")
    private boolean hasMoreTransactions;

    public static AccountTransactionsResponse empty() {
        return new AccountTransactionsResponse();
    }

    public PaginationEntity getPagination() {
        return pagination;
    }

    public RepaginationEntity getRepagination() {
        return repagination;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public IbanEntity getAssociatedAccount() {
        return associatedAccount;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public boolean hasMoreTransactions() {
        return hasMoreTransactions;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions
                .stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(hasMoreTransactions);
    }

    @Override
    public URL nextKey() {
        return Option.of(pagination)
                .flatMap(PaginationEntity::getNextPage)
                .map(URL::new)
                .getOrNull();
    }
}
