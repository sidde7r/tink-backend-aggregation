package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CashAccountTypeEnumEntity {
    CACC("CACC"),
    CARD("CARD");

    private String value;

    CashAccountTypeEnumEntity(String value) {
        this.value = value;
    }

    @JsonCreator
    public static CashAccountTypeEnumEntity fromValue(String text) {
        for (CashAccountTypeEnumEntity b : CashAccountTypeEnumEntity.values()) {
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
