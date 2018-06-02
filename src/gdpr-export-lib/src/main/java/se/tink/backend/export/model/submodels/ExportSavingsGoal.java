package se.tink.backend.export.model.submodels;

public class ExportSavingsGoal {

    private final Double targetAmount;
    private final String targetPeriod;
    private final String name;

    public ExportSavingsGoal(String accountNames, Double targetAmount, String targetPeriod) {
        this.name = accountNames;
        this.targetAmount = targetAmount;
        this.targetPeriod = targetPeriod;
    }

    public Double getTargetAmount() {
        return targetAmount;
    }

    public String getTargetPeriod() {
        return targetPeriod;
    }

    public String getName() {
        return name;
    }
}
