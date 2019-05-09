package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.entities;

import java.util.List;
import org.assertj.core.util.Lists;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsEntity {

    private List<TransactionEntity> transactions;

    public List<TransactionEntity> getTransactions() {
        return transactions == null ? Lists.emptyList() : transactions;
    }
}
