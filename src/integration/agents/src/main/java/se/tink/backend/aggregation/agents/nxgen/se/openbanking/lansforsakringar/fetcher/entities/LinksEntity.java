package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private LinksDetailsEntity account;
    private LinksDetailsEntity balances;
    private LinksDetailsEntity transactions;

    @JsonIgnore
    public LinksDetailsEntity getAccount() {
        return account;
    }

    @JsonIgnore
    public LinksDetailsEntity getBalances() {
        return balances;
    }

    @JsonIgnore
    public LinksDetailsEntity getTransactions() {
        return transactions;
    }
}
