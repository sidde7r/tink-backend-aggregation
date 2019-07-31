package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.Transactions.SelfEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksBalanceEntity {

    @JsonProperty("self")
    private SelfEntity selfEntity;

    @JsonProperty("transactions")
    private TransactionsEntity transactions;

    @JsonProperty("parent-list")
    private ParentListEntity parentListEntity;

    public SelfEntity getSelfEntity() {
        return selfEntity;
    }

    public TransactionsEntity getTransactions() {
        return transactions;
    }

    public ParentListEntity getParentListEntity() {
        return parentListEntity;
    }
}
