package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.transactionalaccount.entities.TransactionEntities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsResponse extends NordeaResponse {
    @JsonProperty("getAccountTransactionsOut")
    private TransactionEntities transactionEntities;

    public TransactionEntities getTransactionEntities() {
        return transactionEntities;
    }

    @JsonIgnore
    public List<TransactionEntity> getTransactions() {
        return getTransactionEntities() != null ? getTransactionEntities().getTransactions() : null;
    }

    @JsonIgnore
    public String getContinueKey() {
        return getTransactionEntities() != null ? getTransactionEntities().getContinueKey() : null;
    }
}
