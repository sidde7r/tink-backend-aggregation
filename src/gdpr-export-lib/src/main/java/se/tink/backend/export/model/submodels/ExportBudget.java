package se.tink.backend.export.model.submodels;

public class ExportBudget {

    private final String name;
    private final Double budgetedAmount;

    public ExportBudget(String name, Double budgetedAmount) {
        this.name = name;
        this.budgetedAmount = budgetedAmount;
    }

    public String getName() {
        return name;
    }

    public Double getBudgetedAmount() {
        return budgetedAmount;
    }
}
