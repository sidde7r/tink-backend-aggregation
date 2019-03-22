package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities.TransactionResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse {

    @JsonProperty("methodResult")
    private TransactionResultEntity transactionResult;

    public Collection<Transaction> toTinkTransactions() {
        return transactionResult.toTinkTransactions();
    }
}
