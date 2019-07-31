package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChargeBearerCodeEntity {
    SLEV("SLEV");

    private String value;

    ChargeBearerCodeEntity(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ChargeBearerCodeEntity fromValue(String text) {
        for (ChargeBearerCodeEntity b : ChargeBearerCodeEntity.values()) {
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
