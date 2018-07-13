package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MessageEntity {
    private String type;
    private String key;

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }
}
