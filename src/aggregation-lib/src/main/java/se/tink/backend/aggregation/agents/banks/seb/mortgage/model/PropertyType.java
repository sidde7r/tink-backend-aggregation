package se.tink.backend.aggregation.agents.banks.seb.mortgage.model;

public enum PropertyType {
    APARTMENT("BRF", "01"),
    VILLA("VILLA", "02"),
    VACATION_HOUSE("FRITIDSHUS", "03"),
    TENANCY("HYRESRÄTT", null),
    OTHERS("ANNAT", null);

    private final String key;
    private final String numerical;

    PropertyType(String key, String numerical) {
        this.key = key;
        this.numerical = numerical;
    }

    public String getKey() {
        return key;
    }

    public String getNumericalKey() {
        if (numerical == null) {
            throw new UnsupportedOperationException(String.format(
                    "%s.%s has no numerical representation",
                    PropertyType.class.getSimpleName(),
                    name()));
        }

        return numerical;
    }
}
