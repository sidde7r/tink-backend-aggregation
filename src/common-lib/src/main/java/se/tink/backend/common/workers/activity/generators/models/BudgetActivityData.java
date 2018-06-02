package se.tink.backend.common.workers.activity.generators.models;

import se.tink.backend.core.Budget;

public class BudgetActivityData {
    private Budget budget;
    private String period;

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}
