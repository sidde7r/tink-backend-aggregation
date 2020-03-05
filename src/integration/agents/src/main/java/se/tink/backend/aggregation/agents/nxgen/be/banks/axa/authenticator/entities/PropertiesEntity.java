package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PropertiesEntity {

    private List<Entry> entry;

    @JsonObject
    public static class Entry {
        private String key;
        private String value;
    }
}
