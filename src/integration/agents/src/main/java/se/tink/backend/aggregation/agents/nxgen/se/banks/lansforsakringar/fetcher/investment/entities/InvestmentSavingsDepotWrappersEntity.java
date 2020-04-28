package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentSavingsDepotWrappersEntity {
    private DepotEntity depot;
    private AccountEntity account;
    private SavingsGoalEntity savingsGoal;

    public DepotEntity getDepot() {
        return depot;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public SavingsGoalEntity getSavingsGoal() {
        return savingsGoal;
    }
}
