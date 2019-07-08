package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GrantTypeEnum {
    CODE("authorization_code");

    private String value;

    GrantTypeEnum(String value) {
        this.value = value;
    }

    @JsonCreator
    public static GrantTypeEnum fromValue(String text) {
        for (GrantTypeEnum b : GrantTypeEnum.values()) {
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
