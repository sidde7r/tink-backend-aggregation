package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities.MetadataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities.TransactionAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities.TransactionKey;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class CreditCardTransactionsResponse implements TransactionKeyPaginatorResponse {

    private MetadataEntity metadata;
    private TransactionAccountEntity account;

    @JsonIgnore
    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(account).map(TransactionAccountEntity::getMovements)
                .orElse(Collections.emptyList()).stream()
                .filter(TransactionEntity::isValidTransaction)
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(
                metadata.getOffset() * metadata.getResultCount() < metadata.getTotalCount());
    }

    @Override
    public TransactionKey nextKey() {
        if (!canFetchMore().orElse(false)) {
            return null; // This must be an exception
        }
        return new TransactionKey(metadata.getOffset() * metadata.getResultCount());
    }
}
