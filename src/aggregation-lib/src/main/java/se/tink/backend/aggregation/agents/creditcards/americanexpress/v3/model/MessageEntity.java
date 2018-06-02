package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MessageEntity {
    private String shortValue;

    public String getShortValue() {
        return shortValue;
    }
}
