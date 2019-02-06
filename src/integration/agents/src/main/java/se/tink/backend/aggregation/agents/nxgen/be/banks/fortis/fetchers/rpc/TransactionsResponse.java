package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities.BusinessMessageBulk;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities.TransactionValue;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements PaginatorResponse {
    private BusinessMessageBulk businessMessageBulk;
    private TransactionValue value;

    public BusinessMessageBulk getBusinessMessageBulk() {
        return businessMessageBulk;
    }

    public TransactionValue getValue() {
        return value;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        if (value == null) {
            return Collections.emptyList();
        }
        return value.toTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(!getTinkTransactions().isEmpty() && !value.isCompleteListFlag());
    }
}
