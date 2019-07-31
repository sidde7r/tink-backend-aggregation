package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    @JsonProperty("balances")
    private BalancesEntity balancesEntity;

    @JsonProperty("transactions")
    private TransactionsEntity transactionsEntity;

    public BalancesEntity getBalancesEntity() {
        return balancesEntity;
    }

    public TransactionsEntity getTransactionsEntity() {
        return transactionsEntity;
    }
}
