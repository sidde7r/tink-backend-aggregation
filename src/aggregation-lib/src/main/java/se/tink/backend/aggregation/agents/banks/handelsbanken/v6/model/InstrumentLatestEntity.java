package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstrumentLatestEntity {
    private SecurityIdentifierEntity identifier;
    private String longName;
    private String lastUpdated;
    private AmountEntity lastPaid;
    private String changeToday;
    private QuantityEntity changeTodayPercentage;
    private String changeDirection;

    public SecurityIdentifierEntity getIdentifier() {
        return identifier;
    }

    public void setIdentifier(
            SecurityIdentifierEntity identifier) {
        this.identifier = identifier;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public AmountEntity getLastPaid() {
        return lastPaid;
    }

    public void setLastPaid(AmountEntity lastPaid) {
        this.lastPaid = lastPaid;
    }

    public String getChangeToday() {
        return changeToday;
    }

    public void setChangeToday(String changeToday) {
        this.changeToday = changeToday;
    }

    public QuantityEntity getChangeTodayPercentage() {
        return changeTodayPercentage;
    }

    public void setChangeTodayPercentage(
            QuantityEntity changeTodayPercentage) {
        this.changeTodayPercentage = changeTodayPercentage;
    }

    public String getChangeDirection() {
        return changeDirection;
    }

    public void setChangeDirection(String changeDirection) {
        this.changeDirection = changeDirection;
    }
}
