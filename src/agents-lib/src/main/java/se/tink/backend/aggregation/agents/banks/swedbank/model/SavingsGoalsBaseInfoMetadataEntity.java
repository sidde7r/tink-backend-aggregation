package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SavingsGoalsBaseInfoMetadataEntity {
    private int minimumGoalAmount;
    private int maximumGoalAmount;
    private int maximumSaveMonths;
    private int maximumTransferAmount;
    private int minimumSaveMonths;
    private int minimumTransferAmount;
    private int numberAllowedSavingsGoalsTotal;
    private String currentDate;

    public int getMinimumGoalAmount() {
        return minimumGoalAmount;
    }

    public void setMinimumGoalAmount(int minimumGoalAmount) {
        this.minimumGoalAmount = minimumGoalAmount;
    }

    public int getMaximumGoalAmount() {
        return maximumGoalAmount;
    }

    public void setMaximumGoalAmount(int maximumGoalAmount) {
        this.maximumGoalAmount = maximumGoalAmount;
    }

    public int getMaximumSaveMonths() {
        return maximumSaveMonths;
    }

    public void setMaximumSaveMonths(int maximumSaveMonths) {
        this.maximumSaveMonths = maximumSaveMonths;
    }

    public int getMaximumTransferAmount() {
        return maximumTransferAmount;
    }

    public void setMaximumTransferAmount(int maximumTransferAmount) {
        this.maximumTransferAmount = maximumTransferAmount;
    }

    public int getMinimumSaveMonths() {
        return minimumSaveMonths;
    }

    public void setMinimumSaveMonths(int minimumSaveMonths) {
        this.minimumSaveMonths = minimumSaveMonths;
    }

    public int getMinimumTransferAmount() {
        return minimumTransferAmount;
    }

    public void setMinimumTransferAmount(int minimumTransferAmount) {
        this.minimumTransferAmount = minimumTransferAmount;
    }

    public int getNumberAllowedSavingsGoalsTotal() {
        return numberAllowedSavingsGoalsTotal;
    }

    public void setNumberAllowedSavingsGoalsTotal(int numberAllowedSavingsGoalsTotal) {
        this.numberAllowedSavingsGoalsTotal = numberAllowedSavingsGoalsTotal;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }
}
