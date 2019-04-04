package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.balance;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.ParentList;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.Self;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Links {
    @JsonProperty("parent-list")
    private ParentList parentList;

    private Self self;
    private Transactions transactions;

    public Transactions getTransactions() {
        return transactions;
    }
}
