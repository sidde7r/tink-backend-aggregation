package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.ParentList;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.Self;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Links {

    private Balances balances;
    private Last last;
    private Next next;
    private Self self;

    @JsonProperty("parent-list")
    private ParentList parentList;

    public Balances getBalances() {
        return balances;
    }

    public Next getNext() {
        return next;
    }
}
