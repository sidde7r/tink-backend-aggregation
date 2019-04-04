package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.common.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.common.MetaEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.transaction.TransactionDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.transaction.TransactionTypeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class CrosskeyTransactionsResponse implements PaginatorResponse {

    private TransactionDataEntity data;
    private LinksEntity links;
    private MetaEntity meta;

    @JsonIgnore
    private TransactionTypeEntity transactionType;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        if (data == null) {
            return Collections.emptyList();
        }

        return data.getTransactions()
            .stream()
            .filter(getTransactionFilter())
            .map(transactionEntity -> transactionEntity.toTinkTransaction(transactionType))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }


    public CrosskeyTransactionsResponse setTransactionType(TransactionTypeEntity transactionType) {
        this.transactionType = transactionType;
        return this;
    }

    protected Predicate<TransactionEntity> getTransactionFilter() {
        return transactionEntity -> transactionEntity.getCreditDebitIndicator().equalsIgnoreCase(
            transactionType.getValue());
    }


}
