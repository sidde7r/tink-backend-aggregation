package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.entities.transactions;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsEntity {

    private List<TransactionsItemEntity> booked;
    private List<TransactionsItemEntity> pending;

    public List<TransactionsItemEntity> getBooked() {
        return booked;
    }

    public List<TransactionsItemEntity> getPending() {
        return pending;
    }
}
