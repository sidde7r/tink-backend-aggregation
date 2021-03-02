package se.tink.libraries.transfer.rpc;

public enum Frequency {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly");

    private Frequency(String value) {
        this.value = value;
    }

    private final String value;

    @Override
    public String toString() {
        return this.value;
    }
}
