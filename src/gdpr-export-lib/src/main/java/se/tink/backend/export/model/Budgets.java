package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportBudget;

public class Budgets {

    private final List<ExportBudget> budgets;

    public Budgets(List<ExportBudget> budgets) {
        this.budgets = budgets;
    }

    public List<ExportBudget> getBudgets() {
        return budgets;
    }
}
