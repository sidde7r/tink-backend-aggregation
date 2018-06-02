package se.tink.backend.aggregation.workers.refresh;

public enum ProcessableItem {
    ACCOUNTS,
    TRANSACTIONS,
    EINVOICES,
    TRANSFER_DESTINATIONS;

    public String asMetricValue() {
        return name().toLowerCase();
    }
}
