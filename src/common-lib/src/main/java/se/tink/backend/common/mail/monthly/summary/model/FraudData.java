package se.tink.backend.common.mail.monthly.summary.model;

public class FraudData {

    private boolean activated;
    private int updatedCount;
    private int unhandledEventsCount;
    private boolean fraudEnabledOnUserMarket;

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public void setUpdatedCount(int updatedCount) {
        this.updatedCount = updatedCount;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void setUnhandledEventsCount(int unhandledEventsCount) {
        this.unhandledEventsCount = unhandledEventsCount;
    }

    public int getUnhandledEventsCount() {
        return unhandledEventsCount;
    }

    public boolean isFraudActive() {
        return activated;
    }

    public boolean isFraudInactive(){
        return !activated;
    }

    public boolean isFraudEnabledOnUserMarket() {
        return fraudEnabledOnUserMarket;
    }

    public void setFraudEnabledOnUserMarket(boolean fraudEnabledOnUserMarket) {
        this.fraudEnabledOnUserMarket = fraudEnabledOnUserMarket;
    }
}
