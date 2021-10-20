package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.OffsetConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities.MetadataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities.TransactionAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities.TransactionKey;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Slf4j
@JsonObject
public class CreditCardTransactionsResponse
        implements TransactionKeyPaginatorResponse<TransactionKey> {

    @JsonIgnore private String providerMarket;

    private MetadataEntity metadata;
    private TransactionAccountEntity account;

    @JsonIgnore
    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(account).map(TransactionAccountEntity::getMovements)
                .orElse(Collections.emptyList()).stream()
                .filter(TransactionEntity::isValidTransaction)
                .map(te -> te.toTinkTransaction(providerMarket))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(
                metadata.getOffset()
                                - OffsetConstants.TRANSACTIONS_OFFSET_ALIGNMENT
                                + metadata.getResultCount()
                        < metadata.getTotalCount());
        // offset is number of transactions, not page
    }

    @Override
    public TransactionKey nextKey() {
        if (!canFetchMore().orElse(false)) {
            log.error("There is no nextKey, so impossible to fetch new batch of transactions");
            return null; // This must be an exception
        }
        return new TransactionKey(
                metadata.getOffset()
                        - OffsetConstants.TRANSACTIONS_OFFSET_ALIGNMENT
                        + metadata.getResultCount());
    }

    public CreditCardTransactionsResponse setProviderMarket(String providerMarket) {
        this.providerMarket = providerMarket;
        return this;
    }
}
