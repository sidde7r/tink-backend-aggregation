package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TppMessagesEntity {
    private String category;
    private String code;
    private String text;

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }
}
