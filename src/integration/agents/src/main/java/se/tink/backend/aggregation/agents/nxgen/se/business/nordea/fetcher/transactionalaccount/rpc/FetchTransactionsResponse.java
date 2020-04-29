package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchTransactionsResponse {
    @JsonProperty("getAccountTransactionsOut")
    private TransactionsEntity transactionsEntity;

    public List<TransactionEntity> getTransactions() {
        return Optional.ofNullable(transactionsEntity)
                .orElse(new TransactionsEntity())
                .getTransactions();
    }

    public String getContinueKey() {
        return transactionsEntity.getContinueKey();
    }
}
