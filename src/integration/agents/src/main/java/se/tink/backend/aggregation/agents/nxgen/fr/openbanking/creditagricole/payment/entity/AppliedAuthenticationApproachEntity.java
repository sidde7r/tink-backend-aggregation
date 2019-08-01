package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AppliedAuthenticationApproachEntity {
    REDIRECT("REDIRECT");

    private String value;

    AppliedAuthenticationApproachEntity(String value) {
        this.value = value;
    }

    @JsonCreator
    public static AppliedAuthenticationApproachEntity fromValue(String text) {
        for (AppliedAuthenticationApproachEntity b : AppliedAuthenticationApproachEntity.values()) {
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
