package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.entities.MetadataEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.entities.TransactionAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class CreditCardTransactionsResponse implements PaginatorResponse {

    private MetadataEntity metadata;
    private TransactionAccountEntity account;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(account).map(TransactionAccountEntity::getMovements)
                .orElse(Collections.emptyList()).stream()
                .map(TransactionEntity::constructCreditCardTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(
                metadata.getOffset() * metadata.getResultCount() < metadata.getTotalCount());
    }
}
