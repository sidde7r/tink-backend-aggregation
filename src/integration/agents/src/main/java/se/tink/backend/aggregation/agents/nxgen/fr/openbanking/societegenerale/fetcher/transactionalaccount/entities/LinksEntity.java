package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    @JsonProperty("balances")
    private Href balancesEntity;

    @JsonProperty("transactions")
    private Href transactionsEntity;

    public Href getBalancesEntity() {
        return balancesEntity;
    }
}
