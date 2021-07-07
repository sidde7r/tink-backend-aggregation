package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.BalancesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    @JsonProperty("next")
    private Href nextEntity;

    @JsonProperty("balances")
    private BalancesEntity balancesEntity;

    @JsonProperty("last")
    private Href lastEntity;

    @JsonProperty("self")
    private Href selfEntity;

    @JsonProperty("parent-list")
    private Href parentListEntity;

    public Href getNextEntity() {
        return nextEntity;
    }

    public BalancesEntity getBalancesEntity() {
        return balancesEntity;
    }

    public Href getLastEntity() {
        return lastEntity;
    }

    public Href getSelfEntity() {
        return selfEntity;
    }

    public Href getParentListEntity() {
        return parentListEntity;
    }
}
