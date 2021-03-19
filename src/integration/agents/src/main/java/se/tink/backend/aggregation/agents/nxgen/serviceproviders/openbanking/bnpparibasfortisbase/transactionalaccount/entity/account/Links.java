package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.transactionalaccount.entity.account;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.transactionalaccount.entity.Self;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Links {

    private Balances balances;
    private First first;
    private Last last;
    private Next next;
    private Prev prev;
    private Self self;

    private Transactions transactions;

    public Balances getBalances() {
        return balances;
    }

    public Transactions getTransactions() {
        return transactions;
    }
}
