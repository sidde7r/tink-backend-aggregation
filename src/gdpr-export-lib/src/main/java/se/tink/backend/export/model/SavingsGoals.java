package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportSavingsGoal;

public class SavingsGoals {

    private final List<ExportSavingsGoal> savingsGoals;

    public SavingsGoals(List<ExportSavingsGoal> savingsGoals) {
        this.savingsGoals = savingsGoals;
    }

    public List<ExportSavingsGoal> getSavingsGoals() {
        return savingsGoals;
    }
}
