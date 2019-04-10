package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MessageTypeEntity {
    @JsonProperty("Id")
    private int id;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Subscribing")
    private boolean subscribing;

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSubscribing() {
        return subscribing;
    }
}
