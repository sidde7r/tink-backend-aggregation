package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksBalanceEntity {

    @JsonProperty("self")
    private HrefEntity selfEntity;

    private HrefEntity transactions;

    @JsonProperty("parent-list")
    private HrefEntity parentListEntity;

    public HrefEntity getSelfEntity() {
        return selfEntity;
    }

    public HrefEntity getTransactions() {
        return transactions;
    }

    public HrefEntity getParentListEntity() {
        return parentListEntity;
    }
}
