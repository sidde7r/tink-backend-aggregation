package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.balance;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Balance {
    private BalanceAmount balanceAmount;
    private String balanceType;
    private String lastCommittedTransaction;
    private String name;

    public BalanceAmount getBalanceAmount() {
        return balanceAmount;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public String getName() {
        return name;
    }
}
