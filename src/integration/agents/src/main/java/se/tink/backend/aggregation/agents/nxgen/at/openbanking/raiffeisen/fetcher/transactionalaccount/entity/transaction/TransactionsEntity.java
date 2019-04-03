package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.entity.transaction;

import java.util.List;
import org.assertj.core.util.Lists;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsEntity {

    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;
    private Links links;

    public List<TransactionEntity> getBooked() {
        return booked == null ? Lists.emptyList() : booked;
    }

    public List<TransactionEntity> getPending() {
        return pending == null ? Lists.emptyList() : pending;
    }
}
