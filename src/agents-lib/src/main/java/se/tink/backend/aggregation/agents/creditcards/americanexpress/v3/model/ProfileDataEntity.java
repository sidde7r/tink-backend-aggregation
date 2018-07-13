package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProfileDataEntity {
    private String maskedUserId;
    private String data;

    public String getData() {
        return data;
    }
}
