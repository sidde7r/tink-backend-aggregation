package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.entity.TransactionsItemEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionResponse implements PaginatorResponse {

    @JsonIgnore private String providerMarket;

    private List<TransactionsItemEntity> transactions;

    public List<TransactionsItemEntity> getTransactions() {
        return transactions;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .filter(TransactionsItemEntity::hasDate)
                .map(te -> te.toTinkTransaction(providerMarket))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }

    public TransactionResponse setProviderMarket(String providerMarket) {
        this.providerMarket = providerMarket;
        return this;
    }
}
