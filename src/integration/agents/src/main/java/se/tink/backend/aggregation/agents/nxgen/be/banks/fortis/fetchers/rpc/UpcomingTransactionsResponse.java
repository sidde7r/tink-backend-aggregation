package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities.BusinessMessageBulk;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities.UpcomingTransactionValue;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class UpcomingTransactionsResponse {
    private BusinessMessageBulk businessMessageBulk;
    private UpcomingTransactionValue value;

    public BusinessMessageBulk getBusinessMessageBulk() {
        return businessMessageBulk;
    }

    public UpcomingTransactionValue getValue() {
        return value;
    }

    public Collection<? extends UpcomingTransaction> getTinkTransactions() {
        if (value == null) {
            return Collections.emptyList();
        }
        return value.toTinkTransactions();
    }

    public Optional<Boolean> canFetchMore() {
        return Optional.of(!getTinkTransactions().isEmpty() && !value.isCompleteListFlag());
    }
}
