package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.data;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Transactions {

    private List<TransactionEntity> booked;

    public List<TransactionEntity> getTransactions() {
        return booked == null ? Collections.emptyList() : booked;
    }
}
