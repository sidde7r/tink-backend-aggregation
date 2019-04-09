package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

public class TimelineRequest {
    private String timeZone;
    private String timeZoneOffset;
    private int sortedIndex;
    private boolean pendingChargeEnabled;
    private boolean cmlEnabled;
    private String localTime;

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(String timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    public int getSortedIndex() {
        return sortedIndex;
    }

    public void setSortedIndex(int sortedIndex) {
        this.sortedIndex = sortedIndex;
    }

    public boolean isPendingChargeEnabled() {
        return pendingChargeEnabled;
    }

    public void setPendingChargeEnabled(boolean pendingChargeEnabled) {
        this.pendingChargeEnabled = pendingChargeEnabled;
    }

    public boolean isCmlEnabled() {
        return cmlEnabled;
    }

    public void setCmlEnabled(boolean cmlEnabled) {
        this.cmlEnabled = cmlEnabled;
    }

    public String getLocalTime() {
        return localTime;
    }

    public void setLocalTime(String localTime) {
        this.localTime = localTime;
    }
}
