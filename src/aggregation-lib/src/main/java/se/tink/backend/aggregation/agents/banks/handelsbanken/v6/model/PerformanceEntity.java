package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PerformanceEntity {
    private AmountEntity changeAmount;
    private QuantityEntity changePercentage;
    private String changeDirection;

    public AmountEntity getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(AmountEntity changeAmount) {
        this.changeAmount = changeAmount;
    }

    public QuantityEntity getChangePercentage() {
        return changePercentage;
    }

    public void setChangePercentage(
            QuantityEntity changePercentage) {
        this.changePercentage = changePercentage;
    }

    public String getChangeDirection() {
        return changeDirection;
    }

    public void setChangeDirection(String changeDirection) {
        this.changeDirection = changeDirection;
    }
}
