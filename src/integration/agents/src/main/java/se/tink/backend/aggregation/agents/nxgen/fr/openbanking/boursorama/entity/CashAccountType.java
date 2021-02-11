package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity;

public enum CashAccountType {
    CACC("CACC"),
    CARD("CARD");

    private String value;

    CashAccountType(String value) {
        this.value = value;
    }

    public static CashAccountType fromValue(String text) {
        for (CashAccountType b : CashAccountType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
