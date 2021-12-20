package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.UpdateAccountTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UpdateTransactionsResponse {

    private List<UpdateAccountTransactionEntity> accountTransactions;

    public List<UpdateAccountTransactionEntity> getAccountTransactions() {
        return accountTransactions;
    }
}
