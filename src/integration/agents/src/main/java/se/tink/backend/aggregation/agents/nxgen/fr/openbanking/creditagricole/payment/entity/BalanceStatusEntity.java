package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BalanceStatusEntity {
    CLBD("CLBD"),
    XPCD("XPCD"),
    VALU("VALU"),
    OTHR("OTHR");

    private String value;

    BalanceStatusEntity(String value) {
        this.value = value;
    }

    @JsonCreator
    public static BalanceStatusEntity fromValue(String text) {
        for (BalanceStatusEntity b : BalanceStatusEntity.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }
}
