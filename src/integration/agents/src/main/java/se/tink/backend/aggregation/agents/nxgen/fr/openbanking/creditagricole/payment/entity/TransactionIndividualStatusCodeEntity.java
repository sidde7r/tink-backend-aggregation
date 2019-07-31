package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionIndividualStatusCodeEntity {
    RJCT("RJCT"),
    PDNG("PDNG"),
    ACSP("ACSP"),
    ACSC("ACSC");

    private String value;

    TransactionIndividualStatusCodeEntity(String value) {
        this.value = value;
    }

    @JsonCreator
    public static TransactionIndividualStatusCodeEntity fromValue(String text) {
        for (TransactionIndividualStatusCodeEntity b :
                TransactionIndividualStatusCodeEntity.values()) {
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
