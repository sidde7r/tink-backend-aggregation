package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvestmentSavingsDepotWrappersEntity {
    private DepotEntity depot;
    private InvestmentAccountEntity account;
    private SavingsGoalEntity savingsGoal;

    public DepotEntity getDepot() {
        return depot;
    }

    public void setDepot(DepotEntity depot) {
        this.depot = depot;
    }

    public InvestmentAccountEntity getAccount() {
        return account;
    }

    public void setAccount(InvestmentAccountEntity account) {
        this.account = account;
    }

    public SavingsGoalEntity isSavingsGoal() {
        return savingsGoal;
    }

    public void setSavingsGoal(SavingsGoalEntity savingsGoal) {
        this.savingsGoal = savingsGoal;
    }
}
