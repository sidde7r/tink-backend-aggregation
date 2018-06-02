package se.tink.backend.aggregation.agents.banks.swedbank.model;

public class CreateSavingsGoalRequest {
    private String goalAmount;
    private String openNewAccountProductId;
    private String name;

    public String getGoalAmount() {
        return goalAmount;
    }

    public void setGoalAmount(String goalAmount) {
        this.goalAmount = goalAmount;
    }

    public String getOpenNewAccountProductId() {
        return openNewAccountProductId;
    }

    public void setOpenNewAccountProductId(String openNewAccountProductId) {
        this.openNewAccountProductId = openNewAccountProductId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
