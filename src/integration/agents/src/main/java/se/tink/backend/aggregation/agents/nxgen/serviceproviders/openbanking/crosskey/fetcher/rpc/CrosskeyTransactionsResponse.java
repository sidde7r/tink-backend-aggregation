package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
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

    @JsonIgnore private boolean canFetchMore = true;
    @JsonIgnore private TransactionTypeEntity transactionType;

    public Collection<? extends Transaction> getTinkCreditCardTransactions() {
        return Optional.ofNullable(data).map(TransactionDataEntity::getTransactions)
                .orElse(Collections.emptyList()).stream()
                .map(TransactionEntity::constructCreditCardTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(data).map(TransactionDataEntity::getTransactions)
                .orElse(Collections.emptyList()).stream()
                .map(TransactionEntity::constructTransactionalAccountTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        if (data == null || data.getTransactions().size() == 0) {
            return Optional.empty();
        }
        return Optional.of(canFetchMore);
    }

    public CrosskeyTransactionsResponse setTransactionType(TransactionTypeEntity transactionType) {
        this.transactionType = transactionType;
        return this;
    }
}
