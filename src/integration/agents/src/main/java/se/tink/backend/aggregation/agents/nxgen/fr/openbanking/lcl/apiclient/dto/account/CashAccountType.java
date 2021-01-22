package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CashAccountType {
    CACC("CACC"),
    CARD("CARD");

    private String value;

    CashAccountType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static CashAccountType fromValue(String text) {
        for (CashAccountType b : CashAccountType.values()) {
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
