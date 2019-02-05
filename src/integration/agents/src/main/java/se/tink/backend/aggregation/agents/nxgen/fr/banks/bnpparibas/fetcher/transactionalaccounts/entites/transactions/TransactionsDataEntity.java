package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsDataEntity {
    @JsonProperty("listerOperations")
    private TransactionsInfoEntity transactionsInfo;

    public TransactionsInfoEntity transactionsInfo() {
        return transactionsInfo;
    }
}
