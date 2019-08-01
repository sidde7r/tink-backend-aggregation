package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesLinksEntity {
    @JsonProperty("self")
    private GenericLinkEntity self = null;

    @JsonProperty("parent-list")
    private GenericLinkEntity parentList = null;

    @JsonProperty("transactions")
    private GenericLinkEntity transactions = null;

    public GenericLinkEntity getSelf() {
        return self;
    }

    public void setSelf(GenericLinkEntity self) {
        this.self = self;
    }

    public GenericLinkEntity getParentList() {
        return parentList;
    }

    public void setParentList(GenericLinkEntity parentList) {
        this.parentList = parentList;
    }

    public GenericLinkEntity getTransactions() {
        return transactions;
    }

    public void setTransactions(GenericLinkEntity transactions) {
        this.transactions = transactions;
    }
}
