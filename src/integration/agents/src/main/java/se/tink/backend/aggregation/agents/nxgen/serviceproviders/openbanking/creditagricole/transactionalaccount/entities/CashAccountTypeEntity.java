package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CashAccountTypeEntity {
    CACC("CACC"),
    CARD("CARD");

    private String value;

    CashAccountTypeEntity(String value) {
        this.value = value;
    }

    @JsonCreator
    public static CashAccountTypeEntity fromValue(String text) {
        for (CashAccountTypeEntity b : CashAccountTypeEntity.values()) {
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
