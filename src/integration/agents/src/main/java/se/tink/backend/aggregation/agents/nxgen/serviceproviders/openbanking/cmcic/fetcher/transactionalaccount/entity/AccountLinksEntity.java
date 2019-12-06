package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountLinksEntity {
    @JsonProperty("balances")
    private GenericLinkEntity balances = null;

    @JsonProperty("transactions")
    private GenericLinkEntity transactions = null;

    public GenericLinkEntity getBalances() {
        return balances;
    }

    public void setBalances(GenericLinkEntity balances) {
        this.balances = balances;
    }

    public GenericLinkEntity getTransactions() {
        return transactions;
    }

    public void setTransactions(GenericLinkEntity transactions) {
        this.transactions = transactions;
    }
}
