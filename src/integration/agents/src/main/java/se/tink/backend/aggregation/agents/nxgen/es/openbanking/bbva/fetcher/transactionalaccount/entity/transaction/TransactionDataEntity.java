
package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.entity.transaction;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionDataEntity {

    private List<AccountTransactionEntity> accountTransactions;

    public List<AccountTransactionEntity> getAccountTransactions() {
        return accountTransactions;
    }
}
