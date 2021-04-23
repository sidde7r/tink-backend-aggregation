package se.tink.libraries.transfer.rpc;

public enum Frequency {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    EVERY_TWO_WEEKS("EveryTwoWeeks"),
    MONTHLY("Monthly"),
    EVERY_TWO_MONTHS("EveryTwoMonths"),
    QUARTERLY("Quarterly"),
    SEMI_ANNUAL("SemiAnnual"),
    ANNUAL("Annual");

    Frequency(String value) {
        this.value = value;
    }

    private final String value;

    @Override
    public String toString() {
        return this.value;
    }
}
