package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ServiceLevelCodeEntity {
    SEPA("SEPA");

    private String value;

    ServiceLevelCodeEntity(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ServiceLevelCodeEntity fromValue(String text) {
        for (ServiceLevelCodeEntity b : ServiceLevelCodeEntity.values()) {
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
