package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc;

public abstract class TimelineRequest {
    private String timeZone;
    private String timeZoneOffset;
    private int sortedIndex;
    private boolean pendingChargeEnabled;
    private String localTime;
    private String timestamp;

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

    public String getLocalTime() {
        return localTime;
    }

    public void setLocalTime(String localTime) {
        this.localTime = localTime;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
