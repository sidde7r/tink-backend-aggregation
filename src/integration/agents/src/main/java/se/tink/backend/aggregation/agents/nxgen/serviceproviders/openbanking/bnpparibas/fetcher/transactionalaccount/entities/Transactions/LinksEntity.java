package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.Transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    @JsonProperty("next")
    private NextEntity nextEntity;

    @JsonProperty("balances")
    private BalancesEntity balancesEntity;

    @JsonProperty("last")
    private LastEntity lastEntity;

    @JsonProperty("self")
    private SelfEntity selfEntity;

    @JsonProperty("parent-list")
    private ParentListEntity parentListEntity;

    public NextEntity getNextEntity() {
        return nextEntity;
    }

    public BalancesEntity getBalancesEntity() {
        return balancesEntity;
    }

    public LastEntity getLastEntity() {
        return lastEntity;
    }

    public SelfEntity getSelfEntity() {
        return selfEntity;
    }

    public ParentListEntity getParentListEntity() {
        return parentListEntity;
    }
}
