package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.transactionalaccount.entities.TransactionEntities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsResponse extends NordeaResponse {
    @JsonProperty("getAccountTransactionsOut")
    private TransactionEntities transactionsEntity;

    public TransactionEntities getTransactionsEntity() {
        return transactionsEntity;
    }

    @JsonIgnore
    public List<TransactionEntity> getTransactions() {
        return getTransactionsEntity() != null ? getTransactionsEntity().getTransactions() : null;
    }

    @JsonIgnore
    public String getContinueKey() {
        return getTransactionsEntity() != null ? getTransactionsEntity().getContinueKey() : null;
    }
}
