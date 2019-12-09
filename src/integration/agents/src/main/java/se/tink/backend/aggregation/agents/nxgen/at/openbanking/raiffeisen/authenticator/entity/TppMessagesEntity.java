package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TppMessagesEntity {

    private String code;
    private String text;

    public String getText() {
        return text;
    }

    public String getCode() {
        return code;
    }
}
