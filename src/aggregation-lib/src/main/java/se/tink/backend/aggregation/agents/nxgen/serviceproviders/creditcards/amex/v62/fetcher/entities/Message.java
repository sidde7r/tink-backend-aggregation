package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Message {
    private String type;
    private String shortValue;

    public String getType() {
        return type;
    }

    public String getShortValue() {
        return shortValue;
    }
}
