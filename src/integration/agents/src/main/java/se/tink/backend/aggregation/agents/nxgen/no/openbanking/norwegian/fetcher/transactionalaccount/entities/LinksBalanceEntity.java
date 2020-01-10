package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksBalanceEntity {

    @JsonProperty("self")
    private Href selfEntity;

    private Href transactions;

    @JsonProperty("parent-list")
    private Href parentListEntity;

    public Href getSelfEntity() {
        return selfEntity;
    }

    public Href getTransactions() {
        return transactions;
    }

    public Href getParentListEntity() {
        return parentListEntity;
    }
}
